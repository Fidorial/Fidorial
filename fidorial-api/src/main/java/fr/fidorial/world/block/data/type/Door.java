package fr.fidorial.world.block.data.type;

import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.data.Bisected;
import fr.fidorial.world.block.data.Directional;
import fr.fidorial.world.block.data.Openable;
import fr.fidorial.world.block.data.Powerable;

import java.util.Locale;

/**
 * The block state of a door: its facing, half, hinge side, and whether it is open or powered.
 *
 * @since 0.1.0
 */
public interface Door extends Directional, Bisected, Openable, Powerable {

    @Override
    default Door withFacing(final BlockFace facing) {
        return (Door) Directional.super.withFacing(facing);
    }

    @Override
    default Door withHalf(final Half half) {
        return (Door) Bisected.super.withHalf(half);
    }

    @Override
    default Door withOpen(final boolean open) {
        return (Door) Openable.super.withOpen(open);
    }

    @Override
    default Door withPowered(final boolean powered) {
        return (Door) Powerable.super.withPowered(powered);
    }

    /**
     * {@return the side the door is hinged on}
     *
     * @since 0.1.0
     */
    default Hinge getHinge() {
        return Hinge.valueOf(get("hinge").toUpperCase(Locale.ROOT));
    }

    /**
     * {@return the same door hinged on another side}
     *
     * @param hinge the side to hinge the door on
     * @since 0.1.0
     */
    default Door withHinge(final Hinge hinge) {
        return (Door) with("hinge", hinge.name().toLowerCase(Locale.ROOT));
    }

    /**
     * The side a door is hinged on.
     *
     * @since 0.1.0
     */
    enum Hinge {
        /**
         * Hinged on the left.
         */
        LEFT,
        /**
         * Hinged on the right.
         */
        RIGHT
    }
}
