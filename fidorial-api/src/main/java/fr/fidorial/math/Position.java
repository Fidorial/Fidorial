package fr.fidorial.math;

import fr.fidorial.world.BlockFace;
import fr.fidorial.world.ChunkPos;
import fr.fidorial.world.World;
import org.jetbrains.annotations.Contract;

/**
 * An immutable set of X, Y and Z coordinates.
 *
 * @since 0.1.0
 */
public sealed interface Position permits BlockPosition, FinePosition {
    /**
     * Creates a fine position at the given coordinates.
     *
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param z the Z coordinate
     * @return a new fine position at the given coordinates
     * @since 0.1.0
     */
    @Contract(value = "_, _, _ -> new", pure = true)
    static FinePosition fine(final double x, final double y, final double z) {
        return new FinePositionImpl(x, y, z);
    }

    /**
     * Creates a block position at the given coordinates.
     *
     * @param x the block X coordinate
     * @param y the block Y coordinate
     * @param z the block Z coordinate
     * @return a new block position at the given coordinates
     * @since 0.1.0
     */
    @Contract(value = "_, _, _ -> new", pure = true)
    static BlockPosition block(final int x, final int y, final int z) {
        return new BlockPositionImpl(x, y, z);
    }

    /**
     * Gets the X coordinate of this position.
     *
     * @return the X coordinate
     * @since 0.1.0
     */
    @Contract(pure = true)
    double x();

    /**
     * Gets the Y coordinate of this position.
     *
     * @return the Y coordinate
     * @since 0.1.0
     */
    @Contract(pure = true)
    double y();

    /**
     * Gets the Z coordinate of this position.
     *
     * @return the Z coordinate
     * @since 0.1.0
     */
    @Contract(pure = true)
    double z();

    /**
     * Moves this position by the defined distance in the direction of the given face.
     *
     * @param face     the direction to move towards
     * @param distance the offset distance
     * @return a new position moved by one block towards the given face
     * @since 0.1.0
     */
    @Contract(value = "_, _ -> new", pure = true)
    Position relative(BlockFace face, int distance);

    /**
     * Moves this position by one block in the direction of the given face.
     *
     * @param face the direction to move towards
     * @return a new position moved by one block towards the given face
     * @see #relative(BlockFace, int)
     * @since 0.1.0
     */
    @Contract(value = "_ -> new", pure = true)
    Position relative(BlockFace face);

    /**
     * Moves this position along the X axis.
     *
     * @param x the amount to add to the X coordinate
     * @return a new position with the given amount added to the X coordinate
     * @since 0.1.0
     */
    @Contract(value = "_ -> new", pure = true)
    FinePosition offsetX(double x);

    /**
     * Moves this position along the Y axis.
     *
     * @param y the amount to add to the Y coordinate
     * @return a new position with the given amount added to the Y coordinate
     * @since 0.1.0
     */
    @Contract(value = "_ -> new", pure = true)
    FinePosition offsetY(double y);

    /**
     * Moves this position along the Z axis.
     *
     * @param z the amount to add to the Z coordinate
     * @return a new position with the given amount added to the Z coordinate
     * @since 0.1.0
     */
    @Contract(value = "_ -> new", pure = true)
    FinePosition offsetZ(double z);

    /**
     * Moves this position along all three axes.
     *
     * @param x the amount to add to the X coordinate
     * @param y the amount to add to the Y coordinate
     * @param z the amount to add to the Z coordinate
     * @return a new position with the given amounts added to each coordinate
     * @since 0.1.0
     */
    @Contract(value = "_, _, _ -> new", pure = true)
    FinePosition offset(double x, double y, double z);

    /**
     * Moves this position along the X axis by a whole number of blocks.
     *
     * @param x the amount to add to the X coordinate
     * @return a new position of the same kind with the given amount added to the X coordinate
     * @since 0.1.0
     */
    @Contract(value = "_ -> new", pure = true)
    Position offsetX(int x);

    /**
     * Moves this position along the Y axis by a whole number of blocks.
     *
     * @param y the amount to add to the Y coordinate
     * @return a new position of the same kind with the given amount added to the Y coordinate
     * @since 0.1.0
     */
    @Contract(value = "_ -> new", pure = true)
    Position offsetY(int y);

    /**
     * Moves this position along the Z axis by a whole number of blocks.
     *
     * @param z the amount to add to the Z coordinate
     * @return a new position of the same kind with the given amount added to the Z coordinate
     * @since 0.1.0
     */
    @Contract(value = "_ -> new", pure = true)
    Position offsetZ(int z);

    /**
     * Moves this position along all three axes by a whole number of blocks.
     *
     * @param x the amount to add to the X coordinate
     * @param y the amount to add to the Y coordinate
     * @param z the amount to add to the Z coordinate
     * @return a new position of the same kind with the given amounts added to each coordinate
     * @since 0.1.0
     */
    @Contract(value = "_, _, _ -> new", pure = true)
    Position offset(int x, int y, int z);

