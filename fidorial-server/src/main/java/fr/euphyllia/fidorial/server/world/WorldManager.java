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
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
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
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
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
    private volatile @Nullable Key preferredDefaultKey;
    private volatile @Nullable Key defaultWorldKey;

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
        reassignDefaultWorld();
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

    /**
     * Sets the key of the world preferred as the server's default.
     *
     * <p>This preference is remembered even while the world it names is unloaded, so that
     * reloading it later automatically restores it as the active default. Pass {@code null}
     * to clear the preference.</p>
     *
     * @param key the preferred default world's key, or {@code null} to clear it
     * @since 0.1.0
     */
    public void setDefaultWorld(final @Nullable Key key) {
        this.preferredDefaultKey = key;
        reassignDefaultWorld();
    }

    /**
     * {@return the configured default world key set via {@link #setDefaultWorld}, or {@code null}
     * if none is set, independent of whether that world is currently loaded}
     */
    public @Nullable Key defaultWorldKey() {
        return preferredDefaultKey;
    }

    /**
     * {@return the currently active default world, or {@link Optional#empty()} if no world is loaded}
     *
     * @since 0.1.0
     */
    public Optional<ServerWorld> defaultWorld() {
        final Key key = defaultWorldKey;
        return key == null ? Optional.empty() : Optional.ofNullable(world(key));
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

    private @Nullable ServerWorld mostPopulatedWorld() {
        if (worlds.isEmpty()) {
            return null;
        }

        final Object2IntOpenHashMap<Key> population = new Object2IntOpenHashMap<>();
        for (final ServerPlayer player : FidorialServer.getInstance().players()) {
            population.addTo(player.world().key(), 1);
        }

        ServerWorld best = null;
        int bestCount = -1;
        for (final ServerWorld world : worlds.values()) {
            final int count = population.getInt(world.key());
            if (count > bestCount || (count == bestCount && world.key().compareTo(best.key()) < 0)) {
                best = world;
                bestCount = count;
            }
        }
        return best;
    }

    public Collection<ServerWorld> worlds() {
        return Collections.unmodifiableCollection(worlds.values());
    }

    public ServerWorld createWorld(final Key key, final long seed, final @Nullable WorldGenerator generator, final boolean silent) {
        final ChunkGenerator chunkGenerator = generator != null
                ? new PluginBackedChunkGenerator(
                generator, FlatChunkGenerator.cobblestone(generator.dimensionType()))
                : FlatChunkGenerator.cobblestone(VanillaDimensionTypes.OVERWORLD);

        final Dimension dim = Dimension.datapack(key, chunkGenerator.dimensionType().key());

        final boolean existed = worlds.containsKey(dim.id());
        final ServerWorld world = registerDimension(dim, chunkGenerator, seed);
        if (!existed && !silent) {
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

        if (worlds.size() <= 1) {
            LOGGER.warn("Refusal to unload {}: it is the last loaded world.", key);
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
        reassignDefaultWorld();

        final StructureService structureService = structures;
        if (structureService != null) {
            structureService.forget(key);
        }
        LOGGER.debug("Removing world '{}' from manager (save={})", key, save);
        FidorialServer.getInstance().players().forEach(ServerPlayer::enterConfigurationPhase);

        return world;
    }

    /**
     * Re-resolves the currently active default world.
     *
     * <p>Resolution order:</p>
     * <ol>
     *   <li>the {@linkplain #setDefaultWorld preferred default world}, if currently loaded;</li>
     *   <li>the overworld, if loaded;</li>
     *   <li>otherwise, the loaded world with the most players currently in it.</li>
     * </ol>
     */
    private void reassignDefaultWorld() {
        final Key preferred = preferredDefaultKey;
        final ServerWorld preferredWorld = preferred != null ? worlds.get(preferred) : null;
        final ServerWorld overworld = worlds.get(Dimension.OVERWORLD.id());
        final ServerWorld next = preferredWorld != null ? preferredWorld
                : overworld != null ? overworld
                : mostPopulatedWorld();

        final Key previous = defaultWorldKey;
        final Key resolved = next == null ? null : next.key();
        if (Objects.equals(previous, resolved)) {
            return;
        }
        defaultWorldKey = resolved;

        if (resolved == null) {
            LOGGER.warn("No default world is currently resolvable: no worlds are loaded.");
        } else if (previous == null) {
            LOGGER.info("Default world set to {}", resolved);
        } else {
            LOGGER.info("Default world changed from {} to {}", previous, resolved);
        }
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

    public CompletableFuture<Integer> unloadUnusedChunks() {
        CompletableFuture<Integer> total = CompletableFuture.completedFuture(0);
        for (final ServerWorld w : worlds.values()) {
            total = total.thenCombine(w.unloadUnusedChunks(), Integer::sum);
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
