package fr.fidorial.world.block.data;

import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockProperties.Names;
import fr.fidorial.world.block.BlockTrait;

/**
 * A block state carrying the {@code open} property: whether the block is open, like doors, trapdoors and gates.
 *
 * <p>Block states are immutable: {@code with...} methods return another state and leave this one
 * untouched.</p>
 *
 * @since 0.1.0
 */
@BlockTrait(properties = Names.OPEN)
public interface Openable extends BlockData {

    /**
     * {@return {@code true} if the block is open}
     *
     * @since 0.1.0
     */
    default boolean isOpen() {
        return get(type().flag(Names.OPEN));
    }

    /**
     * {@return the same block with another {@code open} value}
     *
     * @param open {@code true} to open the block
     * @since 0.1.0
     */
    default Openable withOpen(final boolean open) {
        return (Openable) with(type().flag(Names.OPEN), open);
    }
}
