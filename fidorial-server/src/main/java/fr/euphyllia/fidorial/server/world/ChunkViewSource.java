package fr.euphyllia.fidorial.server.world;

import it.unimi.dsi.fastutil.longs.LongSet;

public interface ChunkViewSource {

    void collectViewedChunks(LongSet target);

    boolean isViewing(long chunkKey);
}
