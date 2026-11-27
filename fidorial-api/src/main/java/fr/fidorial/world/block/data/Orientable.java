package fr.fidorial.world.block.data;

import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockProperties.Names;
import fr.fidorial.world.block.BlockTrait;

/**
 * A block state carrying the {@code axis} property: the axis the block is aligned on, like logs and pillars.
 *
 * <p>Block states are immutable: {@code with...} methods return another state and leave this one
 * untouched.</p>
 *
 * @since 0.1.0
 */
@BlockTrait(values = @BlockTrait.Values(property = Names.AXIS, allowed = {"x", "y", "z"}))
public interface Orientable extends BlockData {

    /**
     * {@return the axis the block is aligned on}
     *
     * @since 0.1.0
     */
    default Axis getAxis() {
        return get(type().choice(Names.AXIS, Axis.class));
    }

    /**
     * {@return the same block with another {@code axis} value}
     *
     * @param axis the axis to align on
     * @since 0.1.0
     */
    default Orientable withAxis(final Axis axis) {
        return (Orientable) with(type().choice(Names.AXIS, Axis.class), axis);
    }

    /**
     * The three axes of the world.
     *
     * @since 0.1.0
     */
    enum Axis {
        /**
         * The X axis.
         */
        X,
        /**
         * The Y axis.
         */
        Y,
        /**
         * The Z axis.
         */
        Z
    }
}
