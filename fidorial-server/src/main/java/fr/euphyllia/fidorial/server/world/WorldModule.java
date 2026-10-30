package fr.euphyllia.fidorial.server.world;

import fr.euphyllia.fidorial.server.configuration.ServerConfiguration;
import fr.euphyllia.fidorial.server.configuration.WorldConfigurationContainer;
import fr.euphyllia.fidorial.server.debug.DebugModule;
import fr.euphyllia.fidorial.server.entity.EntityModule;
import fr.euphyllia.fidorial.server.entity.player.PlayerList;
import fr.euphyllia.fidorial.server.entity.player.PlayerModule;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.lifecycle.Quietly;
import fr.euphyllia.fidorial.server.lifecycle.ServerModule;
import fr.euphyllia.fidorial.server.lifecycle.ServiceRegistrar;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundBlockUpdatePacket;
import fr.euphyllia.fidorial.server.registry.RegistryModule;
import fr.euphyllia.fidorial.server.schedulers.DayNightThread;
import fr.euphyllia.fidorial.server.schedulers.LightUpdateDispatcher;
import fr.euphyllia.fidorial.server.schedulers.WorkerModule;
import fr.euphyllia.fidorial.server.world.fluid.FluidEngine;
import fr.euphyllia.fidorial.server.world.gamerule.FidorialGameRules;
import fr.euphyllia.fidorial.server.world.structure.StructureService;
import fr.euphyllia.fidorial.server.world.structure.gen.RegistryBlockValidator;
import fr.euphyllia.fidorial.server.world.weather.WeatherEngine;
import fr.fidorial.event.EventBus;
import fr.fidorial.gamerule.GameRuleDefinition;
import fr.fidorial.gamerule.GameRules;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.keys.GameRuleKeys;
import fr.fidorial.service.ServiceRegistry;
import fr.fidorial.world.World;
import fr.fidorial.world.WorldManager;
import fr.fidorial.world.WorldSpec;
import fr.fidorial.world.dimension.types.VanillaDimensionTypes;
import fr.fidorial.world.fluid.FluidManager;
import fr.fidorial.world.structure.StructureManager;
import fr.fidorial.world.weather.WeatherManager;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The worlds and everything that runs on them: weather, day/night cycle, light, fluids, block
 * edits, structures, game rules and boss bars.
 *
 * <p>This is the server's implementation of the {@link WorldManager} API.</p>
 */
public final class WorldModule implements ServerModule, WorldManager {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(WorldModule.class);

    private final ServiceRegistry services;
    private final RegistryModule registries;
    private final PlayerList online;
    private final ServerWorldManager manager;
    private final StructureService structures;
    private final WeatherEngine weather;
    private final FluidEngine fluids;
    private final FidorialGameRules gameRules;
    private final DayNightThread dayNight;
    private final BossBarRegistry bossBars;
    private final ChunkNetworkSerializer chunkSerializer;
    private final LightUpdateDispatcher light;
    private final BlockEditService blockEdits;
    private final AtomicBoolean unloadingChunks = new AtomicBoolean(false);

    public WorldModule(
            final ServerConfiguration config, final EventBus events, final ServiceRegistry services,
            final RegistryModule registries, final WorkerModule scheduler, final PlayerModule players,
            final EntityModule entities, final DebugModule debug) throws IOException {
        this.services = services;
        this.registries = registries;
        this.online = players.connections();

        this.manager = ServerWorldManager.openOrCreate(
                config.worlds().path(), registries.blockStates(), scheduler.regionizer(), config.worlds().levelSeed(),
                config.worlds().initialEnabledPacks(), WorldConfigurationContainer.load(ServerConfiguration.DIRECTORY));
        this.structures = new StructureService(
                config.worlds().path().resolve("datapacks"), new RegistryBlockValidator(registries.blocks()));
        this.weather = new WeatherEngine(manager);
        this.fluids = new FluidEngine(manager, scheduler.regionizer(), registries.blockStates(), online::broadcast);
        this.gameRules = new FidorialGameRules(manager.levelData(), events);
        this.dayNight = new DayNightThread(manager, registries.registry(RegistryKey.WORLD_CLOCK));
        this.bossBars = new BossBarRegistry(manager.levelData(), online::players);
        this.chunkSerializer = new ChunkNetworkSerializer(registries.blockStates(), registries.registry(RegistryKey.BIOME));
        this.light = new LightUpdateDispatcher(
                config.threading().lightWorkers(), online::broadcast, chunkSerializer, manager::world);
        this.blockEdits = new BlockEditService(
                registries.blockStates(),
                (pos, stateId) -> online.broadcast(new ClientboundBlockUpdatePacket(pos, stateId)),
                fluids::notifyBlockChanged,
                light::queueBlockChange);

        // Wiring that cannot go through constructors (circular references). Done before plugins
        // load, so that a world created by a plugin is complete.
        manager.setStructureService(structures);
        manager.setChunkLoader(scheduler.chunkWorker());
        manager.setLightDispatcher(light);
        manager.setEntityBridge(entities.ids()::allocate, entities.spawnBridge());
        manager.setDefaultWorld(config.worlds().defaultWorld());
        manager.setDefaultGenerator(new ServiceBackedChunkGenerator(
                services, FlatChunkGenerator.cobblestone(VanillaDimensionTypes.OVERWORLD)));
        fluids.setLightHook(light::queueBlockChange);
        fluids.setDebugEvents(debug.gameEvents());
        blockEdits.setDebugEvents(debug.gameEvents());
        gameRules.addListener(this::syncGameRule);
    }

