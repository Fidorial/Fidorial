package fr.fidorial.world.block.data;

import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockProperties.Names;
import fr.fidorial.world.block.BlockTrait;

/**
 * A block state carrying the {@code facing} property: the side the block faces.
 *
 * <p>Block states are immutable: {@code with...} methods return another state and leave this one
 * untouched.</p>
 *
 * @since 0.1.0
 */
@BlockTrait(values = @BlockTrait.Values(property = Names.FACING, allowed = {"north", "south", "east", "west", "up", "down"}))
public interface Directional extends BlockData {

    /**
     * {@return the side the block faces}
     *
     * @since 0.1.0
     */
    default BlockFace getFacing() {
        return get(type().choice(Names.FACING, BlockFace.class));
    }

    /**
     * {@return the same block with another {@code facing} value}
     *
     * @param facing the side to face; must be one of the values the block accepts
     * @since 0.1.0
     */
    default Directional withFacing(final BlockFace facing) {
        return (Directional) with(type().choice(Names.FACING, BlockFace.class), facing);
    }
}
