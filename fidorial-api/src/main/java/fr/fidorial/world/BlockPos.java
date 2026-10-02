package fr.fidorial.world;

/**
 * The integer coordinates of a block, independent of any world.
 *
 * @param x the block X coordinate
 * @param y the block Y coordinate
 * @param z the block Z coordinate
 * @since 0.1.0
 */
public record BlockPos(int x, int y, int z) {

    /**
     * {@return the position of the neighbouring block on the given side}
     *
     * @param face the side to step towards
     * @since 0.1.0
     */
    public BlockPos relative(final BlockFace face) {
        return new BlockPos(x + face.dx(), y + face.dy(), z + face.dz());
    }

    /**
     * {@return this position moved by the given amounts}
     *
     * @param dx the offset on the X axis
     * @param dy the offset on the Y axis
     * @param dz the offset on the Z axis
     * @since 0.1.0
     */
    public BlockPos offset(final int dx, final int dy, final int dz) {
        return new BlockPos(x + dx, y + dy, z + dz);
    }

    /**
     * {@return the X coordinate of the chunk holding this block}
     *
     * @since 0.1.0
     */
    public int chunkX() {
        return x >> 4;
    }

    /**
     * {@return the Z coordinate of the chunk holding this block}
     *
     * @since 0.1.0
     */
    public int chunkZ() {
        return z >> 4;
    }

    /**
     * {@return the X coordinate of this block inside its chunk, {@code 0} to {@code 15}}
     *
     * @since 0.1.0
     */
    public int localX() {
        return x & 15;
    }

    /**
     * {@return the Z coordinate of this block inside its chunk, {@code 0} to {@code 15}}
     *
     * @since 0.1.0
     */
    public int localZ() {
        return z & 15;
    }

    /**
     * {@return the position of the chunk holding this block}
     *
     * @since 0.1.0
     */
    public ChunkPos chunk() {
        return new ChunkPos(chunkX(), chunkZ());
    }
}
