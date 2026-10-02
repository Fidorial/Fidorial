package fr.fidorial.world.block.data;

import fr.fidorial.world.block.BlockData;

/**
 * A block state carrying the {@code open} property: whether the block is open, like doors, trapdoors and gates.
 *
 * <p>Block states are immutable: {@code with...} methods return another state and leave this one
 * untouched.</p>
 *
 * @since 0.1.0
 */
public interface Openable extends BlockData {

    /**
     * {@return {@code true} if the block is open}
     *
     * @since 0.1.0
     */
    default boolean isOpen() {
        return Boolean.parseBoolean(get("open"));
    }

    /**
     * {@return the same block with another {@code open} value}
     *
     * @param open {@code true} to open the block
     * @since 0.1.0
     */
    default Openable withOpen(final boolean open) {
        return (Openable) with("open", String.valueOf(open));
    }
}
