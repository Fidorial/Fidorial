package fr.fidorial.world.block.data;

import fr.fidorial.world.block.BlockData;

/**
 * A block state carrying the {@code snowy} property: whether snow covers the block, like grass and podzol.
 *
 * <p>Block states are immutable: {@code with...} methods return another state and leave this one
 * untouched.</p>
 *
 * @since 0.1.0
 */
public interface Snowable extends BlockData {

    /**
     * {@return {@code true} if the block is covered with snow}
     *
     * @since 0.1.0
     */
    default boolean isSnowy() {
        return Boolean.parseBoolean(get("snowy"));
    }

    /**
     * {@return the same block with another {@code snowy} value}
     *
     * @param snowy {@code true} to cover the block with snow
     * @since 0.1.0
     */
    default Snowable withSnowy(final boolean snowy) {
        return (Snowable) with("snowy", String.valueOf(snowy));
    }
}
