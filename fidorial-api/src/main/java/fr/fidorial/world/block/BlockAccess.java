package fr.fidorial.world.block;

import fr.fidorial.item.ItemStack;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.BlockPos;

import java.util.List;

/**
 * The slice of a world a {@link BlockBehaviour} is allowed to see and touch while
 * it reacts to something: a random tick, a neighbour changing, a placement check.
 *
 * @since 0.1.0
 */
public interface BlockAccess extends BlockGetter {

    /**
     * @param pos the position to read
     * @return the state there, or air when its chunk is not loaded
     * @since 0.1.0
     */
    @Override
    BlockData blockAt(BlockPos pos);

    /**
     * @param pos       the position to start from
     * @param direction the direction to look in
     * @return the state of the neighbour, or air
     * @since 0.1.0
     */
    default BlockData relative(final BlockPos pos, final BlockFace direction) {
        return blockAt(pos.relative(direction));
    }

    /**
     * Replaces a block, tells nearby clients about it, then lets the six
     * neighbours react (a crop pops off when its soil goes, a stem lets go of its
     * fruit, ...).
     *
     * @param pos  the position to write to
     * @param data the state to write
     * @return {@code true} when the write went through
     * @since 0.1.0
     */
    boolean setBlock(BlockPos pos, BlockData data);

    /**
     * Breaks a block the way the world would: replaces it with air, optionally
     * hands out its {@linkplain BlockBehaviour#drops drops}, then updates the
     * neighbours.
     *
     * @param pos       the position to break
     * @param dropItems {@code false} to break it without giving anything back
     * @return {@code true} when something was broken
     * @since 0.1.0
     */
    boolean destroyBlock(BlockPos pos, boolean dropItems);

    /**
     * Hands items out at a position without breaking anything — berries picked
     * off a bush, for instance.
     *
     * @param pos   where the items come from
     * @param items what to hand out
     * @since 0.1.0
     */
    void dropItems(BlockPos pos, List<ItemStack> items);

    /**
     * @param pos the position to look at
     * @return the combined block and sky light there, {@code 0}–{@code 15}
     * @since 0.1.0
     */
    int lightLevel(BlockPos pos);
}
