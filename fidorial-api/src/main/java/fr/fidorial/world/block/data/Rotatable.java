package fr.fidorial.world.block.data;

import fr.fidorial.world.block.BlockData;

/**
 * A block state carrying the {@code rotation} property: the sixteen-step orientation of signs, banners and heads.
 *
 * <p>Block states are immutable: {@code with...} methods return another state and leave this one
 * untouched.</p>
 *
 * @since 0.1.0
 */
public interface Rotatable extends BlockData {

    /**
     * {@return the rotation, from {@code 0} (south) to {@code 15}, clockwise}
     *
     * @since 0.1.0
     */
    default int getRotation() {
        return Integer.parseInt(get("rotation"));
    }

    /**
     * {@return the same block with another {@code rotation} value}
     *
     * @param rotation the rotation, from {@code 0} to {@code 15}
     * @since 0.1.0
     */
    default Rotatable withRotation(final int rotation) {
        return (Rotatable) with("rotation", String.valueOf(rotation));
    }
}
