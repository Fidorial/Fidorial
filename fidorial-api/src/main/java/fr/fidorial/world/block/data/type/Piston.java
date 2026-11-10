package fr.fidorial.world.block.data.type;

import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockTrait;
import fr.fidorial.world.block.data.Directional;

/**
 * The block state of the base of a piston, sticky or not: the side its head comes out of, and
 * whether the head is out.
 *
 * <p>An extended base is one block of a two block structure, the other being its
 * {@link PistonHead}, one block further along {@link #getFacing()}.</p>
 *
 * @since 0.1.0
 */
@BlockTrait(properties = "extended")
public interface Piston extends Directional {

    @Override
    default Piston withFacing(final BlockFace facing) {
        return (Piston) Directional.super.withFacing(facing);
    }

    /**
     * {@return {@code true} if the head of the piston is out}
     *
     * @since 0.1.0
     */
    default boolean isExtended() {
        return Boolean.parseBoolean(get("extended"));
    }

    /**
     * {@return the same piston, extended or not}
     *
     * @param extended {@code true} for a piston whose head is out
     * @since 0.1.0
     */
    default Piston withExtended(final boolean extended) {
        return (Piston) with("extended", String.valueOf(extended));
    }
}
