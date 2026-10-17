package fr.fidorial.math;

import fr.fidorial.world.ChunkPos;
import org.jetbrains.annotations.Contract;

/**
 * An immutable pair of block X and Z coordinates, pointing at a column of blocks.
 *
 * @see #column(int, int)
 * @since 0.1.0
 */
public sealed interface ColumnPosition permits ColumnPositionImpl {
    /**
     * Creates a column position at the given coordinates.
     *
     * @param x the block X coordinate
     * @param z the block Z coordinate
     * @return a new column position at the given coordinates
     * @since 0.1.0
     */
    @Contract(value = "_, _ -> new", pure = true)
    static ColumnPosition column(final int x, final int z) {
        return new ColumnPositionImpl(x, z);
    }

    /**
     * Gets the X coordinate of this column.
     *
     * @return the block X coordinate
     * @since 0.1.0
     */
    @Contract(pure = true)
    int x();

    /**
     * Gets the Z coordinate of this column.
     *
     * @return the block Z coordinate
     * @since 0.1.0
     */
    @Contract(pure = true)
    int z();

    /**
     * Moves this column along the X axis.
     *
     * @param x the amount to add to the X coordinate
     * @return a new column with the given amount added to the X coordinate
     * @since 0.1.0
     */
    @Contract(value = "_ -> new", pure = true)
    ColumnPosition offsetX(int x);

    /**
     * Moves this column along the Z axis.
     *
     * @param z the amount to add to the Z coordinate
     * @return a new column with the given amount added to the Z coordinate
     * @since 0.1.0
     */
    @Contract(value = "_ -> new", pure = true)
    ColumnPosition offsetZ(int z);

    /**
     * Moves this column along both axes.
     *
     * @param x the amount to add to the X coordinate
     * @param z the amount to add to the Z coordinate
     * @return a new column with the given amounts added to each coordinate
     * @since 0.1.0
     */
    @Contract(value = "_, _ -> new", pure = true)
    ColumnPosition offset(int x, int z);

    /**
     * Copies this column with a different X coordinate.
     *
     * @param x the new X coordinate
     * @return a new column with the X coordinate replaced
     * @since 0.1.0
     */
    @Contract(value = "_ -> new", pure = true)
    ColumnPosition withX(int x);

    /**
     * Copies this column with a different Z coordinate.
     *
     * @param z the new Z coordinate
     * @return a new column with the Z coordinate replaced
     * @since 0.1.0
     */
    @Contract(value = "_ -> new", pure = true)
    ColumnPosition withZ(int z);

    /**
     * Copies this column with different coordinates.
     *
     * @param x the new X coordinate
     * @param z the new Z coordinate
     * @return a new column with both coordinates replaced
     * @since 0.1.0
     */
    @Contract(value = "_, _ -> new", pure = true)
    ColumnPosition with(int x, int z);

    /**
     * Gets the block of this column at the given height.
     *
     * @param y the block Y coordinate
     * @return a new block position in this column at the given height
     * @since 0.1.0
     */
    @Contract(value = "_ -> new", pure = true)
    default BlockPosition atY(final int y) {
        return Position.block(x(), y, z());
    }

    /**
     * Gets the position of the chunk containing this column.
     *
     * @return the position of the chunk holding this column
     * @since 0.1.0
     */
    @Contract(value = " -> new", pure = true)
    default ChunkPos chunk() {
        return ChunkPos.fromBlock(x(), z());
    }

    /**
     * Gets the X coordinate of the chunk containing this column.
     *
     * @return the X coordinate of the chunk holding this column
     * @since 0.1.0
     */
    @Contract(pure = true)
    default int chunkX() {
        return x() >> 4;
    }

    /**
     * Gets the Z coordinate of the chunk containing this column.
     *
     * @return the Z coordinate of the chunk holding this column
     * @since 0.1.0
     */
    @Contract(pure = true)
    default int chunkZ() {
        return z() >> 4;
    }
}
