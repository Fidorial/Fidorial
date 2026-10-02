package fr.fidorial.world.block.data;

import fr.fidorial.world.block.BlockData;

/**
 * A block state carrying the {@code powered} property: whether the block receives redstone power.
 *
 * <p>Block states are immutable: {@code with...} methods return another state and leave this one
 * untouched.</p>
 *
 * @since 0.1.0
 */
public interface Powerable extends BlockData {

    /**
     * {@return {@code true} if the block is powered}
     *
     * @since 0.1.0
     */
    default boolean isPowered() {
        return Boolean.parseBoolean(get("powered"));
    }

    /**
     * {@return the same block with another {@code powered} value}
     *
     * @param powered {@code true} to power the block
     * @since 0.1.0
     */
    default Powerable withPowered(final boolean powered) {
        return (Powerable) with("powered", String.valueOf(powered));
    }
}
