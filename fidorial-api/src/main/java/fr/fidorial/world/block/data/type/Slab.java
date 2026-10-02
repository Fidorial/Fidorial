package fr.fidorial.world.block.data.type;

import fr.fidorial.world.block.data.Waterlogged;

/**
 * The block state of a slab: which half it fills, or both, and whether it holds water.
 *
 * @since 0.1.0
 */
public interface Slab extends Waterlogged {

    @Override
    default Slab withWaterlogged(final boolean waterlogged) {
        return (Slab) Waterlogged.super.withWaterlogged(waterlogged);
    }

    /**
     * {@return which part of the block the slab fills}
     *
     * @since 0.1.0
     */
    default Type getType() {
        return Type.fromValue(get("type"));
    }

    /**
     * {@return the same slab filling another part of the block}
     *
     * @param type the part to fill
     * @since 0.1.0
     */
    default Slab withType(final Type type) {
        return (Slab) with("type", type.value());
    }

    /**
     * The part of a block a slab fills.
     *
     * @since 0.1.0
     */
    enum Type {
        /**
         * Stored as {@code top}.
         */
        TOP("top"),
        /**
         * Stored as {@code bottom}.
         */
        BOTTOM("bottom"),
        /**
         * Stored as {@code double}.
         */
        DOUBLE("double");

        private final String value;

        Type(final String value) {
            this.value = value;
        }

        /**
         * {@return the value of this enum stored as the given string}
         *
         * @param value the value as stored in the block state
         * @throws IllegalArgumentException if the value is unknown
         * @since 0.1.0
         */
        public static Type fromValue(final String value) {
            for (final Type type : values()) {
                if (type.value.equals(value)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown slab type: " + value);
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
