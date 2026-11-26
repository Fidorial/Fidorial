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
import fr.euphyllia.fidorial.server.registry.ServerRegistry;
import fr.euphyllia.fidorial.server.schedulers.DayNightThread;
import fr.euphyllia.fidorial.server.schedulers.LightUpdateDispatcher;
import fr.euphyllia.fidorial.server.schedulers.ThreadedRegionRegionizer;
import fr.euphyllia.fidorial.server.schedulers.WorkerModule;
import fr.euphyllia.fidorial.server.schedulers.tick.ServerTickRates;
import fr.euphyllia.fidorial.server.world.block.chest.TrappedChestBlock;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.farming.FidorialFarmingEngine;
import fr.euphyllia.fidorial.server.world.fluid.FluidEngine;
import fr.euphyllia.fidorial.server.world.gamerule.FidorialGameRules;
import fr.euphyllia.fidorial.server.world.generation.NoiseChunkGenerator;
import fr.euphyllia.fidorial.server.world.generation.NoiseTerrainService;
import fr.euphyllia.fidorial.server.world.generation.block.BlockLookup;
import fr.euphyllia.fidorial.server.world.generation.data.WorldgenData;
import fr.euphyllia.fidorial.server.world.redstone.FidorialRedstoneEngine;
import fr.euphyllia.fidorial.server.world.redstone.MovingPistons;
import fr.euphyllia.fidorial.server.world.storage.Dimension;
import fr.euphyllia.fidorial.server.world.storage.LevelData;
import fr.euphyllia.fidorial.server.world.structure.StructureService;
import fr.euphyllia.fidorial.server.world.structure.gen.RegistryBlockValidator;
import fr.euphyllia.fidorial.server.world.tick.ScheduledTickEngine;
import fr.euphyllia.fidorial.server.world.weather.WeatherEngine;
import fr.fidorial.event.EventBus;
import fr.fidorial.gamerule.GameRuleDefinition;
import fr.fidorial.gamerule.GameRules;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.GameRule;
import fr.fidorial.registry.keys.DimensionTypeKeys;
import fr.fidorial.registry.keys.GameRuleKeys;
import fr.fidorial.service.ServiceRegistry;
import fr.fidorial.world.World;
import fr.fidorial.world.WorldManager;
import fr.fidorial.world.WorldSpec;
import fr.fidorial.world.biome.BiomeDefinition;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.dimension.DimensionTypeDefinition;
import fr.fidorial.world.dimension.types.VanillaDimensionTypes;
import fr.fidorial.world.farming.FarmingBehaviour;
import fr.fidorial.world.farming.FarmingEngine;
import fr.fidorial.world.farming.FarmingWorld;
import fr.fidorial.world.fluid.FluidManager;
import fr.fidorial.world.generation.NoiseTerrains;
import fr.fidorial.world.redstone.RedstoneBehaviour;
import fr.fidorial.world.redstone.RedstoneEngine;
import fr.fidorial.world.redstone.RedstoneWorld;
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
 * The worlds and everything that runs on them: weather, day/night cycle, light, fluids, redstone,
 * farming, block edits, structures, game rules and boss bars.
 *
 * <p>This is the server's implementation of the {@link WorldManager} API.</p>
 */
public final class WorldModule implements ServerModule, WorldManager {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(WorldModule.class);

    private static final Key FLAT_GENERATOR = Key.key("flat");
    private static final Key NOISE_GENERATOR = Key.key("noise");

