package fr.fidorial.world.block.data;

import fr.fidorial.world.block.BlockData;

/**
 * A block state carrying the {@code waterlogged} property: whether the block holds water.
 *
 * <p>Block states are immutable: {@code with...} methods return another state and leave this one
 * untouched.</p>
 *
 * @since 0.1.0
 */
public interface Waterlogged extends BlockData {

    /**
     * {@return {@code true} if the block holds water}
     *
     * @since 0.1.0
     */
    default boolean isWaterlogged() {
        return Boolean.parseBoolean(get("waterlogged"));
    }

    /**
     * {@return the same block with another {@code waterlogged} value}
     *
     * @param waterlogged {@code true} to fill the block with water
     * @since 0.1.0
     */
    default Waterlogged withWaterlogged(final boolean waterlogged) {
        return (Waterlogged) with("waterlogged", String.valueOf(waterlogged));
    }
}
