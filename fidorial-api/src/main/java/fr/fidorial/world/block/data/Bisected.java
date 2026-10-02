package fr.fidorial.world.block.data;

import fr.fidorial.world.block.BlockData;

/**
 * A block state carrying the {@code half} property: which half of a two-part block this state is.
 *
 * <p>Block states are immutable: {@code with...} methods return another state and leave this one
 * untouched.</p>
 *
 * @since 0.1.0
 */
public interface Bisected extends BlockData {

    /**
     * {@return the half this state represents}
     *
     * @since 0.1.0
     */
    default Half getHalf() {
        return Half.fromValue(get("half"));
    }

    /**
     * {@return the same block with another {@code half} value}
     *
     * @param half the half to represent; must be one of the values the block accepts
     * @since 0.1.0
     */
    default Bisected withHalf(final Half half) {
        return (Bisected) with("half", half.value());
    }

    /**
     * The halves a two-part block may consist of.
     *
     * <p>Slabs, stairs and trapdoors use {@code TOP} and {@code BOTTOM}; doors and tall plants use
     * {@code UPPER} and {@code LOWER}.</p>
     *
     * @since 0.1.0
     */
    enum Half {
        /**
         * Stored as {@code top}.
         */
        TOP("top"),
        /**
         * Stored as {@code bottom}.
         */
        BOTTOM("bottom"),
        /**
         * Stored as {@code upper}.
         */
        UPPER("upper"),
        /**
         * Stored as {@code lower}.
         */
        LOWER("lower");

        private final String value;

        Half(final String value) {
            this.value = value;
        }

        /**
         * {@return the value of this enum stored as the given string}
         *
         * @param value the value as stored in the block state
         * @throws IllegalArgumentException if the value is unknown
         * @since 0.1.0
         */
        public static Half fromValue(final String value) {
            for (final Half half : values()) {
                if (half.value.equals(value)) {
                    return half;
                }
            }
            throw new IllegalArgumentException("Unknown half: " + value);
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
