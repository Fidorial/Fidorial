package fr.euphyllia.fidorial.server.network.connection.listener;

import fr.euphyllia.fidorial.auth.GameProfile;
import fr.euphyllia.fidorial.auth.SessionKeyExchange;
import fr.euphyllia.fidorial.auth.SessionKeys;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.VersionConstants;
import fr.euphyllia.fidorial.server.configuration.ServerConfiguration.ProxyForwarding;
import fr.euphyllia.fidorial.server.events.player.PlayerLoginAttemptEventImpl;
import fr.euphyllia.fidorial.server.network.connection.ClientConnection;
import fr.euphyllia.fidorial.server.network.connection.ConnectionState;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.login.ClientboundApplyOptionsPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.login.ClientboundCustomQueryPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.login.ClientboundKeyPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.login.ClientboundLoginDisconnectPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.login.ClientboundLoginFinishedPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.login.ClientboundSetupCompressionPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.login.ConnectionOptions;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.LoginPacketListener;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.common.ServerboundCookieResponsePacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.login.ServerboundCustomQueryAnswerPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.login.ServerboundHelloPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.login.ServerboundKeyPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.login.ServerboundLoginAcknowledgedPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.login.ServerboundOptionsAppliedPacket;
import fr.euphyllia.fidorial.server.network.protocol.proxy.VelocityForwarding;
import fr.fidorial.entity.player.profile.PlayerProfile;
import fr.fidorial.event.player.PlayerLoginAttemptEvent;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public final class LoginPacketHandler implements LoginPacketListener {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(LoginPacketHandler.class);

    private final ClientConnection connection;
    private final FidorialServer server;

    private @Nullable SessionKeyExchange keyExchange;
    private @Nullable String pendingUsername;
    private int velocityTransactionId = -1;
    private boolean encryptionRequested = false;
    private boolean keyReceived = false;
    private boolean loginComplete = false;

    // Since 26.4-snapshot-5 encryption and compression are negotiated: the server announces them,
    // the client acknowledges, and only then are they switched on.
    private @Nullable SessionKeys pendingSessionKeys;
    private SessionKeys.@Nullable InitializationVectors pendingIvs;
    private @Nullable GameProfile pendingProfile;
    private int compressionThreshold = -1;

    // Cookies asked for by the listeners of the login attempt event: the decision waits for their answers.
    private int pendingCookieChecks;
    private boolean cookieCheckFailed;
    private @Nullable Runnable afterCookieChecks;

    public LoginPacketHandler(final ClientConnection connection) {
        this.connection = connection;
        this.server = connection.server();
    }

    private boolean behindVelocity() {
        return server.config().network().proxy() instanceof ProxyForwarding.Velocity;
    }

    @Override
    public void handleHello(final ServerboundHelloPacket packet) {
        if (pendingUsername != null) {
            connection.close();
            return;
        }
        if (connection.clientProtocol() < VersionConstants.PROTOCOL_VERSION) {
            disconnect(Component.translatable("multiplayer.disconnect.outdated_client", Component.text(VersionConstants.MINECRAFT_VERSION_NAME)));
            return;
        } else if (connection.clientProtocol() > VersionConstants.PROTOCOL_VERSION) {
            disconnect(Component.translatable("multiplayer.disconnect.outdated_server", Component.text(VersionConstants.MINECRAFT_VERSION_NAME)));
            return;
        }
        this.pendingUsername = packet.username();
        connection.setUsername(pendingUsername);
        if (behindVelocity()) {
            sendVelocityForwardingRequest();
        } else if (server.config().network().onlineMode()) {
            sendEncryptionRequest(true);
        } else {
            LOGGER.warn("Offline connection (unauthenticated): {}", pendingUsername);
            if (server.config().network().encryptOfflineModeConnections()) {
                sendEncryptionRequest(false);
            } else {
                announceCompression();
                sendLoginSuccess(offlineProfile(pendingUsername));
            }
        }
    }

    private static GameProfile offlineProfile(final String username) {
        final UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8));
        return new GameProfile(uuid, username, UUID.randomUUID(), List.of());
    }

    private void sendVelocityForwardingRequest() {
        this.velocityTransactionId = ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE);
        final byte[] requestedVersion = {(byte) VelocityForwarding.MAX_SUPPORTED_VERSION};
        connection.send(
                new ClientboundCustomQueryPacket(velocityTransactionId, VelocityForwarding.CHANNEL, requestedVersion));
    }

    @Override
    public void handleCustomQueryAnswer(final ServerboundCustomQueryAnswerPacket packet) {
        if (!(server.config().network().proxy() instanceof ProxyForwarding.Velocity(final String secret))
                || packet.transactionId() != velocityTransactionId) {
            LOGGER.trace("Unexpected custom_query_answer (id {}), ignored", packet.transactionId());
            return;
        }
        velocityTransactionId = -1;
        if (!packet.understood()) {
            disconnect("This server only accepts connections via the Velocity proxy.");
            return;
        }
        try {
            final VelocityForwarding.ForwardedData data = VelocityForwarding.decode(packet.payload(), secret);
            connection.setForwardedAddress(data.remoteAddress());
            connection.setUsername(data.profile().name());
            this.pendingUsername = data.profile().name();
            LOGGER.info(
                    "Player transferred by Velocity: {} ({}) from {}",
                    data.profile().name(),
                    data.profile().uuid(),
                    data.remoteAddress());
            announceCompression();
            sendLoginSuccess(data.profile());
        } catch (final VelocityForwarding.ForwardingException e) {
            LOGGER.warn("Velocity forwarding rejected for {}: {}", pendingUsername, e.getMessage());
            disconnect("Invalid Velocity forwarding data");
        }
    }

    /**
     * Starts the key exchange (an X25519 agreement since 26.4-snapshot-9).
     *
     * @param shouldAuthenticate whether the client must join through Mojang's session server; false
     *                           for an encrypted offline mode connection
     */
    private void sendEncryptionRequest(final boolean shouldAuthenticate) {
        this.encryptionRequested = true;
        final SessionKeyExchange exchange = server.network().keyExchanges().create(
                SessionKeyExchange.SERVER_NAME, VersionConstants.PROTOCOL_VERSION, shouldAuthenticate);
        this.keyExchange = exchange;
        connection.send(new ClientboundKeyPacket(
                exchange.salt(), exchange.serverName(), exchange.encodedPublicKey(), exchange.authenticationRequested()));
    }

    @Override
    public void handleKey(final ServerboundKeyPacket packet) {
        final SessionKeyExchange exchange = this.keyExchange;
        final String username = pendingUsername;
        if (!encryptionRequested || keyReceived || loginComplete || exchange == null || username == null) {
            connection.close();
            return;
        }
        this.keyReceived = true;
        this.keyExchange = null;

        final SessionKeys keys;
        final byte[] integrityDigest;
        try {
            final PublicKey clientKey = SessionKeyExchange.decodePublicKey(packet.clientKey());
            keys = exchange.createSessionKeys(clientKey, username);
            integrityDigest = keys.computeIntegrityDigest();
        } catch (final GeneralSecurityException | RuntimeException e) {
            LOGGER.warn("Key exchange failed for {}", username, e);
            disconnect(Component.translatable("disconnect.packetError"));
            return;
        }
        if (!MessageDigest.isEqual(integrityDigest, packet.signature())) {
            LOGGER.warn("Key exchange signature mismatch for {}, potential tampering detected", username);
            disconnect(Component.translatable("disconnect.packetError"));
            return;
        }
        // Kept aside: encryption is switched on once the client answers apply_options.
        this.pendingSessionKeys = keys;
        this.pendingIvs = SessionKeys.initializationVectors(integrityDigest);

        if (!exchange.authenticationRequested()) {
            // Encrypted offline mode: the client did not join through Mojang, there is nothing to check.
            applyEncryption(offlineProfile(username));
            return;
        }
        final String serverHash = keys.computeKeysAuthenticationDigest();
        Thread.startVirtualThread(() -> authenticate(username, serverHash));
    }

    private void authenticate(final String username, final String serverHash) {
        try {
            final Optional<GameProfile> profile = server.network().sessionService().hasJoined(username, serverHash);
            connection.execute(() -> {
                if (profile.isEmpty()) {
                    disconnect(Component.translatable("disconnect.loginFailedInfo.invalidSession"));
                } else {
                    applyEncryption(profile.get());
                }
            });
        } catch (final Exception e) {
            LOGGER.warn("Mojang session unreachable for {}", username, e);
            connection.execute(() -> disconnect(Component.translatable("disconnect.loginFailedInfo.serversUnavailable")));
        }
    }

    private void applyEncryption(final GameProfile profile) {
        if (!connection.isActive()) {
            return;
        }
        this.pendingProfile = profile;
        connection.send(new ClientboundApplyOptionsPacket(ConnectionOptions.ENCRYPTION));
    }

    @Override
    public void handleOptionsApplied(final ServerboundOptionsAppliedPacket packet) {
        final SessionKeys keys = this.pendingSessionKeys;
        final SessionKeys.InitializationVectors ivs = this.pendingIvs;
        final GameProfile profile = this.pendingProfile;
        if (keys == null || ivs == null || profile == null) {
            LOGGER.warn("options_applied rejected for {}: no pending encryption", pendingUsername);
            disconnect(Component.translatable("disconnect.packetError"));
            return;
        }
        this.pendingSessionKeys = null;
        this.pendingIvs = null;
        this.pendingProfile = null;
        try {
            connection.installEncryption(keys, ivs);
        } catch (final Exception e) {
            LOGGER.warn("Encryption failed for {}", pendingUsername, e);
            disconnect(Component.translatable("disconnect.packetError"));
            return;
        }
        announceCompression();
        sendLoginSuccess(profile);
    }

    /**
     * Sends the threshold only: compression is switched on when login_acknowledged arrives,
     * as announced by the COMPRESSION flag of login_finished.
     */
    private void announceCompression() {
        final int threshold = server.config().network().compressionThreshold();
        if (threshold < 0) {
            return;
        }
        this.compressionThreshold = threshold;
        connection.send(new ClientboundSetupCompressionPacket(threshold));
    }

    private byte loginFinishedOptions() {
        return compressionThreshold >= 0 ? ConnectionOptions.COMPRESSION : 0;
    }

    private void sendLoginSuccess(final GameProfile profile) {
        final List<PlayerProfile.Property> properties = profile.properties().stream()
                .map(p -> new PlayerProfile.Property(p.name(), p.value(), p.signature()))
                .toList();
        final PlayerProfile playerProfile = new PlayerProfile(profile.uuid(), profile.name(), properties);
        final boolean authenticated = server.config().network().onlineMode() || behindVelocity();
        final String address = connection.remoteAddress();

        final PlayerLoginAttemptEvent attempt = new PlayerLoginAttemptEventImpl(
                playerProfile, address, authenticated, connection.transferred(), this::requestLoginCookie);

        loginRefusal(playerProfile, connection.remoteInetAddress()).ifPresent(attempt::refuse);

        final PlayerLoginAttemptEvent event;
        try {
            event = server.events().post(attempt);
        } catch (final Throwable t) {
            LOGGER.error("Login attempt of {} failed, connection refused", playerProfile.name(), t);
            connection.execute(() -> disconnect(Component.translatable("multiplayer.disconnect.generic")));
            return;
        }
        connection.execute(() -> afterCookieChecks(() -> decideLogin(event, profile, playerProfile)));
    }

    private void decideLogin(final PlayerLoginAttemptEvent event, final GameProfile profile, final PlayerProfile playerProfile) {
        if (!connection.isActive()) {
            return;
        }
        if (cookieCheckFailed) {
            LOGGER.info("Login refused for {} ({}): a cookie could not be checked", playerProfile.name(), playerProfile.uuid());
            disconnect(Component.translatable("multiplayer.disconnect.generic"));
            return;
        }
        if (event.isCancelled()) {
            LOGGER.info("Login refused for {} ({})", playerProfile.name(), playerProfile.uuid());
            disconnect(event.refusal().orElseGet(() -> Component.translatable("multiplayer.disconnect.generic")));
            return;
        }
        completeLogin(profile, playerProfile);
    }

    /**
     * Asks the client for a cookie on behalf of a listener of the login attempt event. The decision on the
     * login waits for the answer; a cookie that cannot be checked (the action throws, or the client cannot
     * be asked) refuses the connection.
     */
    private void requestLoginCookie(final Key key, final Consumer<Optional<byte[]>> action) {
        pendingCookieChecks++;
        connection.requestCookie(key).whenComplete((cookie, failure) -> {
            if (failure != null) {
                cookieCheckFailed = true;
            } else {
                try {
                    action.accept(cookie);
                } catch (final Throwable t) {
                    LOGGER.error("Checking the cookie {} of {} failed", key.asString(), pendingUsername, t);
                    cookieCheckFailed = true;
                }
            }
            pendingCookieChecks--;
            final Runnable next = afterCookieChecks;
            if (pendingCookieChecks == 0 && next != null) {
                afterCookieChecks = null;
                next.run();
            }
        });
    }

    private void afterCookieChecks(final Runnable next) {
        if (pendingCookieChecks == 0) {
            next.run();
        } else {
            afterCookieChecks = next;
        }
    }

    @Override
    public void handleCookieResponse(final ServerboundCookieResponsePacket packet) {
        connection.handleCookieResponse(packet);
    }

    private Optional<Component> loginRefusal(final PlayerProfile profile, @Nullable final InetAddress address) {
        return server.players().bans().findAny(profile.uuid(), address)
                .map(server.players().bans()::disconnectMessage)
                .or(() -> server.players().whitelist().allows(profile.uuid())
                        ? Optional.empty()
                        : Optional.of(Component.translatable("multiplayer.disconnect.not_whitelisted")));
    }

    private void completeLogin(final GameProfile profile, final PlayerProfile playerProfile) {
        this.loginComplete = true;
        LOGGER.info("Authenticated: {} ({}){}", profile.name(), profile.uuid(),
                connection.transferred() ? ", transferred from another server" : "");
        connection.setProfile(playerProfile);
        server.players().offline().remember(playerProfile);
        connection.send(new ClientboundLoginFinishedPacket(profile, loginFinishedOptions()));
    }

    @Override
    public void handleLoginAcknowledged(final ServerboundLoginAcknowledgedPacket packet) {
        // The client switches compression on right after sending this packet.
        if (ConnectionOptions.isCompression(loginFinishedOptions())) {
            connection.installCompression(compressionThreshold);
            LOGGER.debug("Compression enabled (threshold {}) for {}", compressionThreshold, pendingUsername);
        }
        if (!loginComplete || connection.profile() == null) {
            LOGGER.warn("login_acknowledged rejected for {}: login not completed", pendingUsername);
            disconnect(Component.translatable("disconnect.packetError"));
            return;

        }
        connection.setState(ConnectionState.CONFIGURATION);
    }

    private void disconnect(final String reason) {
        connection.sendAndClose(ClientboundLoginDisconnectPacket.ofComponent(Component.text(reason)));
    }

    private void disconnect(final Component reason) {
        connection.sendAndClose(ClientboundLoginDisconnectPacket.ofComponent(reason));
    }
}
