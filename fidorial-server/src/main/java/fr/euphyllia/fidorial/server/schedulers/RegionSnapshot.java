package fr.euphyllia.fidorial.server.schedulers;

import fr.fidorial.scheduler.Region;
import fr.fidorial.world.ChunkPos;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.kyori.adventure.key.Key;

/**
 * A {@link TickRegion} as the API sees it: frozen when taken.
 *
 * @param sectionKeys the {@linkplain ChunkPos#chunkKey(int, int) keys} of the section coordinates
 */
public record RegionSnapshot(long id, Key world, LongSet sectionKeys, ChunkPos centerChunk) implements Region {

    @Override
    public int sectionCount() {
        return sectionKeys.size();
    }

    @Override
    public boolean contains(final ChunkPos chunk) {
        return sectionKeys.contains(ChunkPos.chunkKey(
                ThreadedRegionRegionizer.chunkToSection(chunk.x()), ThreadedRegionRegionizer.chunkToSection(chunk.z())));
    }
}
