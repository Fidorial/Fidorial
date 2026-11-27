package fr.fidorial.world.block.data;

import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockProperties.Names;
import fr.fidorial.world.block.BlockTrait;

/**
 * A block state carrying the {@code waterlogged} property: whether the block holds water.
 *
 * <p>Block states are immutable: {@code with...} methods return another state and leave this one
 * untouched.</p>
 *
 * @since 0.1.0
 */
@BlockTrait(properties = Names.WATERLOGGED)
public interface Waterlogged extends BlockData {

    /**
     * {@return {@code true} if the block holds water}
     *
     * @since 0.1.0
     */
    default boolean isWaterlogged() {
        return get(type().flag(Names.WATERLOGGED));
    }

    /**
     * {@return the same block with another {@code waterlogged} value}
     *
     * @param waterlogged {@code true} to fill the block with water
     * @since 0.1.0
     */
    default Waterlogged withWaterlogged(final boolean waterlogged) {
        return (Waterlogged) with(type().flag(Names.WATERLOGGED), waterlogged);
    }
}
