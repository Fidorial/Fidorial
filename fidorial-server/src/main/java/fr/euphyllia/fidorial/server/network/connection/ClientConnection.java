package fr.euphyllia.fidorial.server.network.connection;

import com.google.common.base.Preconditions;
import com.google.common.net.InetAddresses;
import fr.euphyllia.fidorial.auth.EncryptionUtils;
import fr.euphyllia.fidorial.auth.SessionKeys;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.chat.FilterType;
import fr.euphyllia.fidorial.server.chat.IdentifiedSignedMessage;
import fr.euphyllia.fidorial.server.chat.LastSeenMessages;
import fr.euphyllia.fidorial.server.chat.SignedChatSession;
import fr.euphyllia.fidorial.server.chat.SignedMessageChain;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.network.connection.listener.ConfigurationPacketHandler;
import fr.euphyllia.fidorial.server.network.connection.listener.HandshakePacketHandler;
import fr.euphyllia.fidorial.server.network.connection.listener.LoginPacketHandler;
import fr.euphyllia.fidorial.server.network.connection.listener.PlayPacketHandler;
import fr.euphyllia.fidorial.server.network.connection.listener.StatusPacketHandler;
import fr.euphyllia.fidorial.server.network.protocol.ProtocolMap;
import fr.euphyllia.fidorial.server.network.protocol.catalog.ConfigurationClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.catalog.LoginClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.ServerboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.common.ClientboundClearDialogPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.common.ClientboundCookieRequestPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.common.ClientboundCustomPayloadPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.common.ClientboundPostEffectsPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.common.ClientboundResourcePackPopPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.common.ClientboundResourcePackPushPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.common.ClientboundShowDialogPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.common.ClientboundStoreCookiePacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.common.ClientboundTransferPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.configuration.ClientboundResetChatPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.login.ClientboundLoginDisconnectPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundDeleteMessagePacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundDisconnectPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundKeepAlivePacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundPlayerChatPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.common.ServerboundCookieResponsePacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.common.ServerboundCustomPayloadPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
import fr.euphyllia.fidorial.server.network.serialization.util.CipherDecoder;
import fr.euphyllia.fidorial.server.network.serialization.util.CipherEncoder;
import fr.euphyllia.fidorial.server.network.serialization.util.CompressionDecoder;
import fr.euphyllia.fidorial.server.network.serialization.util.CompressionEncoder;
import fr.euphyllia.fidorial.server.translation.BuiltInTranslationStore;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.fidorial.command.CommandSource;
import fr.fidorial.dialog.DialogDefinition;
import fr.fidorial.dialog.DialogReference;
import fr.fidorial.entity.RespawnPoint;
import fr.fidorial.entity.player.profile.PlayerProfile;
import fr.fidorial.event.player.PlayerJoinEvent;
import fr.fidorial.event.player.PlayerRespawnEvent;
import fr.fidorial.math.Location;
import fr.fidorial.messaging.PluginMessenger;
import fr.fidorial.protocol.PacketListener;
import fr.fidorial.protocol.ServerboundPacket;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.storage.player.PlayerDataStorage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.embedded.EmbeddedChannel;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.chat.ChatType;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.dialog.DialogLike;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.pointer.Pointers;
import net.kyori.adventure.pointer.PointersSupplier;
import net.kyori.adventure.resource.ResourcePackCallback;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.resource.ResourcePackStatus;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import javax.crypto.Cipher;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.SequencedCollection;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public final class ClientConnection extends SimpleChannelInboundHandler<ByteBuf> implements Audience {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(ClientConnection.class);

    private static final Executor VIRTUAL_THREAD_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    private static final int KEEP_ALIVE_INTERVAL_SECONDS = 10;
    private static final int LATENCY_SMOOTHING = 3;
    private static final Key BRAND_CHANNEL = Key.key("brand");
    private static final int MAX_BRAND_LENGTH = 256;
    // Bounds what a client can make the server remember: as many channels as Velocity lets a client register.
    private static final int MAX_PLUGIN_CHANNELS = 1024;

    private static final PointersSupplier<ClientConnection> pointers = PointersSupplier.<ClientConnection>builder()
            .resolving(Identity.NAME, connection -> {
                final PlayerProfile profile = connection.profile();
                return profile != null ? profile.name() : "<unknown>";
            })
            .resolving(Identity.UUID, connection -> {
                final PlayerProfile profile = connection.profile();
                return profile != null ? profile.uuid() : new UUID(0L, 0L);
            })
            .resolving(Identity.LOCALE, ClientConnection::locale)
            .build();

    private final FidorialServer server;
    private final ProtocolMap protocol;

    private @Nullable ChannelHandlerContext ctx;
    private ConnectionState state;
    private PacketListener listener;
    private @Nullable ConnectionState rejectedState;
    private @Nullable List<Class<? extends ServerboundPacket>> exemptPackets;

    private int clientProtocol;
    private boolean transferred;
    private @Nullable String username;
    private @Nullable PlayerProfile profile;
    private @Nullable ServerPlayer player;
    private int displayedSkinParts = 0x7F; // toutes les couches activees par defaut
    private int viewDistance = 2; // minimum
    private @Nullable String forwardedAddress;
    private Locale locale = BuiltInTranslationStore.DEFAULT_LOCALE;
    private @Nullable ScheduledFuture<?> keepAliveTask;
    private volatile @Nullable SignedChatSession chatSession;
    private volatile @Nullable SignedMessageChain chatChain;
    private final LastSeenMessages lastSeenMessages = new LastSeenMessages();
    private final AtomicInteger nextGlobalChatIndex = new AtomicInteger();
    private volatile List<Key> postEffects = List.of();
    private final Map<Key, CompletableFuture<Optional<byte[]>>> pendingCookies = new ConcurrentHashMap<>();
    // The plugin message channels the client, and the proxy in front of the server, announced.
    private final Set<Key> pluginChannels = ConcurrentHashMap.newKeySet();
    private volatile @Nullable String clientBrand;

    private volatile long pendingKeepAliveId;
    private volatile int latencyMillis;

    public ClientConnection(final FidorialServer server) {
        this.server = server;
        this.protocol = server.network().protocolMap();
        this.state = ConnectionState.HANDSHAKE;
        this.listener = new HandshakePacketHandler(this);
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
        super.channelActive(ctx);
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final ByteBuf raw) {
        final CodecAwarePacketBuffer buf = new CodecAwarePacketBuffer(raw, server.registries().serverRegistries());
        final int packetId = buf.readVarInt();

        final Key name = protocol.serverboundName(state, packetId);
        if (name == null) {
            LOGGER.trace("Unknown packet {} 0x{} (ignored)", state, Integer.toHexString(packetId));
            return;
        }
        final ServerboundPacket packet = ServerboundPackets.decode(state, name, buf);
        if (packet == null || isRejected(packet.getClass())) {
            LOGGER.trace("{}: {} received (unmanaged, ignored)", state, name);
            return;
        }
        packet.handle(listener);
    }

    public void setState(final ConnectionState newState) {
        this.state = newState;
        this.rejectedState = null;
        this.exemptPackets = null;
        this.listener = createListener(newState);
        this.listener.onEnter();
    }

    private PacketListener createListener(final ConnectionState newState) {
        return switch (newState) {
            case HANDSHAKE -> new HandshakePacketHandler(this);
            case STATUS -> new StatusPacketHandler(this);
            case LOGIN -> new LoginPacketHandler(this);
            case CONFIGURATION -> new ConfigurationPacketHandler(this);
            case PLAY, MOCK_PLAY -> new PlayPacketHandler(this);
        };
    }

    public void send(final ClientboundPacket<?> packet) {
        this.send(packet, false);
    }

    public void send(final ClientboundPacket<?> packet, final boolean bypassRejection) {
        if (context().channel().eventLoop().inEventLoop()) {
            sendNow(packet, bypassRejection);
        } else {
            execute(() -> sendNow(packet, bypassRejection));
        }
    }

    private void sendNow(final ClientboundPacket<?> packet, final boolean bypassRejection) {
        final ChannelFuture future = write(packet, bypassRejection);
        if (future != null) {
            future.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        }
    }

    public void sendAndClose(final ClientboundPacket<?> packet) {
        this.sendAndClose(packet, false);
    }

    public void sendAndClose(final ClientboundPacket<?> packet, final boolean bypassRejection) {
        if (!context().channel().eventLoop().inEventLoop()) {
            execute(() -> sendAndClose(packet, bypassRejection));
            return;
        }
        final ChannelFuture future = write(packet, bypassRejection);
        if (future != null) {
            future.addListener(ChannelFutureListener.CLOSE);
        } else {
            close();
        }
    }

    public void disconnect(final Component reason) {
        if (state == ConnectionState.LOGIN) {
            sendAndClose(ClientboundLoginDisconnectPacket.ofComponent(reason));
        } else {
            LOGGER.info(
                    Component.text("Disconnection of ")
                            .append(Component.text(username == null ? "<unknown>" : username)) // shouldn't ever be null but idea doesn't want to shut up
                            .append(Component.text(": "))
                            .append(reason)
            );
            sendAndClose(new ClientboundDisconnectPacket(reason));
        }
    }

    private @Nullable ChannelFuture write(final ClientboundPacket<?> packet, final boolean bypassRejection) {
        if (!isActive() || state == ConnectionState.MOCK_PLAY) {
            return null;
        }

        if (this.rejectedState != null && this.rejectedState == this.state) {
            if (!bypassRejection) {
                return null;
            }
        }

        final ByteBuf out = context().alloc().buffer();
        try {
            final CodecAwarePacketBuffer p = new CodecAwarePacketBuffer(out, server.registries().serverRegistries());
            p.writeVarInt(protocol.clientboundId(state, packet.name()));
            packet.write(p);
        } catch (final Throwable t) {
            out.release();
            throw t;
        }
        return context().writeAndFlush(out);
    }

    public void close() {
        context().close();
    }

    /**
     * Checks whether this connection is still usable.
     *
     * @return {@code true} if the underlying channel is open and connected
     */
    public boolean isActive() {
        return ctx != null && ctx.channel().isActive();
    }

    private ChannelHandlerContext context() {
        final ChannelHandlerContext context = this.ctx;
        Preconditions.checkState(context != null, "The channel of this connection is not active yet");
        return context;
    }

    public boolean isRejected(final Class<? extends ServerboundPacket> packet) {
        final List<Class<? extends ServerboundPacket>> exempt = this.exemptPackets;
        if (exempt != null) {
            return this.state.equals(this.rejectedState) && !exempt.contains(packet);
        }
        return this.state.equals(this.rejectedState);
    }

    public void execute(final Runnable task) {
        context().channel().eventLoop().execute(task);
    }

    /**
     * Switches encryption on, with the keys agreed on during the login.
     *
     * @param keys the keys of the session
     * @param ivs  the initialization vectors drawn from the session's integrity digest
     * @throws GeneralSecurityException if a cipher cannot be created
     */
    public void installEncryption(final SessionKeys keys, final SessionKeys.InitializationVectors ivs)
            throws GeneralSecurityException {
        // The server is side A of the exchange: it reads what B (the client) sends and writes towards B.
        final Cipher decrypt = EncryptionUtils.createStreamCipher(Cipher.DECRYPT_MODE, keys.streamBToA(), ivs.clientToServer());
        final Cipher encrypt = EncryptionUtils.createStreamCipher(Cipher.ENCRYPT_MODE, keys.streamAToB(), ivs.serverToClient());
        context().pipeline().addBefore("frame-decoder", "cipher-decoder", new CipherDecoder(decrypt));
        context().pipeline().addBefore("frame-decoder", "cipher-encoder", new CipherEncoder(encrypt));
    }

    public void installCompression(final int threshold) {
        context().pipeline().addBefore("handler", "decompress", new CompressionDecoder(threshold));
        context().pipeline().addBefore("handler", "compress", new CompressionEncoder(threshold));
    }

    public void startKeepAlive() {
        keepAliveTask = context().channel()
                .eventLoop()
                .scheduleAtFixedRate(
                        this::sendKeepAlive,
                        KEEP_ALIVE_INTERVAL_SECONDS,
                        KEEP_ALIVE_INTERVAL_SECONDS,
                        TimeUnit.SECONDS);
    }

    private void sendKeepAlive() {
        final long id = System.currentTimeMillis();
        pendingKeepAliveId = id;
        send(new ClientboundKeepAlivePacket(id));
    }

    public void acknowledgeKeepAlive(final long id) {
        if (id != pendingKeepAliveId || id == 0L) {
            return;
        }
        pendingKeepAliveId = 0L;

        final long sample = Math.clamp(System.currentTimeMillis() - id, 0L, Integer.MAX_VALUE);
        final int previous = latencyMillis;
        latencyMillis = previous == 0
                ? (int) sample
                : (int) ((previous * (long) LATENCY_SMOOTHING + sample) / (LATENCY_SMOOTHING + 1));
    }

    public void pauseKeepAlive() {
        if (keepAliveTask != null) {
            keepAliveTask.cancel(false);
            keepAliveTask = null;
        }
    }

    public int ping() {
        return latencyMillis;
    }

    public @Nullable SignedChatSession chatSession() {
        return chatSession;
    }

    public void setChatSession(final SignedChatSession session) {
        this.chatSession = session;
        final PlayerProfile p = this.profile;
        this.chatChain = p != null ? new SignedMessageChain(p.uuid()) : null;
    }

    public void resetChatSession() {
        this.chatSession = null;
        this.chatChain = null;
        this.lastSeenMessages.clear();
        nextGlobalChatIndex.set(0);
        send(new ClientboundResetChatPacket());
    }

    public @Nullable SignedMessageChain chatChain() {
        return chatChain;
    }

    public LastSeenMessages lastSeen() {
        return lastSeenMessages;
    }

    public void sendSignedMessage(final SignedMessage message, final ChatType.Bound chatType) {
        final ServerPlayer recipient = player();
        if (recipient == null) {
            return;
        }

        final Component unsigned = recipient.prepareMessageForSend(message.unsignedContent());
        final List<ClientboundPlayerChatPacket.PackedSignature> lastSeen =
                message instanceof final IdentifiedSignedMessage verified ? packLastSeen(verified.lastSeen()) : List.of();

        final Runnable write = () -> {
            final int globalIndex = nextGlobalChatIndex.getAndIncrement();
            final int index = message instanceof final IdentifiedSignedMessage verified ? verified.index() : globalIndex;
            final SignedMessage.Signature ownSignature = message.signature();
            send(new ClientboundPlayerChatPacket(
                    globalIndex,
                    message.identity().uuid(),
                    index,
                    ownSignature != null ? ownSignature.bytes() : null,
                    message.message(),
                    message.timestamp(),
                    message.salt(),
                    lastSeen,
                    unsigned,
                    FilterType.PASS_THROUGH,
                    chatType,
                    recipient));
            if (ownSignature != null) {
                lastSeenMessages.push(ownSignature);
            }
        };
        if (context().channel().eventLoop().inEventLoop()) {
            write.run();
        } else {
            execute(write);
        }
    }

    private List<ClientboundPlayerChatPacket.PackedSignature> packLastSeen(final List<SignedMessage.Signature> lastSeen) {
        final List<ClientboundPlayerChatPacket.PackedSignature> packed = new ArrayList<>(lastSeen.size());
        for (final SignedMessage.Signature signature : lastSeen) {
            packed.add(ClientboundPlayerChatPacket.PackedSignature.full(signature.bytes()));
        }
        return packed;
    }

    public void deleteSignedMessage(final SignedMessage.Signature signature) {
        send(ClientboundDeleteMessagePacket.full(signature.bytes()));
    }

    @Override
    public void handlerRemoved(final ChannelHandlerContext ctx) {
        if (keepAliveTask != null) {
            keepAliveTask.cancel(false);
        }
        cancelCookieRequests();
        try {
            listener.onDisconnect();
        } catch (final Throwable t) {
            LOGGER.error("Error during onDisconnect", t);
        }
        server.players().disconnect(this);
        saveInventoryOnDisconnect().exceptionally(throwable -> {
            LOGGER.error("Unable to save inventory on disconnect", throwable);
            return null;
        });
    }

    public CompletableFuture<Boolean> enterConfiguration() {
        return isActive() && state != ConnectionState.MOCK_PLAY
                ? CompletableFuture.supplyAsync(this::startConfiguration, this::execute).thenCompose(Function.identity())
                : CompletableFuture.completedFuture(false);
    }

    private CompletableFuture<Boolean> startConfiguration() {
        if (isActive() && rejectedState != ConnectionState.PLAY && listener instanceof final PlayPacketHandler play) {
            return play.enterConfiguration();
        }
        return CompletableFuture.completedFuture(false);
    }

    public CompletableFuture<@Nullable Void> saveInventoryOnDisconnect() {
        final ServerPlayer disconnecting = this.player;
        if (disconnecting == null) {
            return CompletableFuture.completedFuture(null);
        }
        this.player = null;
        if (disconnecting.containerMenu() != disconnecting.inventoryMenu()) {
            disconnecting.doCloseContainer();
        }
        disconnecting.inventoryMenu().removed();
        return CompletableFuture.runAsync(() -> {
            try {
                server.players().inventoryStorage().save(disconnecting.uuid(), disconnecting.inventory());
                server.players().enderChestStorage().save(disconnecting.uuid(), disconnecting.enderChest());
                final RespawnPoint point = disconnecting.respawnPoint();
                server.players().dataStorage().save(
                        disconnecting.uuid(),
                        new PlayerDataStorage.PlayerData(
                                disconnecting.gameMode(),
                                disconnecting.abilities(),
                                point == null ? null : point.position().toLocation(point.world()),
                                disconnecting.location(),
                                new PlayerDataStorage.Experience(
                                        disconnecting.experienceLevel(),
                                        disconnecting.experienceProgress(),
                                        disconnecting.totalExperience())));
                LOGGER.debug("Inventory + Ender Chest and data for {} saved", disconnecting.name());
            } catch (final Exception e) {
                LOGGER.error("Unable to save inventory for {}", disconnecting.name(), e);
            }
        }, VIRTUAL_THREAD_EXECUTOR);
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        LOGGER.debug("Connexion closed", cause);
        ctx.close();
    }

    public FidorialServer server() {
        return server;
    }

    public ConnectionState state() {
        return state;
    }

    public int clientProtocol() {
        return clientProtocol;
    }

    public void setClientProtocol(final int clientProtocol) {
        this.clientProtocol = clientProtocol;
    }

    /**
     * Checks whether the client arrived through a transfer from another server, that is with the
     * transfer intention ({@code 3}) in its handshake.
     *
     * @return {@code true} if this connection is a transfer
     */
    public boolean transferred() {
        return transferred;
    }

    public void setTransferred(final boolean transferred) {
        this.transferred = transferred;
    }

    public @Nullable String username() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public @Nullable String forwardedAddress() {
        return forwardedAddress;
    }

    /**
     * Gets the address this client is connecting from.
     *
     * <p>When a proxy is in front of the server, the address it forwarded takes precedence over the
     * socket's own, which would be the proxy.</p>
     *
     * @return the client address, or {@code "unknown"} if the channel has none
     */
    public String remoteAddress() {
        if (forwardedAddress != null) {
            return forwardedAddress;
        }
        final SocketAddress address = context().channel().remoteAddress();
        if (address instanceof final InetSocketAddress inet) {
            return inet.getHostString();
        }
        return address == null ? "unknown" : address.toString();
    }

    public InetAddress remoteInetAddress() {
        if (forwardedAddress != null) {
            return InetAddresses.forString(forwardedAddress);
        }

        final SocketAddress address = context().channel().remoteAddress();
        if (address instanceof final InetSocketAddress inet && inet.getAddress() != null) {
            return inet.getAddress();
        }

        throw new IllegalStateException("No IP address for this connection: " + address);
    }

    public void setForwardedAddress(final String forwardedAddress) {
        this.forwardedAddress = forwardedAddress;
    }

    public @Nullable PlayerProfile profile() {
        return profile;
    }

    public void setProfile(final PlayerProfile profile) {
        this.profile = profile;
    }

    public int viewDistance() {
        return viewDistance;
    }

    public void setViewDistance(final int viewDistance) {
        this.viewDistance = viewDistance;
    }

    public int effectiveViewDistance() {
        final int serverMax = server.config().general().sendDistance();
        final int client = viewDistance;
        return Math.clamp(client, 2, serverMax);
    }

    public int displayedSkinParts() {
        return displayedSkinParts;
    }

    public void setDisplayedSkinParts(final int displayedSkinParts) {
        this.displayedSkinParts = displayedSkinParts;
    }

    public Locale locale() {
        return locale;
    }

    public void setLocale(final Locale locale) {
        this.locale = locale;
    }

    public @Nullable ServerPlayer player() {
        return player;
    }

    public void setPlayer(final ServerPlayer player) {
        this.player = player;
    }

    public boolean isInPlayState() {
        return state == ConnectionState.PLAY || state == ConnectionState.MOCK_PLAY;
    }

    /**
     * Creates a mock connection; used by scenario tests
     * @param server the server
     * @return the mock connection
     */
    public static ClientConnection mock(final FidorialServer server) {
        final ClientConnection connection = new ClientConnection(server);
        final ConnectionState state = ConnectionState.MOCK_PLAY;
        new EmbeddedChannel(connection);
        connection.state = state;
        connection.listener = connection.createListener(state);
        return connection;
    }

    /**
     * Attaches a built player to the mock connection; used by scenario tests
     * @param player the player to attach
     */
    public void bindMockPlayer(final ServerPlayer player) {
        if (listener instanceof final PlayPacketHandler play) {
            setUsername(player.profile().name());
            play.bindPlayer(player);
            play.openChunkView((ServerWorld) player.world(), player.chunk());
            server.events().post(new PlayerJoinEvent(player));
        }
    }

    public void mockDisconnect() {
        listener.onDisconnect();
    }

    /**
     * Hands a packet to the listener as if the client had sent it; used by scenario tests
     * @param packet the packet the client would send
     */
    public void mockReceive(final ServerboundPacket packet) {
        Preconditions.checkState(state == ConnectionState.MOCK_PLAY, "Only a mock connection receives packets this way");
        packet.handle(listener);
    }

    public CompletableFuture<Boolean> teleport(final Location location) {
        if (listener instanceof final PlayPacketHandler play) {
            return play.teleport(location);
        }
        return CompletableFuture.completedFuture(false);
    }

    public CompletableFuture<Boolean> respawn(final PlayerRespawnEvent.Cause cause) {
        if (listener instanceof final PlayPacketHandler play) {
            return play.respawn(cause);
        }
        return CompletableFuture.completedFuture(false);
    }

    @Override
    public Pointers pointers() {
        return pointers.view(this);
    }

    private final Map<UUID, ResourcePackCallback> pendingResourcePacks = new ConcurrentHashMap<>();

    @Override
    public void sendResourcePacks(final ResourcePackRequest request) {
        if (state != ConnectionState.CONFIGURATION && !isInPlayState()) {
            return;
        }
        final Key pushName = isInPlayState()
                ? PlayClientboundPackets.RESOURCE_PACK_PUSH
                : ConfigurationClientboundPackets.RESOURCE_PACK_PUSH;

        final ResourcePackCallback cb = request.callback();
        request.packs().forEach(pack -> pendingResourcePacks.put(pack.id(), cb));

        for (final ResourcePackInfo pack : request.packs()) {
            send(new ClientboundResourcePackPushPacket(
                    pushName,
                    pack.id(),
                    pack.uri().toString(),
                    pack.hash(),
                    request.required(),
                    request.prompt()));
        }
    }

    @Override
    public void removeResourcePacks(final UUID id, final UUID... others) {
        popPack(id);
        for (final UUID other : others) {
            popPack(other);
        }
    }

    @Override
    public void clearResourcePacks() {
        popPack(null);
    }

    private void popPack(final @Nullable UUID id) {
        final Key popName = isInPlayState()
                ? PlayClientboundPackets.RESOURCE_PACK_POP
                : ConfigurationClientboundPackets.RESOURCE_PACK_POP;
        send(new ClientboundResourcePackPopPacket(popName, id));
    }

    public void notifyResourcePackResponse(final UUID id, final ResourcePackStatus status) {
        final ResourcePackCallback callback = status.intermediate()
                ? pendingResourcePacks.get(id)
                : pendingResourcePacks.remove(id);
        if (callback != null) {
            callback.packEventReceived(id, status, this.player != null ? this.player : this);
        }
    }

    @Override
    public void showDialog(final DialogLike dialog) {
        if (!isInPlayState() && !(this.state == ConnectionState.CONFIGURATION)) return;
        final Key packetKey = isInPlayState() ? PlayClientboundPackets.SHOW_DIALOG : ConfigurationClientboundPackets.SHOW_DIALOG;
        switch (dialog) {
            case final DialogDefinition definition -> send(ClientboundShowDialogPacket.inline(packetKey, definition));
            case final DialogReference reference -> {
                if (!server.registries().registry(RegistryKey.DIALOG).contains(reference.key())) {
                    LOGGER.warn(
                            "{} cannot be shown dialog {}: nothing is registered under that key.",
                            username, reference.key());
                    return;
                }
                send(ClientboundShowDialogPacket.reference(packetKey, reference));
            }
            default -> LOGGER.warn(
                    "{} cannot be shown a dialog of foreign type {}; build it with fr.fidorial.dialog.Dialog.",
                    username, dialog.getClass().getName());
        }
    }

    /**
     * Tells the client to leave this server and connect to another one, as {@code /transfer} does.
     *
     * <p>The client disconnects on its own once it receives the packet, then logs in to the target with
     * the transfer intention, which the target must accept.</p>
     *
     * @param host the hostname or IP address of the target server
     * @param port the port of the target server
     * @return {@code true} if the packet was sent, {@code false} if the connection is in neither the
     * configuration nor the play phase
     */
    public boolean transfer(final String host, final int port) {
        if (!isInPlayState() && this.state != ConnectionState.CONFIGURATION) {
            return false;
        }
        final Key packetKey = isInPlayState() ? PlayClientboundPackets.TRANSFER : ConfigurationClientboundPackets.TRANSFER;
        LOGGER.debug("Transferring {} to {}:{}", username == null ? "<unknown>" : username, host, port);
        send(new ClientboundTransferPacket(packetKey, host, port, Map.of()));
        return true;
    }

    /**
     * Stores a cookie on the client. The client keeps it for its current session only, through
     * transfers to other servers, and forgets it when it disconnects.
     *
     * @param key     the identifier of the cookie
     * @param payload the content of the cookie, at most {@link fr.fidorial.entity.player.Player#MAX_COOKIE_SIZE} bytes
     * @return {@code true} if the packet was sent, {@code false} if the connection is in neither the
     * configuration nor the play phase
     */
    public boolean storeCookie(final Key key, final byte[] payload) {
        if (!isInPlayState() && this.state != ConnectionState.CONFIGURATION) {
            return false;
        }
        final Key packetKey = isInPlayState() ? PlayClientboundPackets.STORE_COOKIE : ConfigurationClientboundPackets.STORE_COOKIE;
        send(new ClientboundStoreCookiePacket(packetKey, key, payload));
        return true;
    }

    /**
     * Asks the client for a cookie. Asking again for a cookie still awaited shares the answer of the
     * first request, rather than asking the client twice.
     *
     * @param key the identifier of the cookie
     * @return a future completed on the network thread with the content of the cookie, or with an empty
     * optional if the client has none; it completes exceptionally if the client disconnects first, or if
     * it cannot be asked (closed connection, or between two phases)
     */
    public CompletableFuture<Optional<byte[]>> requestCookie(final Key key) {
        if (state == ConnectionState.MOCK_PLAY) {
            return CompletableFuture.completedFuture(Optional.empty());
        }
        final CompletableFuture<Optional<byte[]>> request = new CompletableFuture<>();
        final CompletableFuture<Optional<byte[]>> pending = pendingCookies.putIfAbsent(key, request);
        if (pending != null) {
            return pending.thenApply(ClientConnection::copyCookie);
        }
        if (isActive()) {
            execute(() -> sendCookieRequest(key, request));
        } else {
            abandonCookieRequest(key, request);
        }
        return request.thenApply(ClientConnection::copyCookie);
    }

    private void sendCookieRequest(final Key key, final CompletableFuture<Optional<byte[]>> request) {
        final @Nullable Key packetKey = switch (state) {
            case LOGIN -> LoginClientboundPackets.COOKIE_REQUEST;
            case CONFIGURATION -> ConfigurationClientboundPackets.COOKIE_REQUEST;
            case PLAY -> PlayClientboundPackets.COOKIE_REQUEST;
            case HANDSHAKE, STATUS, MOCK_PLAY -> null;
        };
        final @Nullable ChannelFuture written = packetKey == null ? null : write(new ClientboundCookieRequestPacket(packetKey, key), false);
        if (written == null) {
            // Closed, or between two phases: the client would never answer.
            abandonCookieRequest(key, request);
            return;
        }
        written.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    private void abandonCookieRequest(final Key key, final CompletableFuture<Optional<byte[]>> request) {
        if (pendingCookies.remove(key, request)) {
            request.cancel(false);
        }
    }

    private void cancelCookieRequests() {
        for (final Key key : pendingCookies.keySet()) {
            final CompletableFuture<Optional<byte[]>> request = pendingCookies.remove(key);
            if (request != null) {
                request.cancel(false);
            }
        }
    }

    private static Optional<byte[]> copyCookie(final Optional<byte[]> cookie) {
        return cookie.map(byte[]::clone);
    }

    /**
     * Hands the answer of the client to whoever asked for the cookie. A cookie the server did not ask for
     * ends the connection.
     *
     * @param packet the answer of the client
     */
    public void handleCookieResponse(final ServerboundCookieResponsePacket packet) {
        final CompletableFuture<Optional<byte[]>> request = pendingCookies.remove(packet.key());
        if (request == null) {
            LOGGER.warn("{} sent the cookie {} without being asked for it",
                    username == null ? "<unknown>" : username, packet.key().asString());
            disconnect(Component.translatable("multiplayer.disconnect.unexpected_query"));
            return;
        }
        request.complete(Optional.ofNullable(packet.payload()));
    }

    /**
     * Handles a plugin message: the channels the other side announces or withdraws and the brand of the
     * client are kept here, every other message goes to the listeners of its channel once the player is in
     * the game.
     *
     * @param packet the message
     */
    public void handlePluginMessage(final ServerboundCustomPayloadPacket packet) {
        final Key channel = packet.channel();
        if (channel.equals(PluginMessenger.REGISTER)) {
            int ignored = 0;
            for (final Key announced : readChannelList(packet.data())) {
                if (pluginChannels.size() >= MAX_PLUGIN_CHANNELS && !pluginChannels.contains(announced)) {
                    ignored++;
                } else {
                    pluginChannels.add(announced);
                }
            }
            if (ignored > 0) {
                LOGGER.warn("{} announced more than {} plugin message channels: {} ignored",
                        username == null ? "<unknown>" : username, MAX_PLUGIN_CHANNELS, ignored);
            }
        } else if (channel.equals(PluginMessenger.UNREGISTER)) {
            pluginChannels.removeAll(readChannelList(packet.data()));
        } else if (channel.equals(BRAND_CHANNEL)) {
            clientBrand = readBrand(packet.data());
        } else {
            final ServerPlayer receiver = player;
            if (receiver == null) {
                LOGGER.debug("{} sent a plugin message on {} before joining the game",
                        username == null ? "<unknown>" : username, channel.asString());
                return;
            }
            server.network().messaging().dispatch(receiver, channel, packet.data());
        }
    }

    /**
     * Sends a plugin message, to the client or to the proxy reading the connection.
     *
     * @param channel the channel of the message
     * @param data    the content of the message, which must not change afterwards
     * @return {@code true} if the packet was sent, {@code false} if the connection is in neither the
     * configuration nor the play phase
     */
    public boolean sendPluginMessage(final Key channel, final byte[] data) {
        if (!isInPlayState() && this.state != ConnectionState.CONFIGURATION) {
            return false;
        }
        final Key packetKey = isInPlayState() ? PlayClientboundPackets.CUSTOM_PAYLOAD : ConfigurationClientboundPackets.CUSTOM_PAYLOAD;
        send(new ClientboundCustomPayloadPacket(packetKey, channel, data));
        return true;
    }

    /**
     * Announces channels the server listens to, or withdraws them.
     *
     * @param type     {@link PluginMessenger#REGISTER} or {@link PluginMessenger#UNREGISTER}
     * @param channels the channels; nothing is sent when there are none
     */
    public void sendChannelList(final Key type, final Collection<Key> channels) {
        if (channels.isEmpty()) {
            return;
        }
        final String list = String.join("\0", channels.stream().map(Key::asString).toList());
        sendPluginMessage(type, list.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Gets the plugin message channels the client, and the proxy in front of the server, announced.
     *
     * @return the channels, as they are now
     */
    public Set<Key> pluginChannels() {
        return Set.copyOf(pluginChannels);
    }

    /**
     * Gets the brand the client announced.
     *
     * @return the brand, or empty if the client did not announce one
     */
    public Optional<String> clientBrand() {
        return Optional.ofNullable(clientBrand);
    }

    /**
     * Reads the channels of an announcement: their names, separated by a null character. Names that are
     * not valid channels are skipped.
     */
    private List<Key> readChannelList(final byte[] data) {
        final List<Key> channels = new ArrayList<>();
        for (final String name : new String(data, StandardCharsets.UTF_8).split("\0")) {
            if (name.isEmpty()) {
                continue;
            }
            try {
                channels.add(Key.key(name));
            } catch (final InvalidKeyException invalid) {
                LOGGER.debug("{} announced an invalid plugin message channel: {}",
                        username == null ? "<unknown>" : username, name);
            }
        }
        return channels;
    }

    private @Nullable String readBrand(final byte[] data) {
        try {
            return new PacketBuffer(Unpooled.wrappedBuffer(data)).readString(MAX_BRAND_LENGTH);
        } catch (final RuntimeException malformed) {
            LOGGER.debug("{} announced a malformed brand", username == null ? "<unknown>" : username, malformed);
            return null;
        }
    }

    @Override
    public void closeDialog() {
        if (!isInPlayState() && !(this.state == ConnectionState.CONFIGURATION)) return;
        final Key packetKey = isInPlayState() ? PlayClientboundPackets.CLEAR_DIALOG : ConfigurationClientboundPackets.CLEAR_DIALOG;
        send(new ClientboundClearDialogPacket(packetKey));
    }

    private final List<Component> pendingMessages = new ArrayList<>();

    @Override
    public void sendMessage(final Component message) {
        final ServerPlayer recipient;
        synchronized (pendingMessages) {
            if (!isInPlayState() || (recipient = this.player) == null) {
                // we're too early to send a message so just collect it
                pendingMessages.add(message);
                return;
            }
        }
        recipient.sendMessage(message);
    }

    public void flushPendingMessages() {
        final List<Component> queued;
        synchronized (pendingMessages) {
            if (pendingMessages.isEmpty()) {
                return;
            }
            queued = List.copyOf(pendingMessages);
            pendingMessages.clear();
        }
        queued.forEach(this::sendMessage);
    }

    /**
     * Sets the {@link ConnectionState} for which packets shouldn't be read.
     * This gets reset on each {@link setState} call.
     *
     * @param state the state to reject packets
     */
    public void setRejectedState(final ConnectionState state) {
        this.rejectedState = state;
    }

    /**
     * Adds a packet to the list of packets that should be exempt from rejection.
     * @param packet the packet to add
     */
    public void addExemptPacket(final Class<? extends ServerboundPacket> packet) {
        if (this.exemptPackets == null) {
            this.exemptPackets = new ArrayList<>();
        }
        this.exemptPackets.add(packet);
    }

    public List<Key> postEffects() {
        return postEffects;
    }

    public void updatePostEffects(final SequencedCollection<Key> effects) {
        this.postEffects = List.copyOf(effects);
        this.sendPostEffects(effects);
    }

    public void sendPostEffects(final SequencedCollection<Key> postEffects) {
        if (!isInPlayState() && !(this.state == ConnectionState.CONFIGURATION)) return;
        final Key packetKey = isInPlayState() ? PlayClientboundPackets.POST_EFFECTS : ConfigurationClientboundPackets.POST_EFFECTS;
        send(new ClientboundPostEffectsPacket(packetKey, postEffects));
    }

    public void sendCommands(final CommandSource source) {
        if (!isInPlayState() && !(this.state == ConnectionState.CONFIGURATION)) return;
        final Key packetKey = isInPlayState() ? PlayClientboundPackets.COMMANDS : ConfigurationClientboundPackets.COMMANDS;
        send(server.commands().manager().createCommandsPacket(packetKey, source));
    }
}
