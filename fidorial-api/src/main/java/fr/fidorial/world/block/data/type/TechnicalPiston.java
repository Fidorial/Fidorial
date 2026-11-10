package fr.fidorial.world.block.data.type;

import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockTrait;
import fr.fidorial.world.block.data.Directional;

/**
 * The block state of the parts of a piston that no item places: its {@linkplain PistonHead head},
 * and the block standing in for whatever a piston is moving. Both carry the side the piston faces
 * and whether it is a sticky one.
 *
 * @since 0.1.0
 */
@BlockTrait(values = @BlockTrait.Values(property = "type", allowed = {"normal", "sticky"}))
public interface TechnicalPiston extends Directional {

    @Override
    default TechnicalPiston withFacing(final BlockFace facing) {
        return (TechnicalPiston) Directional.super.withFacing(facing);
    }

    /**
     * {@return {@code true} if the part belongs to a sticky piston}
     *
     * @since 0.1.0
     */
    default boolean isSticky() {
        return "sticky".equals(get("type"));
    }

    /**
     * {@return the same part, of a sticky piston or of a normal one}
     *
     * @param sticky {@code true} for the part of a sticky piston
     * @since 0.1.0
     */
    default TechnicalPiston withSticky(final boolean sticky) {
        return (TechnicalPiston) with("type", sticky ? "sticky" : "normal");
    }
}
