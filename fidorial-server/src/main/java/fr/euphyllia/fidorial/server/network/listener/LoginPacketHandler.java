package fr.euphyllia.fidorial.server.network.listener;

import fr.euphyllia.fidorial.auth.EncryptionUtils;
import fr.euphyllia.fidorial.auth.GameProfile;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.VersionConstants;
import fr.euphyllia.fidorial.server.configuration.ServerConfiguration.ProxyForwarding;
import fr.euphyllia.fidorial.server.network.ClientConnection;
import fr.euphyllia.fidorial.server.network.ConnectionState;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.login.ClientboundCustomQueryPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.login.ClientboundHelloPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.login.ClientboundLoginCompressionPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.login.ClientboundLoginDisconnectPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.login.ClientboundLoginFinishedPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.LoginPacketListener;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.login.ServerboundCustomQueryAnswerPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.login.ServerboundHelloPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.login.ServerboundKeyPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.login.ServerboundLoginAcknowledgedPacket;
import fr.euphyllia.fidorial.server.network.proxy.VelocityForwarding;
import fr.fidorial.entity.PlayerProfile;
import fr.fidorial.event.player.PlayerLoginAttemptEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import javax.crypto.SecretKey;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class LoginPacketHandler implements LoginPacketListener {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(LoginPacketHandler.class);

    private final ClientConnection connection;
    private final FidorialServer server;

    private byte @Nullable [] verifyToken;
    private @Nullable String pendingUsername;
    private int velocityTransactionId = -1;
    private boolean encryptionRequested = false;
    private boolean loginComplete = false;

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
                enableCompression();
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
            enableCompression();
            sendLoginSuccess(data.profile());
        } catch (final VelocityForwarding.ForwardingException e) {
            LOGGER.warn("Velocity forwarding rejected for {}: {}", pendingUsername, e.getMessage());
            disconnect("Invalid Velocity forwarding data");
        }
    }

    private void sendEncryptionRequest(final boolean shouldAuthenticate) {
        this.encryptionRequested = true;
        this.verifyToken = EncryptionUtils.generateVerifyToken();
        final byte[] publicKey = server.keyPair().getPublic().getEncoded();
        connection.send(new ClientboundHelloPacket("", publicKey, verifyToken, shouldAuthenticate));
    }

    @Override
    public void handleKey(final ServerboundKeyPacket packet) {
        if (!encryptionRequested || loginComplete) {
            connection.close();
            return;
        }
        try {
            final byte[] token = EncryptionUtils.decryptRsa(server.keyPair().getPrivate(), packet.encryptedToken());
            if (!Arrays.equals(token, verifyToken)) {
                disconnect(Component.translatable("disconnect.loginFailedInfo.invalidSession"));
                return;
            }
            final byte[] sharedSecret = EncryptionUtils.decryptRsa(server.keyPair().getPrivate(), packet.encryptedSecret());
            final SecretKey key = EncryptionUtils.toAesKey(sharedSecret);
            connection.installEncryption(key);

            final String serverHash = EncryptionUtils.computeServerHash(
                    "", sharedSecret, server.keyPair().getPublic());
            final String username = pendingUsername;
            Thread.startVirtualThread(() -> authenticate(username, serverHash));
        } catch (final Exception e) {
            LOGGER.warn("Encryption failed for {}", pendingUsername, e);
            disconnect(Component.translatable("disconnect.packetError"));
        }
    }

    private void authenticate(final String username, final String serverHash) {
        try {
            final Optional<GameProfile> profile = server.sessionService().hasJoined(username, serverHash);
            connection.execute(() -> {
                if (!server.config().network().onlineMode()) {
                    enableCompression();
                    sendLoginSuccess(offlineProfile(username));
                    return;
                }
                if (profile.isEmpty()) {
                    disconnect(Component.translatable("disconnect.loginFailedInfo.invalidSession"));
                } else {
                    enableCompression();
                    sendLoginSuccess(profile.get());
                }
            });
        } catch (final Exception e) {
            LOGGER.warn("Mojang session unreachable for {}", username, e);
            connection.execute(() -> disconnect(Component.translatable("disconnect.loginFailedInfo.serversUnavailable")));
        }
    }

    private void enableCompression() {
        final int threshold = server.config().network().compressionThreshold();
        if (threshold < 0) {
            return;
        }
        connection.send(new ClientboundLoginCompressionPacket(threshold));
        connection.installCompression(threshold);
        LOGGER.debug("Compression enabled (threshold {}) for {}", threshold, pendingUsername);
    }

    private void sendLoginSuccess(final GameProfile profile) {
        final List<PlayerProfile.Property> properties = profile.properties().stream()
                .map(p -> new PlayerProfile.Property(p.name(), p.value(), p.signature()))
                .toList();
        final PlayerProfile playerProfile = new PlayerProfile(profile.uuid(), profile.name(), properties);
        final boolean authenticated = server.config().network().onlineMode() || behindVelocity();
        final String address = connection.remoteAddress();

        final PlayerLoginAttemptEvent attempt = new PlayerLoginAttemptEvent(playerProfile, address, authenticated);

        loginRefusal(playerProfile, connection.remoteInetAddress()).ifPresent(attempt::refuse);

        final PlayerLoginAttemptEvent event;
        try {
            event = server.events().post(attempt);
        } catch (final Throwable t) {
            LOGGER.error("Login attempt of {} failed, connection refused", playerProfile.name(), t);
            connection.execute(() -> disconnect(Component.translatable("multiplayer.disconnect.generic")));
            return;
        }
        connection.execute(() -> {
            if (!connection.isActive()) {
                return;
            }
            if (event.isCancelled()) {
                LOGGER.info("Login refused for {} ({})", playerProfile.name(), playerProfile.uuid());
                disconnect(event.refusal().orElseGet(() -> Component.translatable("multiplayer.disconnect.generic")));
                return;
            }
            completeLogin(profile, playerProfile);
        });
    }

    private Optional<Component> loginRefusal(final PlayerProfile profile, @Nullable final InetAddress address) {
        return server.bans().findAny(profile.uuid(), address)
                .map(server.bans()::disconnectMessage)
                .or(() -> server.whitelist().allows(profile.uuid())
                        ? Optional.empty()
                        : Optional.of(Component.translatable("multiplayer.disconnect.not_whitelisted")));
    }

    private void completeLogin(final GameProfile profile, final PlayerProfile playerProfile) {
        this.loginComplete = true;
        LOGGER.info("Authenticated: {} ({})", profile.name(), profile.uuid());
        connection.setProfile(playerProfile);
        server.offlinePlayers().remember(playerProfile);
        connection.send(new ClientboundLoginFinishedPacket(profile));
    }

    @Override
    public void handleLoginAcknowledged(final ServerboundLoginAcknowledgedPacket packet) {
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
