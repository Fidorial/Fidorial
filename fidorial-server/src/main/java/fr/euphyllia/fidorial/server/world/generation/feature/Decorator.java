package fr.euphyllia.fidorial.server.world.generation.feature;

import fr.euphyllia.fidorial.server.world.generation.NoiseChunkGenerator;
import fr.euphyllia.fidorial.server.world.generation.climate.BiomeInfo;
import fr.euphyllia.fidorial.server.world.generation.random.XoroshiroRandom;
import fr.euphyllia.fidorial.server.world.generation.terrain.GenChunk;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Decorates the chunks: every chunk runs the features of its biomes once, and they may write into the eight
 * chunks around it. A chunk is final once the nine chunks around it, itself included, are decorated.
 *
 * <p>The chunks waiting for their neighbours are kept here. A decoration locks the nine chunks it may touch, in a
 * fixed order, so that decorations far enough apart run in parallel. A decoration sees the decorations already
 * done around it, the order depending on which chunks are asked for first.</p>
 *
 * <p>A chunk asked for a second time, after it was handed over, is decorated again on its own, with its nine
 * decorations replayed in a private copy of the area.</p>
 */
public final class Decorator {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(Decorator.class);
    private static final int MAX_WAITING = 4096;
    private static final int MAX_REMEMBERED = 1 << 18;

    /**
     * A decorated chunk.
     *
     * @param chunk         the blocks
     * @param blockEntities the extra data of the block entities, by {@code BlockEntity.positionKey}
     * @param fluidTicks    the fluids that must start flowing: absolute x, y, z, then 1 for lava or 0 for water
     */
    public record Result(GenChunk chunk, Map<Integer, CompoundBinaryTag> blockEntities, List<int[]> fluidTicks) {
    }

    private final NoiseChunkGenerator generator;
    private final DecorationContext context;
    private final FeatureOrder order;
    private final Map<Long, DecorationChunk> waiting = new ConcurrentHashMap<>();
    private final LongLinkedOpenHashSet decorated = new LongLinkedOpenHashSet();
    private final Map<String, Boolean> failing = new ConcurrentHashMap<>();

    public Decorator(final NoiseChunkGenerator generator) {
        this.generator = generator;
        this.context = new DecorationContext(generator);
        this.order = FeatureOrder.of(generator.biomes().all(), generator.environment().data());
    }

    public DecorationContext context() {
        return context;
    }

    private static long key(final int chunkX, final int chunkZ) {
        return (long) chunkX & 0xFFFFFFFFL | ((long) chunkZ & 0xFFFFFFFFL) << 32;
    }

    private boolean isDecorated(final long key) {
        synchronized (decorated) {
            return decorated.contains(key);
        }
    }

    private void markDecorated(final long key) {
        synchronized (decorated) {
            decorated.add(key);
            while (decorated.size() > MAX_REMEMBERED) {
                decorated.removeFirstLong();
            }
        }
    }

