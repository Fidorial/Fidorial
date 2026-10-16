package fr.fidorial.world.block.data.type;

import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.data.Waterlogged;

import java.util.Locale;

/**
 * The block state of one piece of a speleothem: pointed dripstone, a sulfur spike or an icicle.
 *
 * <p>A speleothem is a column of such pieces, each one knowing which way the column points and
 * how thick it is at that height.</p>
 *
 * @since 0.1.0
 */
public interface Speleothem extends Waterlogged {

    @Override
    default Speleothem withWaterlogged(final boolean waterlogged) {
        return (Speleothem) Waterlogged.super.withWaterlogged(waterlogged);
    }

    /**
     * {@return the way the tip points: {@link BlockFace#UP} or {@link BlockFace#DOWN}}
     *
     * @since 0.1.0
     */
    default BlockFace getVerticalDirection() {
        return BlockFace.valueOf(get("vertical_direction").toUpperCase(Locale.ROOT));
    }

    /**
     * {@return the same piece pointing the other way}
     *
     * @param direction {@link BlockFace#UP} or {@link BlockFace#DOWN}
     * @throws IllegalArgumentException if the direction is not vertical
     * @since 0.1.0
     */
    default Speleothem withVerticalDirection(final BlockFace direction) {
        if (direction != BlockFace.UP && direction != BlockFace.DOWN) {
            throw new IllegalArgumentException("A speleothem points up or down, not " + direction);
        }
        return (Speleothem) with("vertical_direction", direction.name().toLowerCase(Locale.ROOT));
    }

    /**
     * {@return how thick the column is at this piece}
     *
     * @since 0.1.0
     */
    default Thickness getThickness() {
        return Thickness.fromValue(get("thickness"));
    }

    /**
     * {@return the same piece with another thickness}
     *
     * @param thickness the thickness of the column at this piece
     * @since 0.1.0
     */
    default Speleothem withThickness(final Thickness thickness) {
        return (Speleothem) with("thickness", thickness.value());
    }

    /**
     * The shapes a piece of speleothem takes, from its tip to its base.
     *
     * @since 0.1.0
     */
    enum Thickness {
        /**
         * Stored as {@code tip_merge}: a tip touching the tip of the opposite column.
         */
        TIP_MERGE("tip_merge"),
        /**
         * Stored as {@code tip}.
         */
        TIP("tip"),
        /**
         * Stored as {@code frustum}: the piece right behind the tip.
         */
        FRUSTUM("frustum"),
        /**
         * Stored as {@code middle}.
         */
        MIDDLE("middle"),
        /**
         * Stored as {@code base}: the piece held by the block the column grows from.
         */
        BASE("base");

        private final String value;

        Thickness(final String value) {
            this.value = value;
        }

        /**
         * {@return the value of this enum stored as the given string}
         *
         * @param value the value as stored in the block state
         * @throws IllegalArgumentException if the value is unknown
         * @since 0.1.0
         */
        public static Thickness fromValue(final String value) {
            for (final Thickness thickness : values()) {
                if (thickness.value.equals(value)) {
                    return thickness;
                }
            }
            throw new IllegalArgumentException("Unknown speleothem thickness: " + value);
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
