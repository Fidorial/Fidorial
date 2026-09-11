package fr.euphyllia.fidorial.server.world;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.AbstractEntity;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.schedulers.LightUpdateDispatcher;
import fr.euphyllia.fidorial.server.schedulers.ThreadedRegionRegionizer;
import fr.euphyllia.fidorial.server.util.annotations.NeedsToBeRevisited;
import fr.euphyllia.fidorial.server.world.chunk.AnvilChunkSerializer;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.entity.AnvilEntitySerializer;
import fr.euphyllia.fidorial.server.world.storage.ChunkStorage;
import fr.euphyllia.fidorial.server.world.storage.Dimension;
import fr.euphyllia.fidorial.server.world.storage.EntityRegionStorage;
import fr.euphyllia.fidorial.server.world.storage.LevelData;
import fr.euphyllia.fidorial.server.world.storage.WorldPaths;
import fr.euphyllia.fidorial.server.world.structure.StructureService;
import fr.euphyllia.fidorial.server.world.time.WorldTimeEngine;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.world.dimension.types.VanillaDimensionTypes;
import fr.fidorial.world.entity.EntitySpawnBridge;
import fr.fidorial.world.generation.WorldGenerator;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.IntSupplier;

public final class WorldManager implements AutoCloseable {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(WorldManager.class);

    private final WorldPaths paths;
    private final LevelData levelData;
    private final ChunkStorage storage;
    private final EntityRegionStorage entityStorage;
    private final AnvilEntitySerializer entitySerializer;
    private final ThreadedRegionRegionizer scheduler;
    private final Map<Key, ServerWorld> worlds = new ConcurrentHashMap<>();
    private volatile @Nullable LightUpdateDispatcher lightDispatcher;
    private final BlockStateRegistry blockStates;
    private volatile @Nullable ChunkGenerator defaultGenerator;
    private volatile @Nullable AsyncChunkLoader chunkLoader;
    private volatile @Nullable IntSupplier entityIdSupplier;
    private volatile @Nullable EntitySpawnBridge entityBridge;
    private volatile @Nullable StructureService structures;

    private WorldManager(
            final WorldPaths paths,
            final LevelData levelData,
            final ChunkStorage storage,
            final EntityRegionStorage entityStorage,
            final AnvilEntitySerializer entitySerializer,
            final BlockStateRegistry blockStates,
            final ThreadedRegionRegionizer scheduler
    ) {
        this.paths = paths;
        this.levelData = levelData;
        this.storage = storage;
        this.entityStorage = entityStorage;
        this.entitySerializer = entitySerializer;
        this.blockStates = blockStates;
        this.scheduler = scheduler;
    }

    public static WorldManager openOrCreate(final Path worldRoot, final BlockStateRegistry blockStates, final ThreadedRegionRegionizer scheduler) throws IOException {
        return openOrCreate(worldRoot, blockStates, scheduler, null);
    }

    public static WorldManager openOrCreate(final Path worldRoot, final BlockStateRegistry blockStates, final ThreadedRegionRegionizer scheduler,
                                            final @Nullable Long newWorldSeed) throws IOException {
        final WorldPaths paths = new WorldPaths(worldRoot, WorldPaths.Layout.MODERN);

        final LevelData levelData;
        if (paths.levelDat().toFile().isFile()) {
            levelData = LevelData.read(paths.dataDir(), paths.levelDat());
            LOGGER.info("Monde chargé : {} (DataVersion {})", levelData.levelName, levelData.dataVersion);
        } else {
            levelData = new LevelData();
            levelData.seed = newWorldSeed != null ? newWorldSeed : new SecureRandom().nextLong();
            levelData.write(paths.dataDir(), paths.levelDat());
            LOGGER.info("Nouveau monde créé dans {}", worldRoot);
        }

        final AnvilChunkSerializer serializer = new AnvilChunkSerializer();
        final ChunkStorage storage = new ChunkStorage(paths, serializer, BlockState.of(BlockTypeKeys.AIR.key()), Key.key("plains"));

        final EntityRegionStorage entityStorage = new EntityRegionStorage(paths);
        final AnvilEntitySerializer entitySerializer = new AnvilEntitySerializer();

        return new WorldManager(paths, levelData, storage, entityStorage, entitySerializer, blockStates, scheduler);
    }