    private final ServiceRegistry services;
    private final RegistryModule registries;
    private final PlayerList online;
    private final ServerWorldManager manager;
    private final StructureService structures;
    private final NoiseTerrainService noiseTerrains;
    private final WeatherEngine weather;
    private final FluidEngine fluids;
    private final ScheduledTickEngine ticks;
    private final FidorialGameRules gameRules;
    private final DayNightThread dayNight;
    private final ServerTickRates tickRates;
    private final BossBarRegistry bossBars;
    private final ChunkNetworkSerializer chunkSerializer;
    private final LightUpdateDispatcher light;
    private final BlockEditService blockEdits;
    private final ThreadedRegionRegionizer regionizer;
    private final FidorialRedstoneEngine redstone;
    private final FidorialFarmingEngine farming;
    private final MovingPistons movingPistons;
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
        this.noiseTerrains = newNoiseTerrains(registries, manager, structures);
        this.weather = new WeatherEngine(manager);
        this.fluids = new FluidEngine(scheduler.regionizer(), registries.blockStates(), world -> redstone().world(world));
        this.ticks = new ScheduledTickEngine(manager, scheduler.regionizer());
        this.gameRules = new FidorialGameRules(manager.levelData(), events);
        this.dayNight = new DayNightThread(manager, registries.registry(RegistryKey.WORLD_CLOCK));
        this.tickRates = new ServerTickRates(scheduler.regionizer(), dayNight, weather);
        this.bossBars = new BossBarRegistry(manager.levelData(), online::players);
        this.chunkSerializer = new ChunkNetworkSerializer(
                registries.blockStates(), registries.registry(RegistryKey.BIOME),
                registries.registry(RegistryKey.BLOCK_ENTITY_TYPE)
        );
        this.light = new LightUpdateDispatcher(
                config.threading().lightWorkers(), chunkSerializer, manager::world);
        this.blockEdits = new BlockEditService(
                registries.blockStates(),
                (world, pos, stateId) -> world.sendToChunkViewers(
                        pos.chunk().key(), new ClientboundBlockUpdatePacket(pos, stateId)),
                light::queueBlockChange);
        this.regionizer = scheduler.regionizer();
        this.redstone = new FidorialRedstoneEngine(
                manager, regionizer, registries.blockStates(), registries.redstone(), blockEdits);
        this.movingPistons = new MovingPistons(manager, regionizer, registries.redstone(), world -> redstone().world(world));
        this.farming = new FidorialFarmingEngine(
                manager, regionizer, registries.blockStates(), registries.farming(), blockEdits,
                registries.serverRegistries(), online::players);