    @Override
    public void registerServices(final ServiceRegistrar services) {
        services.provide(FluidManager.class, fluids);
        services.provide(WeatherManager.class, weather);
        services.provide(GameRules.class, gameRules); // Todo : Currently, plugins cannot implement their own system.
        services.provide(BlockEditService.class, blockEdits); // Todo : Currently, plugins cannot implement their own system.
        services.provide(BossBarRegistry.class, bossBars); // Todo : Currently, plugins cannot implement their own system.
        services.provide(StructureManager.class, structures);
    }

    @Override
    public void start() {
        structures.load(registries.datapacks());
        manager.overworld();
        weather.start();
        dayNight.start();
        bossBars.loadFromLevelData();
    }

    @Override
    public void autoSave() throws IOException {
        if (!unloadingChunks.compareAndSet(false, true)) {
            LOGGER.debug("Skipping world auto-save, the previous chunk unload is still running");
            return;
        }
        try {
            manager.saveDirty();
            manager.unloadUnusedChunks().whenComplete((count, failure) -> {
                if (failure != null) {
                    LOGGER.error("An error occurred while unloading chunks during the automatic save:", failure);
                } else if (count > 0) {
                    LOGGER.debug("{} unloaded chunks", count);
                }
                unloadingChunks.set(false);
            });
        } catch (final IOException | RuntimeException e) {
            unloadingChunks.set(false);
            throw e;
        }
    }

    @Override
    public void stop() {
        Quietly.run("bossbars", bossBars::close);
        Quietly.run("day/night cycle", dayNight::close);
        Quietly.run("light engine", light::shutdown);
        Quietly.run("structures", structures::shutdown);
    }

    @Override
    public void close() {
        Quietly.run("weather", weather::close);
        Quietly.run("worlds", manager::close);
    }

    @Override
    public Collection<ServerWorld> all() {
        return manager.worlds();
    }

    @Override
    public World create(final WorldSpec spec) {
        return manager.createWorld(spec.key(), spec.seed(), spec.generator().orElse(null), false);
    }

    /**
     * Creates a world without sending the players back to the configuration phase.
     */
    public World createWorldSilent(final WorldSpec spec) {
        return manager.createWorld(spec.key(), spec.seed(), spec.generator().orElse(null), true);
    }

    @Override
    public boolean unload(final Key key, final boolean save) {
        try {
            return manager.unloadWorld(key, save) != null;
        } catch (final IOException e) {
            LOGGER.error("Saving world {} before unloading failed", key, e);
            return false;
        }
    }

    @Override
    public Optional<Key> defaultWorld() {
        return manager.defaultWorldKey();
    }

    @Override
    public void defaultWorld(final @Nullable Key key) {
        manager.setDefaultWorld(key);
    }

    private void syncGameRule(final @Nullable ServerWorld world, final GameRuleDefinition rule) {
        final Key key = rule.key().key();
        if (key.equals(GameRuleKeys.ADVANCE_TIME.key())) {
            if (world == null) {
                dayNight.resyncAll();
            } else {
                dayNight.resync(world);
            }
        } else if (key.equals(GameRuleKeys.REDUCED_DEBUG_INFO.key()) || key.equals(GameRuleKeys.IMMEDIATE_RESPAWN.key())) {
            for (final ServerPlayer player : online.players()) {
                if (!player.connection().isInPlayState()
                        || !(player.world() instanceof final ServerWorld playerWorld)
                        || (world != null && !playerWorld.equals(world))) {
                    continue;
                }
                gameRules.syncTo(playerWorld, player.entityId(), player.connection()::send);
            }
        }
        LOGGER.debug("Game rule {} changed in {}", key.asString(), world == null ? "every world" : world.key());
    }

    public ServerWorldManager manager() {
        return manager;
    }

    @Override
    public StructureService structures() {
        return structures;
    }

    public WeatherEngine weather() {
        return weather;
    }

    public FluidEngine fluids() {
        return fluids;
    }

    @Override
    public FidorialGameRules gameRules() {
        return gameRules;
    }

    public DayNightThread dayNight() {
        return dayNight;
    }

    public BossBarRegistry bossBars() {
        return services.find(BossBarRegistry.class).orElse(bossBars);
    }

    public ChunkNetworkSerializer chunkSerializer() {
        return chunkSerializer;
    }

    public LightUpdateDispatcher light() {
        return light;
    }

    public BlockEditService blockEdits() {
        return blockEdits;
    }
}