    public ServerWorld registerDimension(final Dimension dim, final ChunkGenerator generator) {
        return registerDimension(dim, generator, levelData.seed);
    }

    public ServerWorld registerDimension(final Dimension dim, final ChunkGenerator generator, final long seed) {
        final ServerWorld world = worlds.computeIfAbsent(dim.id(), _ -> newWorld(dim, generator, seed));
        restoreForcedChunks(world);
        return world;
    }

    private ServerWorld newWorld(final Dimension dim, final ChunkGenerator generator, final long seed) {
        final StructureService structureService = structures;
        final ChunkGenerator effective = structureService == null
                ? generator
                : structureService.wrap(dim.id(), generator, seed);
        final ServerWorld world = new ServerWorld(dim, storage, entityStorage, entitySerializer, effective, blockStates, scheduler);
        if (chunkLoader != null) {
            world.setChunkLoader(chunkLoader);
        }
        if (entityIdSupplier != null && entityBridge != null) {
            world.setEntityBridge(entityIdSupplier, entityBridge);
        }
        if (lightDispatcher != null) {
            world.setLightDispatcher(lightDispatcher);
        }
        restoreTime(world);
        return world;
    }

    private Path forcedChunksFile(final ServerWorld world) {
        return paths.dimensionDataDir(world.dimension()).resolve(Key.MINECRAFT_NAMESPACE).resolve(ForcedChunks.FILE_NAME);
    }

    private void restoreForcedChunks(final ServerWorld world) {
        try {
            final int restored = world.forcedChunks().restore(forcedChunksFile(world));
            if (restored > 0) {
                LOGGER.info("{} force-loaded chunk(s) restored in {}", restored, world.key());
                world.loadForcedChunks();
            }
        } catch (final IOException e) {
            LOGGER.error("Unable to read the force-loaded chunks of {}", world.key(), e);
        }
    }

    private void saveForcedChunks(final ServerWorld world) throws IOException {
        world.forcedChunks().saveIfDirty(forcedChunksFile(world));
    }

    private void restoreTime(final ServerWorld world) {
        final LevelData.WorldTime saved = levelData.worldTime(world.dimension().id());
        if (saved == null) {
            return;
        }
        world.dayNightCycle().restore(saved.worldAge(), saved.dayTime(), saved.doDaylightCycle());
        LOGGER.debug(
                "Cycle de {} restaure a {} ticks",
                world.dimension().id(),
                world.dayNightCycle().timeOfDay());
    }

    private void captureTimes() {
        for (final ServerWorld world : worlds.values()) {
            final WorldTimeEngine cycle = world.dayNightCycle();
            levelData.setWorldTime(
                    world.dimension().id(), cycle.worldAge(), cycle.time(), cycle.doDaylightCycle());
        }
    }

    public void setEntityBridge(final IntSupplier idSupplier, final EntitySpawnBridge bridge) {
        this.entityIdSupplier = idSupplier;
        this.entityBridge = bridge;
        for (final ServerWorld world : worlds.values()) {
            world.setEntityBridge(idSupplier, bridge);
        }
    }

    public void setChunkLoader(final AsyncChunkLoader loader) {
        this.chunkLoader = loader;
        for (final ServerWorld world : worlds.values()) {
            world.setChunkLoader(loader);
        }
    }

    public void setLightDispatcher(final LightUpdateDispatcher dispatcher) {
        this.lightDispatcher = dispatcher;
        for (final ServerWorld world : worlds.values()) {
            world.setLightDispatcher(dispatcher);
        }
    }

    public void setStructureService(final StructureService structures) {
        this.structures = structures;
    }

    public void setDefaultGenerator(final ChunkGenerator generator) {
        this.defaultGenerator = generator;
    }

