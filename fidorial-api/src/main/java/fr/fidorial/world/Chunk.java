package fr.fidorial.world;

import fr.fidorial.scheduler.SchedulerSource;

public interface Chunk extends SchedulerSource {

    World world();

    int chunkX();

    int chunkZ();

    default ChunkPos pos() {
        return new ChunkPos(chunkX(), chunkZ());
    }

    /**
     * Whether this chunk is force-loaded.
     *
     * @return {@code true} if this chunk is force-loaded
     * @see World#isChunkForceLoaded(int, int)
     * @since 0.1.0
     */
    default boolean isForceLoaded() {
        return world().isChunkForceLoaded(chunkX(), chunkZ());
    }

    int minY();

    int height();

    int getBlockStateId(int localX, int worldY, int localZ);

    boolean setBlockStateId(int localX, int worldY, int localZ, int stateId);

    int blockLight(int localX, int worldY, int localZ);

    int skyLight(int localX, int worldY, int localZ);

    int lightLevel(int localX, int worldY, int localZ);
}
