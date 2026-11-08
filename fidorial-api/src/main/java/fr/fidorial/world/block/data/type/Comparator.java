package fr.fidorial.world.block.data.type;

import com.google.common.base.Preconditions;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockTrait;
import fr.fidorial.world.block.data.Directional;
import fr.fidorial.world.block.data.Powerable;

/**
 * The block state of a comparator: its facing, its mode, and whether it is on.
 * {@link #getFacing()} points towards its input; it powers the block on the other side.
 *
 * @since 0.1.0
 */
@BlockTrait(values = @BlockTrait.Values(property = "mode", allowed = {"compare", "subtract"}, exactly = true))
public interface Comparator extends Directional, Powerable {

    @Override
    default Comparator withFacing(final BlockFace facing) {
        return (Comparator) Directional.super.withFacing(facing);
    }

    @Override
    default Comparator withPowered(final boolean powered) {
        return (Comparator) Powerable.super.withPowered(powered);
    }

    /**
     * {@return how the comparator combines its rear and side inputs}
     *
     * @since 0.1.0
     */
    default Mode getMode() {
        final String mode = get("mode");
        Preconditions.checkState(mode != null, "%s has no mode property", key());
        return Mode.fromValue(mode);
    }

    /**
     * {@return the same comparator in another mode}
     *
     * @param mode the mode
     * @since 0.1.0
     */
    default Comparator withMode(final Mode mode) {
        return (Comparator) with("mode", mode.value());
    }

    /**
     * How a comparator combines its inputs.
     *
     * @since 0.1.0
     */
    enum Mode {
        /**
         * Stored as {@code compare}: outputs the rear input, unless a side input is stronger.
         */
        COMPARE("compare"),
        /**
         * Stored as {@code subtract}: outputs the rear input minus the strongest side input.
         */
        SUBTRACT("subtract");

        private final String value;

        Mode(final String value) {
            this.value = value;
        }

        /**
         * {@return the value of this enum stored as the given string}
         *
         * @param value the value as stored in the block state
         * @throws IllegalArgumentException if the value is unknown
         * @since 0.1.0
         */
        public static Mode fromValue(final String value) {
            for (final Mode mode : values()) {
                if (mode.value.equals(value)) {
                    return mode;
                }
            }
            throw new IllegalArgumentException("Unknown comparator mode: " + value);
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
