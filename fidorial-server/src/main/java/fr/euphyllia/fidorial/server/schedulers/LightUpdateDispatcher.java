package fr.euphyllia.fidorial.server.schedulers;

import ca.spottedleaf.concurrentutil.collection.MultiThreadedQueue;
import ca.spottedleaf.concurrentutil.executor.PrioritisedExecutor;
import ca.spottedleaf.concurrentutil.executor.thread.BalancedPrioritisedThreadPool;
import ca.spottedleaf.concurrentutil.util.Priority;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundLightUpdatePacket;
import fr.euphyllia.fidorial.server.util.ConcurrentLongSet;
import fr.euphyllia.fidorial.server.world.ChunkNetworkSerializer;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.chunk.ChunkColumn;
import fr.euphyllia.fidorial.server.world.light.FloodFillLightEngine;
import fr.euphyllia.fidorial.server.world.light.LightEnginePool;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.math.Position;
import fr.fidorial.world.ChunkPos;
import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterable;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class LightUpdateDispatcher {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(LightUpdateDispatcher.class);

    private static final int CLUSTER_SHIFT = 4; // seems to give the most optimal CPS

    private final int lightWorkers;
    private final BalancedPrioritisedThreadPool lightPool;
    private final Map<Bounds, LightEnginePool> enginePools = new ConcurrentHashMap<>();
    private final PrioritisedExecutor lightExecutor;

    private final Map<Key, ChunkRegionScheduler> regionSchedulers = new ConcurrentHashMap<>();

    private final AtomicInteger lightThreadId = new AtomicInteger(0);
    private final ChunkNetworkSerializer serializer;
    private final Function<Key, @Nullable ServerWorld> worldLookup;

    private final Map<Key, WorldLightState> states = new ConcurrentHashMap<>();

    private final Map<Key, MultiThreadedQueue<BlockPosition>> pendingBlocks = new ConcurrentHashMap<>();
    private final Set<Key> scheduledBlocks = ConcurrentHashMap.newKeySet();
    private final Map<Key, ConcurrentLongSet> pendingChunks = new ConcurrentHashMap<>();
    private final Set<Key> scheduledChunks = ConcurrentHashMap.newKeySet();

    public LightUpdateDispatcher(
            final int lightWorkers,
            final ChunkNetworkSerializer serializer,
            final Function<Key, @Nullable ServerWorld> worldLookup
    ) {
        this.lightWorkers = lightWorkers;
        this.lightPool = new BalancedPrioritisedThreadPool(
                BalancedPrioritisedThreadPool.DEFAULT_GROUP_TIME_SLICE,
                r -> Thread.ofPlatform()
                        .name("fidorial-light-worker-" + lightThreadId.incrementAndGet())
                        .unstarted(r)
        );
        this.lightExecutor = lightPool.createOrderedStreamGroup().createExecutor();
        this.lightPool.adjustThreadCount(Math.max(1, lightWorkers));
        this.serializer = serializer;
        this.worldLookup = worldLookup;
        LOGGER.info("Light pool started with {} workers", lightWorkers);
    }

    private LightEnginePool poolFor(final ServerWorld serverWorld) {
        final Bounds bounds = new Bounds(serverWorld.minY(), serverWorld.height());
        return enginePools.computeIfAbsent(bounds, b -> new LightEnginePool(lightWorkers, b.minY(), b.height()));
    }

    private static @Nullable CompletableFuture<Void> decrementLocked(final Stripe stripe, final long key) {
        final int count = stripe.refCounts.get(key);

        if (count == 0) return null;
        if (count <= 1) {
            stripe.refCounts.remove(key);
            return stripe.waiters.remove(key);
        }
        stripe.refCounts.put(key, count - 1);
        return null;
    }

    public void queueBlockChange(final Key world, final int x, final int y, final int z) {
        incrementArea(world, x >> 4, z >> 4);

        pendingBlocks
                .computeIfAbsent(world, _ -> new MultiThreadedQueue<>())
                .add(Position.block(x, y, z));

        scheduleBlocks(world);
    }

    public void queueChunkLoad(final Key world, final int chunkX, final int chunkZ) {
        final ConcurrentLongSet set = pendingChunks.computeIfAbsent(world, _ -> new ConcurrentLongSet());
        final WorldLightState state = stateFor(world);

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                final long key = ChunkPos.chunkKey(chunkX + dx, chunkZ + dz);
                final Stripe stripe = state.stripe(key);
                synchronized (stripe) {
                    if (set.add(key)) {
                        stripe.refCounts.addTo(key, 1);
                    }
                }
            }
        }

        scheduleChunks(world);
    }

    private void incrementArea(final Key world, final int chunkX, final int chunkZ) {
        final WorldLightState state = stateFor(world);

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                final long key = ChunkPos.chunkKey(chunkX + dx, chunkZ + dz);
                final Stripe stripe = state.stripe(key);
                synchronized (stripe) {
                    stripe.refCounts.addTo(key, 1);
                }
            }
        }
    }

    private void decrementArea(final Key world, final int chunkX, final int chunkZ) {
        final WorldLightState state = states.get(world);
        if (state == null) return;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                final long key = ChunkPos.chunkKey(chunkX + dx, chunkZ + dz);
                final Stripe stripe = state.stripe(key);
                final CompletableFuture<@Nullable Void> waiter;
                synchronized (stripe) {
                    waiter = decrementLocked(stripe, key);
                }
                if (waiter != null) {
                    waiter.complete(null);
                }
            }
        }
    }

    private void drainBlocks(final Key world) {
        final MultiThreadedQueue<BlockPosition> queue = pendingBlocks.get(world);
        if (queue == null) {
            scheduledBlocks.remove(world);
            return;
        }

        final ServerWorld serverWorld = worldLookup.apply(world);
        if (serverWorld == null) {
            BlockPosition pos;
            while ((pos = queue.poll()) != null) {
                decrementArea(world, pos.chunkX(), pos.chunkZ());
            }
        } else if (!queue.isEmpty()) {
            final LightEnginePool pool = poolFor(serverWorld);
            final Long2ObjectOpenHashMap<List<BlockPosition>> byCluster = new Long2ObjectOpenHashMap<>();
            int processed = 0;
            BlockPosition pos;
            while (processed < 4096 && (pos = queue.poll()) != null) {
                processed++;
                final int cx = pos.chunkX();
                final int cz = pos.chunkZ();
                final long clusterKey = ChunkPos.chunkKey(cx >> CLUSTER_SHIFT, cz >> CLUSTER_SHIFT);
                byCluster.computeIfAbsent(clusterKey, _ -> new ArrayList<>()).add(pos);
            }

            for (final Long2ObjectOpenHashMap.Entry<List<BlockPosition>> entry : byCluster.long2ObjectEntrySet()) {
                final List<BlockPosition> positions = entry.getValue();
                final LongOpenHashSet chunkKeys = new LongOpenHashSet();
                for (final BlockPosition p : positions) {
                    chunkKeys.add(ChunkPos.chunkKey(p.chunkX(), p.chunkZ()));
                }
                queueAreaTask(world, chunkKeys, () -> {
                    FloodFillLightEngine engine = null;
                    try {
                        engine = pool.acquire();
                        final LongOpenHashSet dirtyChunks = new LongOpenHashSet();
                        for (final BlockPosition p : positions) {
                            dirtyChunks.addAll(serverWorld.checkBlockLight(p.blockX(), p.blockY(), p.blockZ(), engine));
                        }
                        broadcastLight(serverWorld, dirtyChunks);
                    } catch (final InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        for (final BlockPosition p : positions) {
                            decrementArea(world, p.chunkX(), p.chunkZ());
                        }
                        if (engine != null) {
                            pool.release(engine);
                        }
                    }
                }, Priority.HIGH);
            }
        }
        scheduledBlocks.remove(world);
        if (!queue.isEmpty()) {
            scheduleBlocks(world);
        }
    }

    private void queueAreaTask(final Key world, final LongOpenHashSet chunkKeys, final Runnable task, final Priority priority) {
        if (chunkKeys.isEmpty()) {
            task.run();
            return;
        }

        final LongSet lockKeys = withNeighborRing(chunkKeys);
        regionSchedulers
                .computeIfAbsent(world, _ -> new ChunkRegionScheduler(lightExecutor))
                .submit(lockKeys, task, priority);
    }

    private static LongSet withNeighborRing(final LongOpenHashSet chunkKeys) {
        final LongOpenHashSet ring = new LongOpenHashSet(chunkKeys);
        final LongIterator it = chunkKeys.iterator();
        while (it.hasNext()) {
            final long key = it.nextLong();
            final int cx = (int) (key >> 32), cz = (int) key;
            for (int dx = -1; dx <= 1; dx++)
                for (int dz = -1; dz <= 1; dz++) {
                    final long nk = ChunkPos.chunkKey(cx + dx, cz + dz);
                    if (!chunkKeys.contains(nk)) ring.add(nk);
                }
        }
        return ring;
    }

    private void drainChunks(final Key world) {
        final ConcurrentLongSet pending = pendingChunks.get(world);
        if (pending == null) {
            scheduledChunks.remove(world);
            return;
        }

        if (!pending.isEmpty()) {
            final LongSet snapshot = pending.snapshot();
            pending.removeAll(snapshot);

            final ServerWorld serverWorld = worldLookup.apply(world);
            if (serverWorld == null) {
                decrement(world, snapshot.toLongArray());
            } else if (!snapshot.isEmpty()) {
                final Long2ObjectOpenHashMap<LongOpenHashSet> clusters = new Long2ObjectOpenHashMap<>();
                final LongIterator snapIt = snapshot.iterator();
                while (snapIt.hasNext()) {
                    final long key = snapIt.nextLong();
                    final int cx = (int) (key >> 32);
                    final int cz = (int) key;
                    final long clusterKey = ChunkPos.chunkKey(cx >> CLUSTER_SHIFT, cz >> CLUSTER_SHIFT);
                    clusters.computeIfAbsent(clusterKey, _ -> new LongOpenHashSet()).add(key);
                }
                for (final LongOpenHashSet subBatch : clusters.values()) {
                    submitRelightTask(world, serverWorld, subBatch);
                }
            }
        }

        scheduledChunks.remove(world);
        if (!pending.isEmpty()) {
            scheduleChunks(world);
        }
    }

    private void submitRelightTask(final Key world, final ServerWorld serverWorld, final LongOpenHashSet subBatch) {
        final LightEnginePool pool = poolFor(serverWorld);
        queueAreaTask(world, subBatch, () -> {
            FloodFillLightEngine engine = null;
            try {
                engine = pool.acquire();
                broadcastLight(serverWorld, serverWorld.relightChunks(subBatch, engine));
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (final Throwable t) {
                LOGGER.error("Lighting recalculation impossible for world {}", world, t);
            } finally {
                if (engine != null) pool.release(engine);
                decrement(world, subBatch.toLongArray());
            }
        }, Priority.NORMAL);
    }

    private void broadcastLight(final ServerWorld serverWorld, final LongSet dirtyChunks) {
        final LongIterator it = dirtyChunks.iterator();
        while (it.hasNext()) {
            final long key = it.nextLong();
            if (!serverWorld.isViewed(key)) continue;
            final ChunkColumn column = serverWorld.loadedColumn((int) (key >> 32), (int) key);
            if (column == null) {
                continue;
            }
            serverWorld.sendToChunkViewers(key,
                    new ClientboundLightUpdatePacket(serializer, column, serverWorld.dimensionType().hasSkylight()));
        }
    }

    private void scheduleBlocks(final Key world) {
        if (scheduledBlocks.add(world)) {
            try {
                lightExecutor.queueTask(() -> drainBlocks(world), Priority.HIGH);
            } catch (final IllegalStateException e) {
                drainBlocks(world);
            }
        }
    }

    private void scheduleChunks(final Key world) {
        if (scheduledChunks.add(world)) {
            try {
                lightExecutor.queueTask(() -> drainChunks(world), Priority.HIGH);
            } catch (final IllegalStateException e) {
                drainChunks(world);
            }
        }
    }

    private WorldLightState stateFor(final Key world) {
        return states.computeIfAbsent(world, _ -> new WorldLightState());
    }

    private void decrement(final Key world, final long[] chunkKeys) {
        final WorldLightState state = states.get(world);
        if (state == null) return;
        for (final long key : chunkKeys) {
            final Stripe stripe = state.stripe(key);
            final CompletableFuture<@Nullable Void> waiter;
            synchronized (stripe) {
                waiter = decrementLocked(stripe, key);
            }
            if (waiter != null) {
                waiter.complete(null);
            }
        }
    }

    public CompletableFuture<Void> flush(final Key world, final long chunkKey) {
        final WorldLightState state = states.get(world);
        if (state == null) {
            return CompletableFuture.completedFuture(null);
        }
        final Stripe stripe = state.stripe(chunkKey);
        synchronized (stripe) {
            if (stripe.refCounts.containsKey(chunkKey)) {
                return stripe.waiters.computeIfAbsent(chunkKey, _ -> new CompletableFuture<>());
            }
        }
        return CompletableFuture.completedFuture(null);
    }

    public CompletableFuture<Void> flush(final Key world, final LongIterable chunkKeys) {
        final WorldLightState state = states.get(world);
        if (state == null) {
            return CompletableFuture.completedFuture(null);
        }
        final List<CompletableFuture<Void>> futures = new ArrayList<>();
        final LongIterator it = chunkKeys.iterator();
        while (it.hasNext()) {
            final long key = it.nextLong();
            final Stripe stripe = state.stripe(key);
            synchronized (stripe) {
                if (stripe.refCounts.containsKey(key)) {
                    futures.add(stripe.waiters.computeIfAbsent(key, _ -> new CompletableFuture<>()));
                }
            }
        }
        return futures.isEmpty()
                ? CompletableFuture.completedFuture(null)
                : CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    public void shutdown() {
        lightPool.shutdown(false);
        if (!lightPool.join(TimeUnit.SECONDS.toMillis(5L))) {
            lightPool.halt(true);
        }
        LOGGER.info("Light workers stopped");
    }

    private static final class WorldLightState {
        private static final int STRIPES = 64; // power of two
        private final Stripe[] stripes = new Stripe[STRIPES];

        WorldLightState() {
            for (int i = 0; i < STRIPES; i++) {
                stripes[i] = new Stripe();
            }
        }

        Stripe stripe(final long key) {
            return stripes[(int) HashCommon.mix(key) & (STRIPES - 1)];
        }
    }

    private static final class Stripe {
        private final Long2IntOpenHashMap refCounts = new Long2IntOpenHashMap(512);
        private final Long2ObjectOpenHashMap<@Nullable CompletableFuture<Void>> waiters = new Long2ObjectOpenHashMap<>();
    }

    private record Bounds(int minY, int height) {
    }
}
