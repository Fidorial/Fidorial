package fr.fidorial.world.block;

import fr.fidorial.world.BlockPos;
import org.jspecify.annotations.Nullable;

/**
 * Read-only access to the blocks of a world.
 *
 * @since 0.1.0
 */
@FunctionalInterface
public interface BlockGetter {

    /**
     * {@return the state at a position, or {@code null} when it is not loaded}
     *
     * @param pos the block position
     * @since 0.1.0
     */
    @Nullable BlockData blockAt(BlockPos pos);
}
