package fr.fidorial.world.block.data;

import fr.fidorial.world.block.BlockData;

/**
 * A block state carrying the {@code lit} property: whether the block glows, like a furnace in use.
 *
 * <p>Block states are immutable: {@code with...} methods return another state and leave this one
 * untouched.</p>
 *
 * @since 0.1.0
 */
public interface Lightable extends BlockData {

    /**
     * {@return {@code true} if the block is lit}
     *
     * @since 0.1.0
     */
    default boolean isLit() {
        return Boolean.parseBoolean(get("lit"));
    }

    /**
     * {@return the same block with another {@code lit} value}
     *
     * @param lit {@code true} to light the block
     * @since 0.1.0
     */
    default Lightable withLit(final boolean lit) {
        return (Lightable) with("lit", String.valueOf(lit));
    }
}
