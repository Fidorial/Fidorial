package fr.fidorial.world.block.data.type;

import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.data.Bisected;
import fr.fidorial.world.block.data.Directional;
import fr.fidorial.world.block.data.Waterlogged;

/**
 * The block state of stairs: their facing, half, shape, and whether they hold water.
 *
 * @since 0.1.0
 */
public interface Stairs extends Directional, Bisected, Waterlogged {

    @Override
    default Stairs withFacing(final BlockFace facing) {
        return (Stairs) Directional.super.withFacing(facing);
    }

    @Override
    default Stairs withHalf(final Half half) {
        return (Stairs) Bisected.super.withHalf(half);
    }

    @Override
    default Stairs withWaterlogged(final boolean waterlogged) {
        return (Stairs) Waterlogged.super.withWaterlogged(waterlogged);
    }

    /**
     * {@return how the stairs connect to their neighbours}
     *
     * @since 0.1.0
     */
    default Shape getShape() {
        return Shape.fromValue(get("shape"));
    }

    /**
     * {@return the same stairs with another shape}
     *
     * @param shape the shape to take
     * @since 0.1.0
     */
    default Stairs withShape(final Shape shape) {
        return (Stairs) with("shape", shape.value());
    }

    /**
     * How stairs connect to their neighbours.
     *
     * @since 0.1.0
     */
    enum Shape {
        /**
         * A straight run.
         */
        STRAIGHT("straight"),
        /**
         * An inner corner turning left.
         */
        INNER_LEFT("inner_left"),
        /**
         * An inner corner turning right.
         */
        INNER_RIGHT("inner_right"),
        /**
         * An outer corner turning left.
         */
        OUTER_LEFT("outer_left"),
        /**
         * An outer corner turning right.
         */
        OUTER_RIGHT("outer_right");

        private final String value;

        Shape(final String value) {
            this.value = value;
        }

        /**
         * {@return the value of this enum stored as the given string}
         *
         * @param value the value as stored in the block state
         * @throws IllegalArgumentException if the value is unknown
         * @since 0.1.0
         */
        public static Shape fromValue(final String value) {
            for (final Shape shape : values()) {
                if (shape.value.equals(value)) {
                    return shape;
                }
            }
            throw new IllegalArgumentException("Unknown stair shape: " + value);
        }

        /**
         * {@return the value as stored in the block state}
         *
         * @since 0.1.0
         */
        public String value() {
            return value;
        }
    }
}
