package fr.euphyllia.fidorial.server.entity.player;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.configuration.ServerConfiguration;
import fr.euphyllia.fidorial.server.entity.player.profile.FidorialOfflinePlayerManager;
import fr.euphyllia.fidorial.server.entity.player.storage.NbtPlayerDataStorage;
import fr.euphyllia.fidorial.server.entity.player.storage.NbtPlayerEnderChestStorage;
import fr.euphyllia.fidorial.server.entity.player.storage.NbtPlayerInventoryStorage;
import fr.euphyllia.fidorial.server.inventory.BlockViewerTracker;
import fr.euphyllia.fidorial.server.lifecycle.ServerModule;
import fr.euphyllia.fidorial.server.lifecycle.ServiceRegistrar;
import fr.euphyllia.fidorial.server.moderation.FidorialBanManager;
import fr.euphyllia.fidorial.server.moderation.FidorialWhitelist;
import fr.euphyllia.fidorial.server.moderation.ModerationModule;
import fr.euphyllia.fidorial.server.network.ClientConnection;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.fidorial.entity.player.OfflinePlayerManager;
import fr.fidorial.entity.player.PlayerManager;
import fr.fidorial.service.ServiceRegistry;
import fr.fidorial.storage.player.PlayerDataStorage;
import fr.fidorial.storage.player.PlayerEnderChestStorage;
import fr.fidorial.storage.player.PlayerInventoryStorage;
import fr.fidorial.world.World;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

/**
 * Players: who is online, known profiles, who may join and where their data is stored.
 *
 * <p>This is the server's implementation of the {@link PlayerManager} API.</p>
 */
public final class PlayerModule implements ServerModule, PlayerManager {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(PlayerModule.class);
    private static final Duration PROFILE_CACHE_TTL = Duration.ofDays(30);
    private static final int PROFILE_CACHE_MAX_ENTRIES = 65_536;

    private final ServerConfiguration config;
    private final ServiceRegistry services;
    private final ModerationModule moderation;
    private final PlayerList connections = new PlayerList();
    private final BlockViewerTracker blockViewers = new BlockViewerTracker();
    private final NbtPlayerInventoryStorage defaultInventoryStorage;
    private final NbtPlayerDataStorage defaultDataStorage;
    private final NbtPlayerEnderChestStorage defaultEnderChestStorage;
    private final FidorialOfflinePlayerManager offlinePlayers;

    public PlayerModule(
            final FidorialServer server, final ServerConfiguration config, final ServiceRegistry services,
            final ModerationModule moderation) {
        this.config = config;
        this.services = services;
        this.moderation = moderation;
        final Path folder = config.worlds().path().resolve("players");
        this.defaultInventoryStorage = new NbtPlayerInventoryStorage(folder, false);
        this.defaultDataStorage = new NbtPlayerDataStorage(folder, false);
        this.defaultEnderChestStorage = new NbtPlayerEnderChestStorage(folder, false);
        this.offlinePlayers = new FidorialOfflinePlayerManager(
                server, folder.resolve("profiles.fop"), PROFILE_CACHE_TTL, PROFILE_CACHE_MAX_ENTRIES,
                config.network().onlineMode());
    }

    @Override
    public void registerServices(final ServiceRegistrar services) {
        services.provide(PlayerInventoryStorage.class, defaultInventoryStorage);
        services.provide(PlayerDataStorage.class, defaultDataStorage);
        services.provide(PlayerEnderChestStorage.class, defaultEnderChestStorage);
        services.provide(OfflinePlayerManager.class, offlinePlayers);
    }

    @Override
    public void load() {
        try {
            offlinePlayers.load();
        } catch (final IOException e) {
            LOGGER.error("Profile cache unreadable, starting with an empty one", e);
        }
    }

    @Override
    public void autoSave() {
        offlinePlayers.maintain();
    }

    @Override
    public void close() throws IOException {
        offlinePlayers.close();
    }

    @Override
    public List<ServerPlayer> online() {
        return connections.players();
    }

    @Override
    public int count() {
        return connections.connectionCount();
    }

    @Override
    public FidorialOfflinePlayerManager offline() {
        return offlinePlayers;
    }

    @Override
    public FidorialBanManager bans() {
        return moderation.bans();
    }

    @Override
    public FidorialWhitelist whitelist() {
        return moderation.whitelist();
    }

    /**
     * {@return the connections, to broadcast packets or react to joins and leaves}
     */
    public PlayerList connections() {
        return connections;
    }

    /**
     * Called by the network layer when a connection gets a player.
     *
     * @param connection the connection
     */
    public void connect(final ClientConnection connection) {
        connections.add(connection);
    }

    /**
     * Called by the network layer when a connection is closed.
     *
     * @param connection the connection
     */
    public void disconnect(final ClientConnection connection) {
        final ServerPlayer player = connection.player();
        if (player != null) {
            player.clearActiveBossBars();
        }
        connections.remove(connection);
    }

    /**
     * Sends a packet to every connection.
     *
     * @param packet the packet
     */
    public void broadcast(final ClientboundPacket packet) {
        connections.broadcast(packet);
    }

    /**
     * Sends a packet to the players of {@code world} that can see the given point.
     */
    public void broadcastNear(
            final World world, final double x, final double y, final double z, final ClientboundPacket packet) {
        connections.broadcastNear(world, x, y, z, config.general().sendDistance() * 16.0 + 16.0, packet);
    }

    public PlayerInventoryStorage inventoryStorage() {
        return services.find(PlayerInventoryStorage.class).orElse(defaultInventoryStorage);
    }

    public PlayerDataStorage dataStorage() {
        return services.find(PlayerDataStorage.class).orElse(defaultDataStorage);
    }

    public PlayerEnderChestStorage enderChestStorage() {
        return services.find(PlayerEnderChestStorage.class).orElse(defaultEnderChestStorage);
    }

    public BlockViewerTracker blockViewers() {
        return blockViewers;
    }
}
