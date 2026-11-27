package fr.fidorial.world.block.data.type;

import com.google.common.base.Preconditions;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockProperties.Names;
import fr.fidorial.world.block.BlockTrait;
import fr.fidorial.world.block.data.Directional;

/**
 * The block state of a hopper: the side its spout faces, down or horizontal, and whether it is
 * enabled.
 *
 * <p>A hopper receiving redstone power is disabled: it neither pushes, pulls nor picks up items,
 * but other hoppers can still push items into it.</p>
 *
 * @since 0.1.0
 */
@BlockTrait(properties = Names.ENABLED,
        values = @BlockTrait.Values(property = Names.FACING, allowed = {"down", "north", "south", "west", "east"}, exactly = true))
public interface Hopper extends Directional {

    /**
     * {@return the same hopper with its spout facing another side}
     *
     * @param facing the side, down or horizontal
     * @throws IllegalArgumentException if {@code facing} is up
     * @since 0.1.0
     */
    @Override
    default Hopper withFacing(final BlockFace facing) {
        Preconditions.checkArgument(facing != BlockFace.UP, "The spout of a hopper faces down or a side, never up");
        return (Hopper) Directional.super.withFacing(facing);
    }

    /**
     * {@return {@code true} if the hopper moves items, {@code false} while redstone power locks it}
     *
     * @since 0.1.0
     */
    default boolean isEnabled() {
        return get(type().flag(Names.ENABLED));
    }

    /**
     * {@return the same hopper, enabled or locked}
     *
     * <p>This only changes the state: the redstone around the hopper is not told.</p>
     *
     * @param enabled {@code true} to let the hopper move items
     * @since 0.1.0
     */
    default Hopper withEnabled(final boolean enabled) {
        return (Hopper) with(type().flag(Names.ENABLED), enabled);
    }
}
