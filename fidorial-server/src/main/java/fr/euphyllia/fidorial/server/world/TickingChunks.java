package fr.euphyllia.fidorial.server.world;

import ca.spottedleaf.concurrentutil.map.concurrent.longs.ConcurrentChainedLong2IntHashTable;
import fr.fidorial.world.ChunkPos;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

/**
 * Which chunks of a world tick, with vanilla's chunk levels.
 *
 * <p>A source (a player, with the simulation distance as radius, or a force-loaded chunk, with
 * radius {@code 0}) makes the chunks around it:</p>
 * <ul>
 *   <li><em>entity ticking</em> up to its radius: entities tick there (vanilla level 31);</li>
 *   <li><em>block ticking</em> one chunk further: the ticks scheduled on blocks and fluids run there
 *   (vanilla level 32);</li>
 *   <li>loaded but frozen one chunk further still, the border (vanilla level 33): redstone and
 *   fluids may change its blocks, but what they schedule there waits until it ticks.</li>
 * </ul>
 *
 * <p>Like vanilla's levels, a chunk only ticks once the chunks it relies on are loaded:
 * {@link ServerWorld#isBlockTicking} and {@link ServerWorld#isEntityTicking} check that too.</p>
 */
public final class TickingChunks {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(TickingChunks.class);

    private final ServerWorld world;
    private final ConcurrentChainedLong2IntHashTable entityTicking = new ConcurrentChainedLong2IntHashTable();
    private final ConcurrentChainedLong2IntHashTable blockTicking = new ConcurrentChainedLong2IntHashTable();
    private final ConcurrentChainedLong2IntHashTable keptLoaded = new ConcurrentChainedLong2IntHashTable();
    private final Object lock = new Object();

    TickingChunks(final ServerWorld world) {
        this.world = world;
    }

    /**
     * Adds a source, and starts loading the chunks it keeps loaded.
     *
     * @param radius the radius entities tick within, in chunks
     */
    public void add(final int chunkX, final int chunkZ, final int radius) {
        final LongArrayList toLoad = new LongArrayList();
        synchronized (lock) {
            change(chunkX, chunkZ, radius, true, toLoad);
        }
        load(toLoad);
    }

    public void remove(final int chunkX, final int chunkZ, final int radius) {
        synchronized (lock) {
            change(chunkX, chunkZ, radius, false, null);
        }
    }

    /**
     * Moves a source, adding before removing so that the chunks both cover never stop ticking.
     */
    public void move(final int fromX, final int fromZ, final int toX, final int toZ, final int radius) {
        if (fromX == toX && fromZ == toZ) {
            return;
        }
        final LongArrayList toLoad = new LongArrayList();
        synchronized (lock) {
            change(toX, toZ, radius, true, toLoad);
            change(fromX, fromZ, radius, false, null);
        }
        load(toLoad);
    }

    /**
     * {@return whether a source wants entities to tick in the chunk}, whether its neighbours are loaded or not
     */
    boolean wantsEntityTicking(final long chunkKey) {
        return entityTicking.containsKey(chunkKey);
    }

    /**
     * {@return whether a source wants the blocks of the chunk to tick}, whether its neighbours are loaded or not
     */
    boolean wantsBlockTicking(final long chunkKey) {
        return blockTicking.containsKey(chunkKey);
    }

    /**
     * {@return whether a source keeps the chunk loaded}
     */
    public boolean keepsLoaded(final long chunkKey) {
        return keptLoaded.containsKey(chunkKey);
    }

    private void change(final int chunkX, final int chunkZ, final int radius, final boolean add,
                        final @Nullable LongArrayList toLoad) {
        final int keep = radius + 2;
        for (int dx = -keep; dx <= keep; dx++) {
            for (int dz = -keep; dz <= keep; dz++) {
                final int distance = Math.max(Math.abs(dx), Math.abs(dz));
                final long key = ChunkPos.chunkKey(chunkX + dx, chunkZ + dz);
                if (distance <= radius) {
                    count(entityTicking, key, add);
                }
                if (distance <= radius + 1) {
                    count(blockTicking, key, add);
                }
                if (count(keptLoaded, key, add) && toLoad != null) {
                    toLoad.add(key);
                }
            }
        }
    }

    /**
     * {@return whether the chunk just started being counted}
     */
    private static boolean count(final ConcurrentChainedLong2IntHashTable counts, final long key, final boolean add) {
        if (add) {
            return counts.addTo(key, 1, 1) == 1;
        }
        if (counts.containsKey(key)) {
            counts.decFrom(key, 1, 0);
        }
        return false;
    }

    private void load(final LongArrayList keys) {
        for (int i = 0; i < keys.size(); i++) {
            final long key = keys.getLong(i);
            final int chunkX = (int) (key >> 32);
            final int chunkZ = (int) key;
            world.chunkAsync(chunkX, chunkZ).whenComplete((_, failure) -> {
                if (failure != null) {
                    LOGGER.warn("Unable to load the chunk {},{} kept loaded in {}", chunkX, chunkZ, world.key(), failure);
                }
            });
        }
    }
}
