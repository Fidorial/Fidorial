package fr.fidorial.world.block.data;

import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockProperties.Names;
import fr.fidorial.world.block.BlockTrait;

/**
 * A block state carrying the {@code level} property: the fill level of fluids, cauldrons and similar blocks.
 *
 * <p>Block states are immutable: {@code with...} methods return another state and leave this one
 * untouched.</p>
 *
 * @since 0.1.0
 */
@BlockTrait(properties = Names.LEVEL)
public interface Levelled extends BlockData {

    /**
     * {@return the current level, from {@code 0} to {@link #getMaximumLevel()}}
     *
     * @since 0.1.0
     */
    default int getLevel() {
        return get(type().range(Names.LEVEL));
    }

    /**
     * {@return the same block with another {@code level} value}
     *
     * @param level the level
     * @since 0.1.0
     */
    default Levelled withLevel(final int level) {
        return (Levelled) with(type().range(Names.LEVEL), level);
    }

    /**
     * {@return the highest level of this block}
     *
     * @throws IllegalStateException if the block type declares no {@code level} property
     * @since 0.1.0
     */
    default int getMaximumLevel() {
        return type().range(Names.LEVEL).max();
    }
}