    /**
     * {@return a chunk with its decorations and those of the chunks around it}
     */
    public Result decorate(final int chunkX, final int chunkZ) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                ensureDecorated(chunkX + dx, chunkZ + dz);
            }
        }
        final DecorationChunk chunk = waiting.get(key(chunkX, chunkZ));
        if (chunk != null) {
            chunk.lock().lock();
            try {
                if (!chunk.handedOver() && !chunk.missesDecorations()) {
                    chunk.markHandedOver();
                    waiting.remove(key(chunkX, chunkZ), chunk);
                    return new Result(chunk.blocks(), chunk.blockEntities(), chunk.fluidTicks());
                }
            } finally {
                chunk.lock().unlock();
            }
        }
        return decorateAlone(chunkX, chunkZ);
    }

    private DecorationChunk waitingChunk(final int chunkX, final int chunkZ) {
        final long key = key(chunkX, chunkZ);
        final DecorationChunk known = waiting.get(key);
        if (known != null) {
            return known;
        }
        final GenChunk terrain = generator.terrain(chunkX, chunkZ);
        final DecorationChunk created = waiting.computeIfAbsent(key, k -> {
            final DecorationChunk chunk = new DecorationChunk(terrain.copy(), context.states());
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (isDecorated(key(chunkX + dx, chunkZ + dz))) {
                        chunk.markMissesDecorations();
                    }
                }
            }
            return chunk;
        });
        if (waiting.size() > MAX_WAITING) {
            trimWaiting(key);
        }
        return created;
    }

    /**
     * Forgets the waiting chunks no decoration is using, but the one just made; they will be decorated again on
     * their own if they are ever asked for.
     */
    private void trimWaiting(final long keep) {
        int excess = waiting.size() - MAX_WAITING / 2;
        for (final Map.Entry<Long, DecorationChunk> entry : waiting.entrySet()) {
            if (excess <= 0) {
                return;
            }
            if (entry.getKey() == keep) {
                continue;
            }
            final DecorationChunk chunk = entry.getValue();
            if (chunk.lock().tryLock()) {
                try {
                    if (waiting.remove(entry.getKey(), chunk)) {
                        excess--;
                    }
                } finally {
                    chunk.lock().unlock();
                }
            }
        }
    }

    private void ensureDecorated(final int chunkX, final int chunkZ) {
        final long key = key(chunkX, chunkZ);
        if (isDecorated(key)) {
            return;
        }
        final DecorationChunk[] chunks = new DecorationChunk[9];
        final long[] keys = new long[9];
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                final int index = (dx + 1) * 3 + dz + 1;
                chunks[index] = waitingChunk(chunkX + dx, chunkZ + dz);
                keys[index] = key(chunkX + dx, chunkZ + dz);
            }
        }
        final Integer[] lockOrder = {0, 1, 2, 3, 4, 5, 6, 7, 8};
        Arrays.sort(lockOrder, Comparator.comparingLong(i -> keys[i]));
        for (final Integer index : lockOrder) {
            chunks[index].lock().lock();
        }
        try {
            if (isDecorated(key)) {
                return;
            }
            run(new DecorationRegion(context, chunkX, chunkZ, chunks));
            markDecorated(key);
        } finally {
            for (int i = lockOrder.length - 1; i >= 0; i--) {
                chunks[lockOrder[i]].lock().unlock();
            }
        }
    }

    /**
     * Decorates a chunk again, away from the shared chunks: the area around it is rebuilt from the terrain and
     * its nine decorations run there one after the other.
     */
    private Result decorateAlone(final int chunkX, final int chunkZ) {
        final Map<Long, DecorationChunk> area = new HashMap<>();
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                area.put(key(chunkX + dx, chunkZ + dz),
                        new DecorationChunk(generator.terrain(chunkX + dx, chunkZ + dz).copy(), context.states()));
            }
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                final DecorationChunk[] chunks = new DecorationChunk[9];
                for (int ox = -1; ox <= 1; ox++) {
                    for (int oz = -1; oz <= 1; oz++) {
                        chunks[(ox + 1) * 3 + oz + 1] = area.get(key(chunkX + dx + ox, chunkZ + dz + oz));
                    }
                }
                run(new DecorationRegion(context, chunkX + dx, chunkZ + dz, chunks));
            }
        }
        final DecorationChunk chunk = area.get(key(chunkX, chunkZ));
        return new Result(chunk.blocks(), chunk.blockEntities(), chunk.fluidTicks());
    }

    /**
     * Runs the features of a chunk, step after step.
     */
    private void run(final DecorationRegion region) {
        final int originX = region.centerX() << 4;
        final int originZ = region.centerZ() << 4;
        final XoroshiroRandom random = new XoroshiroRandom(0L);
        final long decorationSeed = random.setDecorationSeed(generator.seed(), originX, originZ);

        final BitSet biomes = new BitSet();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                region.chunk(dx, dz).blocks().biomes().collectOwn(biomes);
            }
        }
        final List<BiomeInfo> possible = new ArrayList<>();
        for (int biome = biomes.nextSetBit(0); biome >= 0; biome = biomes.nextSetBit(biome + 1)) {
            possible.add(context.biomes().get(biome));
        }

        for (int step = 0; step < order.stepCount(); step++) {
            final List<String> features = order.step(step);
            final BitSet present = new BitSet(features.size());
            for (final BiomeInfo biome : possible) {
                if (step >= biome.features().size()) {
                    continue;
                }
                for (final String entry : biome.features().get(step)) {
                    for (final String feature : FeatureOrder.expand(context.data(), entry)) {
                        final int position = order.position(step, feature);
                        if (position >= 0) {
                            present.set(position);
                        }
                    }
                }
            }
            for (int index = present.nextSetBit(0); index >= 0; index = present.nextSetBit(index + 1)) {
                final String id = features.get(index);
                random.setFeatureSeed(decorationSeed, index, step);
                try {
                    final PlacedFeature placed = context.library().placed(id);
                    placed.place(region, random, originX, region.minY(), originZ, true);
                } catch (final RuntimeException exception) {
                    if (failing.putIfAbsent(id, Boolean.TRUE) == null) {
                        LOGGER.warn("The feature {} failed in chunk {} {}; it is skipped from now on where it fails",
                                id, region.centerX(), region.centerZ(), exception);
                    }
                }
            }
        }
    }
}