    /**
     * Copies this position with a different X coordinate.
     *
     * @param x the new X coordinate
     * @return a new position with the X coordinate replaced
     * @since 0.1.0
     */
    @Contract(value = "_ -> new", pure = true)
    FinePosition withX(double x);

    /**
     * Copies this position with a different Y coordinate.
     *
     * @param y the new Y coordinate
     * @return a new position with the Y coordinate replaced
     * @since 0.1.0
     */
    @Contract(value = "_ -> new", pure = true)
    FinePosition withY(double y);

    /**
     * Copies this position with a different Z coordinate.
     *
     * @param z the new Z coordinate
     * @return a new position with the Z coordinate replaced
     * @since 0.1.0
     */
    @Contract(value = "_ -> new", pure = true)
    FinePosition withZ(double z);

    /**
     * Copies this position with different coordinates.
     *
     * @param x the new X coordinate
     * @param y the new Y coordinate
     * @param z the new Z coordinate
     * @return a new position with all coordinates replaced
     * @since 0.1.0
     */
    @Contract(value = "_, _, _ -> new", pure = true)
    FinePosition with(double x, double y, double z);

    /**
     * Copies this position with a different whole X coordinate.
     *
     * @param x the new X coordinate
     * @return a new position of the same kind with the X coordinate replaced
     * @since 0.1.0
     */
    @Contract(value = "_ -> new", pure = true)
    Position withX(int x);

    /**
     * Copies this position with a different whole Y coordinate.
     *
     * @param y the new Y coordinate
     * @return a new position of the same kind with the Y coordinate replaced
     * @since 0.1.0
     */
    @Contract(value = "_ -> new", pure = true)
    Position withY(int y);

    /**
     * Copies this position with a different whole Z coordinate.
     *
     * @param z the new Z coordinate
     * @return a new position of the same kind with the Z coordinate replaced
     * @since 0.1.0
     */
    @Contract(value = "_ -> new", pure = true)
    Position withZ(int z);

    /**
     * Copies this position with different whole coordinates.
     *
     * @param x the new X coordinate
     * @param y the new Y coordinate
     * @param z the new Z coordinate
     * @return a new position of the same kind with all coordinates replaced
     * @since 0.1.0
     */
    @Contract(value = "_, _, _ -> new", pure = true)
    Position with(int x, int y, int z);

    /**
     * Gets the X coordinate of the block containing this position.
     *
     * @return the X coordinate of the block holding this position, rounded towards negative infinity
     * @since 0.1.0
     */
    @Contract(pure = true)
    default int blockX() {
        return (int) Math.floor(x());
    }

    /**
     * Gets the Y coordinate of the block containing this position.
     *
     * @return the Y coordinate of the block holding this position, rounded towards negative infinity
     * @since 0.1.0
     */
    @Contract(pure = true)
    default int blockY() {
        return (int) Math.floor(y());
    }

    /**
     * Gets the Z coordinate of the block containing this position.
     *
     * @return the Z coordinate of the block holding this position, rounded towards negative infinity
     * @since 0.1.0
     */
    @Contract(pure = true)
    default int blockZ() {
        return (int) Math.floor(z());
    }

    /**
     * Gets the position of the chunk containing this position.
     *
     * @return the position of the chunk holding this block
     * @since 0.1.0
     */
    @Contract(value = " -> new", pure = true)
    default ChunkPos chunk() {
        return ChunkPos.fromBlock(blockX(), blockZ());
    }

    /**
     * Gets the X coordinate of the chunk containing this position.
     *
     * @return the X coordinate of the chunk holding this block
     * @since 0.1.0
     */
    @Contract(pure = true)
    default int chunkX() {
        return blockX() >> 4;
    }

    /**
     * Gets the Z coordinate of the chunk containing this position.
     *
     * @return the Z coordinate of the chunk holding this block
     * @since 0.1.0
     */
    @Contract(pure = true)
    default int chunkZ() {
        return blockZ() >> 4;
    }

    /**
     * Gets the X coordinate of this position relative to the chunk containing it.
     *
     * @return the X coordinate of this block inside its chunk, {@code 0} to {@code 15}
     * @since 0.1.0
     */
    @Contract(pure = true)
    default int localX() {
        return blockX() & 15;
    }

    /**
     * Gets the Z coordinate of this position relative to the chunk containing it.
     *
     * @return the Z coordinate of this block inside its chunk, {@code 0} to {@code 15}
     * @since 0.1.0
     */
    @Contract(pure = true)
    default int localZ() {
        return blockZ() & 15;
    }

    /**
     * Computes the squared distance between this position and another.
     *
     * @param other the position to measure the distance to
     * @return the squared euclidean distance between this position and another
     * @since 0.1.0
     */
    @Contract(pure = true)
    default double distanceSquared(final Position other) {
        final double dx = x() - other.x();
        final double dy = y() - other.y();
        final double dz = z() - other.z();
        return dx * dx + dy * dy + dz * dz;
    }

    /**
     * Places this position in a world.
     *
     * @param world the world of the location
     * @return a new location at these coordinates in the given world
     * @since 0.1.0
     */
    @Contract(value = "_ -> new", pure = true)
    Location toLocation(World world);
}
