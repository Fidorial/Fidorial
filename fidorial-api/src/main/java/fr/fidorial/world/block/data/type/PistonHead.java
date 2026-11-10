package fr.fidorial.world.block.data.type;

import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockTrait;

/**
 * The block state of the head of an extended piston. {@link #getFacing()} points away from the
 * base, which is one block behind the head.
 *
 * @since 0.1.0
 */
@BlockTrait(properties = "short")
public interface PistonHead extends TechnicalPiston {

    @Override
    default PistonHead withFacing(final BlockFace facing) {
        return (PistonHead) TechnicalPiston.super.withFacing(facing);
    }

    @Override
    default PistonHead withSticky(final boolean sticky) {
        return (PistonHead) TechnicalPiston.super.withSticky(sticky);
    }

    /**
     * {@return {@code true} if the arm of the head is the short one, drawn while the head moves}
     *
     * @since 0.1.0
     */
    default boolean isShort() {
        return Boolean.parseBoolean(get("short"));
    }

    /**
     * {@return the same head with a short arm or a long one}
     *
     * @param shortArm {@code true} for the short arm
     * @since 0.1.0
     */
    default PistonHead withShort(final boolean shortArm) {
        return (PistonHead) with("short", String.valueOf(shortArm));
    }
}
