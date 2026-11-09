package fr.euphyllia.fidorial.server.schedulers;

import fr.fidorial.world.ChunkPos;
import net.kyori.adventure.key.Key;

/**
 * Work run once per tick of a region, over all its sections at once, before the
 * {@linkplain fr.fidorial.scheduler.RegionTickHandler section handlers}: what has to be ordered
 * across the whole region, like the ticks scheduled on blocks and fluids.
 */
@FunctionalInterface
public interface RegionWideTickHandler {

    /**
     * @param world       the key of the world of the region
     * @param sectionKeys the {@linkplain ChunkPos#chunkKey(int, int) keys} of the
     *                    section coordinates of the region; not to be kept or changed
     */
    void tick(Key world, long[] sectionKeys);
}