    public ServerWorld overworld() {
        final ChunkGenerator chunkGenerator = defaultGenerator;
        final ChunkGenerator generator =
                chunkGenerator != null ? chunkGenerator : FlatChunkGenerator.cobblestone(VanillaDimensionTypes.OVERWORLD);
        return registerDimension(Dimension.OVERWORLD, generator);
    }

    public Collection<ServerWorld> worlds() {
        return Collections.unmodifiableCollection(worlds.values());
    }

    public ServerWorld createWorld(final Key key, final long seed, final @Nullable WorldGenerator generator) {
        final ChunkGenerator chunkGenerator = generator != null
                ? new PluginBackedChunkGenerator(
                generator, FlatChunkGenerator.cobblestone(generator.dimensionType()))
                : FlatChunkGenerator.cobblestone(VanillaDimensionTypes.OVERWORLD);

        final Dimension dim = Dimension.datapack(key, chunkGenerator.dimensionType().key());

        final boolean existed = worlds.containsKey(dim.id());
        final ServerWorld world = registerDimension(dim, chunkGenerator, seed);
        if (!existed) {
            LOGGER.info(
                    "World '{}' created (seed={}, generator={})",
                    key,
                    seed,
                    generator != null ? generator.getClass().getName() : "flat");
        }
        // resend worlds
        FidorialServer.getInstance().players().forEach(ServerPlayer::enterConfigurationPhase);
        return world;
    }

    // FIXME: VERY likely needs a rework to use the scheduler, this can cause races, when entities/players are mid-teleport into this world, causing this method to miss them.
    @NeedsToBeRevisited("Is prone to a race")
    public @Nullable ServerWorld unloadWorld(final Key key, final boolean save) throws IOException {
        final ServerWorld world = world(key);
        if (world == null) {
            return null;
        }

        if (key.equals(Dimension.OVERWORLD.id())) {
            LOGGER.warn("Refusal to unload the main world {}", key); // Todo : Make it possible later?
            return null;
        }

        final boolean hasPlayers = FidorialServer.getInstance().players().stream().anyMatch(player -> player.world() == world);
        if (hasPlayers) {
            LOGGER.warn("Refusal to unload {}: players are still present there.", key);
            return null;
        }

        for (final AbstractEntity entity : List.copyOf(world.entityManager().all())) {
            if (entity instanceof ServerPlayer) {
                continue;
            }
            FidorialServer.getInstance().despawnEntity(entity);
        }

        if (save) {
            final WorldTimeEngine cycle = world.dayNightCycle();
            levelData.setWorldTime(world.dimension().id(), cycle.worldAge(), cycle.time(), cycle.doDaylightCycle());
            levelData.write(paths.dataDir(), paths.levelDat());
            world.saveAll();
            saveForcedChunks(world);
        }
        world.forcedChunks().releaseTickets();
        worlds.remove(key);
        final StructureService structureService = structures;
        if (structureService != null) {
            structureService.forget(key);
        }
        LOGGER.debug("Removing world '{}' from manager (save={})", key, save);
        FidorialServer.getInstance().players().forEach(ServerPlayer::enterConfigurationPhase);

        return world;
    }

    public @Nullable ServerWorld dimension(final Dimension dim) {
        return worlds.get(dim.id());
    }

    public @Nullable ServerWorld world(final Key worldKey) {
        return worlds.get(worldKey);
    }

    public LevelData levelData() {
        return levelData;
    }

    public WorldPaths paths() {
        return paths;
    }

    public void saveAll() throws IOException {
        captureTimes();
        levelData.write(paths.dataDir(), paths.levelDat());
        for (final ServerWorld w : worlds.values()) {
            w.saveAll();
            saveForcedChunks(w);
        }
        LOGGER.info("World saved ({} dimension(s))", worlds.size());
    }

    public int unloadUnusedChunks() {
        int total = 0;
        for (final ServerWorld w : worlds.values()) {
            total += w.unloadUnusedChunks();
        }
        return total;
    }

    public void saveDirty() throws IOException {
        for (final ServerWorld w : worlds.values()) {
            w.saveDirty();
            saveForcedChunks(w);
        }
    }

    @Override
    public void close() throws IOException {
        saveAll();
        storage.close();
    }
}
