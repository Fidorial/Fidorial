package fr.fidorial.world;

import fr.fidorial.scheduler.SchedulerSource;

import java.util.concurrent.CompletableFuture;

/**
 * A loaded 16x16 column of a {@link World}.
 *
 * <p>Block coordinates taken by this interface are local on the X and Z axes ({@code 0} to
 * {@code 15}) and absolute on the Y axis. Tasks scheduled through {@link SchedulerSource} run on
 * the region thread owning this chunk.</p>
 *
 * @since 0.1.0
 */
public interface Chunk extends SchedulerSource {

    /**
     * {@return the world this chunk belongs to}
     *
     * @since 0.1.0
     */
    World world();

    /**
     * {@return the chunk X coordinate}
     *
     * @since 0.1.0
     */
    int chunkX();

    /**
     * {@return the chunk Z coordinate}
     *
     * @since 0.1.0
     */
    int chunkZ();

    /**
     * {@return the position of this chunk}
     *
     * @since 0.1.0
     */
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

    /**
     * {@return the lowest block Y coordinate of this chunk, inclusive}
     *
     * @since 0.1.0
     */
    int minY();

    /**
     * {@return the number of block layers of this chunk, starting at {@link #minY()}}
     *
     * @since 0.1.0
     */
    int height();

    /**
     * Gets the block state at a position of this chunk.
     *
     * @param localX the X coordinate inside the chunk, {@code 0} to {@code 15}
     * @param worldY the absolute Y coordinate
     * @param localZ the Z coordinate inside the chunk, {@code 0} to {@code 15}
     * @return the network identifier of the block state
     * @since 0.1.0
     */
    int blockStateId(int localX, int worldY, int localZ);

    /**
     * Replaces the block state at a position of this chunk, on the region thread owning it.
     *
     * @param localX  the X coordinate inside the chunk, {@code 0} to {@code 15}
     * @param worldY  the absolute Y coordinate
     * @param localZ  the Z coordinate inside the chunk, {@code 0} to {@code 15}
     * @param stateId the network identifier of the new block state
     * @return a future completing with {@code true} if the block changed
     * @since 0.1.0
     */
    CompletableFuture<Boolean> setBlockStateId(int localX, int worldY, int localZ, int stateId);

    /**
     * {@return the block light level at a position of this chunk, between {@code 0} and {@code 15}}
     *
     * @param localX the X coordinate inside the chunk
     * @param worldY the absolute Y coordinate
     * @param localZ the Z coordinate inside the chunk
     * @since 0.1.0
     */
    int blockLight(int localX, int worldY, int localZ);

    /**
     * {@return the sky light level at a position of this chunk, between {@code 0} and {@code 15}}
     *
     * @param localX the X coordinate inside the chunk
     * @param worldY the absolute Y coordinate
     * @param localZ the Z coordinate inside the chunk
     * @since 0.1.0
     */
    int skyLight(int localX, int worldY, int localZ);

    /**
     * {@return the effective light level at a position of this chunk, the highest of block and sky light}
     *
     * @param localX the X coordinate inside the chunk
     * @param worldY the absolute Y coordinate
     * @param localZ the Z coordinate inside the chunk
     * @since 0.1.0
     */
    int lightLevel(int localX, int worldY, int localZ);
}
