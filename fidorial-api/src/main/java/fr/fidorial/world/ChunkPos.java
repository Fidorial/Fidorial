package fr.fidorial.world;

/**
 * The coordinates of a 16x16 chunk column, independent of any world.
 *
 * @param x the chunk X coordinate, that is the block X coordinate shifted right by four
 * @param z the chunk Z coordinate, that is the block Z coordinate shifted right by four
 * @since 0.1.0
 */
public record ChunkPos(int x, int z) {

    /**
     * {@return the position of the chunk holding the given block coordinates}
     *
     * @param blockX the block X coordinate
     * @param blockZ the block Z coordinate
     * @since 0.1.0
     */
    public static ChunkPos fromBlock(final int blockX, final int blockZ) {
        return new ChunkPos(blockX >> 4, blockZ >> 4);
    }

    /**
     * {@return the position packed into a single {@code long}, X in the high bits and Z in the low bits}
     *
     * @param pos the chunk position
     * @since 0.1.0
     */
    public static long chunkKey(final ChunkPos pos) {
        return chunkKey(pos.x(), pos.z());
    }

    /**
     * {@return the coordinates packed into a single {@code long}, X in the high bits and Z in the low bits}
     *
     * @param chunkX the chunk X coordinate
     * @param chunkZ the chunk Z coordinate
     * @since 0.1.0
     */
    public static long chunkKey(final int chunkX, final int chunkZ) {
        return ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
    }

    /**
     * {@return this position packed into a single {@code long}}
     *
     * @see #chunkKey(int, int)
     * @since 0.1.0
     */
    public long key() {
        return chunkKey(x, z);
    }

    /**
     * {@return the position unpacked from a {@code long} built by {@link #chunkKey(int, int)}}
     *
     * @param key the packed position
     * @since 0.1.0
     */
    public static ChunkPos fromKey(final long key) {
        return new ChunkPos((int) (key >> 32), (int) key);
    }
}