        // Wiring that cannot go through constructors (circular references). Done before plugins
        // load, so that a world created by a plugin is complete.
        manager.setStructureService(structures);
        manager.setChunkLoader(scheduler.chunkWorker());
        manager.setLightDispatcher(light);
        manager.setChunkReadListener((world, column) -> {
            movingPistons.resume(world, column);
            ticks.resume(world, column);
        });
        manager.setTicks(ticks);
        ticks.setBlockRunner(this::runBlockTick);
        ticks.setFluidRunner(fluids::tick);
        manager.setEntityBridge(entities.ids()::allocate, entities.spawnBridge());
        manager.setDefaultWorld(config.worlds().defaultWorld());
        manager.setDefaultGenerator(new ServiceBackedChunkGenerator(services, overworldGenerator()));
        fluids.setDebugEvents(debug.gameEvents());
        blockEdits.setDebugEvents(debug.gameEvents());
        blockEdits.setRedstoneNotifier(this::redstoneBlockChanged);
        blockEdits.setChangeListener(this::farmingBlockChanged);
        players.blockViewers().setListener(this::viewersChanged);
        gameRules.addListener(this::syncGameRule);
    }

    private static NoiseTerrainService newNoiseTerrains(final RegistryModule registries, final ServerWorldManager manager,
                                                        final StructureService structures) {
        final ServerRegistry<BlockType> blocks = registries.registry(RegistryKey.BLOCK);
        final ServerRegistry<BiomeDefinition> biomes = registries.registry(RegistryKey.BIOME);
        final ServerRegistry<DimensionTypeDefinition> dimensionTypes = registries.registry(RegistryKey.DIMENSION_TYPE);
        final NoiseChunkGenerator.Environment environment = new NoiseChunkGenerator.Environment(
                WorldgenData.bundled(), new BlockLookup(blocks::isTagged),
                id -> biomes.data(TypedKey.withRegistry(RegistryKey.BIOME, id)),
                key -> structures.registry().templateIfProvided(key));
        return new NoiseTerrainService(environment, key -> {
            final TypedKey<DimensionTypeDefinition> typeKey = TypedKey.withRegistry(RegistryKey.DIMENSION_TYPE, key);
            return dimensionTypes.contains(typeKey) ? dimensionTypes.get(typeKey) : VanillaDimensionTypes.OVERWORLD;
        }, () -> manager.levelData().seed);
    }

    /**
     * {@return the generator of the overworld when no plugin provides one: the noise settings saved with the world,
     * the default ones for a new world, or the flat cobblestone of a world saved flat}
     */
    private ChunkGenerator overworldGenerator() {
        final LevelData saved = manager.levelData();
        final Key overworld = Dimension.OVERWORLD.id();
        final String savedType = saved.savedGeneratorType(overworld);
        final Key type = Key.parseable(savedType) ? Key.key(savedType) : null;
        if (FLAT_GENERATOR.equals(type)) {
            return FlatChunkGenerator.cobblestone(VanillaDimensionTypes.OVERWORLD);
        }
        Key settings = NoiseTerrainService.OVERWORLD;
        Key preset = NoiseTerrainService.OVERWORLD;
        final Key savedSettings = saved.savedNoiseSettings(overworld);
        final Key savedPreset = saved.savedBiomePreset(overworld);
        if (NOISE_GENERATOR.equals(type) && savedSettings != null && savedPreset != null
                && noiseTerrains.knows(savedSettings, savedPreset)) {
            settings = savedSettings;
            preset = savedPreset;
        }
        return noiseTerrains.newGenerator(settings, preset, saved.seed);
    }

    @Override
    public void registerServices(final ServiceRegistrar services) {
        services.provide(NoiseTerrains.class, noiseTerrains);
        services.provide(FluidManager.class, fluids);
        services.provide(WeatherManager.class, weather);
        services.provide(GameRules.class, gameRules); // Todo : Currently, plugins cannot implement their own system.
        services.provide(BlockEditService.class, blockEdits); // Todo : Currently, plugins cannot implement their own system.
        services.provide(BossBarRegistry.class, bossBars); // Todo : Currently, plugins cannot implement their own system.
        services.provide(StructureManager.class, structures);
        services.provide(RedstoneEngine.class, redstone);
        services.provide(FarmingEngine.class, farming);
    }

    @Override
    public void start() {
        structures.load(registries.datapacks());
        final ServerWorld overworld = manager.overworld();
        // vanilla freezes debug worlds on load
        if (overworld.generator.describeForSave() instanceof ChunkGeneratorConfig.Debug) {
            tickRates.setFrozen(true);
        }
        overworld.spawnLocation();
        manager.theEnd(registries.registry(RegistryKey.DIMENSION_TYPE).get(DimensionTypeKeys.THE_END));
        weather.start();
        dayNight.start();
        bossBars.loadFromLevelData();
        // the order of a game tick: block ticks, fluid ticks, then block events
        regionizer.registerTickHandler(ticks);
        regionizer.registerTickHandler(redstone.tickHandler());
        // after the scheduled ticks and the block events, where vanilla ticks its block entities
        regionizer.registerTickHandler(movingPistons);
        // once every block settled for this game tick: plants grow on a world at rest
        regionizer.registerTickHandler(this::tickFarming);
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
                } else if (count != null && count > 0) {
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
        final TypedKey<GameRule> key = rule.key();
        if (key.equals(GameRuleKeys.ADVANCE_TIME)) {
            if (world == null) {
                dayNight.resyncAll();
            } else {
                dayNight.resync(world);
            }
        } else if (key.equals(GameRuleKeys.REDUCED_DEBUG_INFO) || key.equals(GameRuleKeys.IMMEDIATE_RESPAWN)) {
            for (final ServerPlayer player : online.players()) {
                if (!player.connection().isInPlayState()
                        || !(player.world() instanceof final ServerWorld playerWorld)
                        || (world != null && !playerWorld.equals(world))) {
                    continue;
                }
                gameRules.syncTo(playerWorld, player.entityId(), player.connection()::send);
            }
        }
        LOGGER.debug("Game rule {} changed in {}", key, world == null ? "every world" : world.key());
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

    /**
     * {@return the ticks scheduled in every world}
     */
    public ScheduledTickEngine ticks() {
        return ticks;
    }

    @Override
    public FidorialGameRules gameRules() {
        return gameRules;
    }

    public ServerTickRates tickRates() {
        return tickRates;
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

    /**
     * {@return the blocks pistons are moving}
     */
    public MovingPistons movingPistons() {
        return movingPistons;
    }

    /**
     * {@return the redstone engine in effect: the one a plugin provides, the server's otherwise}
     */
    public RedstoneEngine redstone() {
        return services.find(RedstoneEngine.class).orElse(redstone);
    }

    /**
     * {@return the farming engine in effect: the one a plugin provides, the server's otherwise}
     */
    public FarmingEngine farming() {
        return services.find(FarmingEngine.class).orElse(farming);
    }

    /**
     * {@return the farming engine of the server, whether or not a plugin provides another one}
     */
    public FidorialFarmingEngine builtinFarming() {
        return farming;
    }

    private void tickFarming(final Key world, final int sectionX, final int sectionZ, final long currentTick) {
        if (!ThreadedRegionRegionizer.isGameTick()) {
            return; // frozen: plants wait
        }
        final FarmingEngine engine = farming();
        if (engine != farming) {
            farming.releaseRegion(world, sectionX, sectionZ);
        }
        engine.tick(world, sectionX, sectionZ, currentTick);
    }

    /**
     * Runs a due block tick: the tick of the redstone behaviour of the block, in the redstone of
     * the engine in effect, then that of its farming behaviour, if it has either.
     */
    private void runBlockTick(final ServerWorld world, final BlockPosition pos, final TypedKey<BlockType> block) {
        final RedstoneWorld redstoneWorld = redstone().world(world);
        final BlockData state = redstoneWorld.blockAt(pos);
        if (state == null || !state.key().equals(block)) {
            return; // unloaded, or replaced since: the tick is dropped
        }
        final RedstoneBehaviour behaviour = registries.redstone().behaviourOrNull(block);
        if (behaviour != null) {
            behaviour.tick(redstoneWorld, pos, state);
        }
        final FarmingBehaviour plant = registries.farming().behaviourOrNull(block);
        if (plant != null) {
            final FarmingWorld farmingWorld = farming().world(world);
            final BlockData still = farmingWorld.blockAt(pos);
            if (still != null && still.key().equals(block)) {
                plant.tick(farmingWorld, pos, still);
            }
        }
    }

    private void farmingBlockChanged(final ServerWorld world, final BlockPosition pos, final BlockState previous,
                                     final BlockState current) {
        final BlockData before = registries.blockStates().resolve(previous);
        final BlockData after = registries.blockStates().resolve(current);
        if (before != null && after != null) {
            farming().world(world).blockChanged(pos, before, after);
        }
    }

    private void redstoneBlockChanged(final ServerWorld world, final BlockPosition pos, final BlockState previous,
                                      final BlockState current) {
        final BlockData before = registries.blockStates().resolve(previous);
        final BlockData after = registries.blockStates().resolve(current);
        if (before != null && after != null) {
            redstone().world(world).blockChanged(pos, before, after);
        }
    }

    private void viewersChanged(final Key worldKey, final BlockPosition pos, final int previous, final int current) {
        final ServerWorld world = manager.world(worldKey);
        if (world == null) {
            return;
        }
        regionizer.execute(worldKey, pos.chunk(), () -> {
            final RedstoneWorld redstoneWorld = redstone().world(world);
            final BlockData state = redstoneWorld.blockAt(pos);
            if (state != null && registries.redstone().behaviourOrNull(state.key()) instanceof final TrappedChestBlock chest) {
                chest.viewersChanged(redstoneWorld, pos, previous, current);
            }
        });
    }
}
