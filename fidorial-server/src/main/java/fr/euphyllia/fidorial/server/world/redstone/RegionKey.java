package fr.euphyllia.fidorial.server.world.redstone;

import fr.euphyllia.fidorial.server.schedulers.ThreadedRegionRegionizer;
import fr.fidorial.world.ChunkPos;
import net.kyori.adventure.key.Key;

/**
 * One region of a world, what the redstone engine keeps its pending work by: scheduled ticks,
 * block events and the blocks pistons are moving.
 *
 * @param dimension the world of the region
 * @param sectionX  the section coordinate of the region on the x axis
 * @param sectionZ  the section coordinate of the region on the z axis
 */
record RegionKey(Key dimension, int sectionX, int sectionZ) {

    /**
     * {@return the region owning a chunk}
     *
     * @param dimension the world of the chunk
     * @param chunk     the chunk
     */
    static RegionKey of(final Key dimension, final ChunkPos chunk) {
        return new RegionKey(dimension, ThreadedRegionRegionizer.chunkToSection(chunk.x()),
                ThreadedRegionRegionizer.chunkToSection(chunk.z()));
    }
}
