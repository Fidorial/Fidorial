package fr.fidorial.world.block;

import com.google.common.base.Preconditions;
import fr.fidorial.item.ItemStack;

import java.util.Objects;
import java.util.random.RandomGenerator;

/**
 * One thing a block gives back when it is broken: a stack to hand over, and how
 * many of it to hand over this time.
 *
 * @param item the stack to hand over; its count is ignored
 * @param min  the smallest quantity, inclusive; {@code 0} lets the roll give nothing
 * @param max  the largest quantity, inclusive
 * @since 0.1.0
 */
public record BlockDrop(ItemStack item, int min, int max) {

    public BlockDrop {
        Objects.requireNonNull(item, "item");
        Preconditions.checkArgument(min >= 0, "min cannot be negative, got %s", min);
        Preconditions.checkArgument(max >= min, "max (%s) cannot be below min (%s)", max, min);
    }

    /**
     * Takes the quantity from the stack itself.
     *
     * @param item the stack to hand over
     * @return a drop that always gives that stack as-is
     * @since 0.1.0
     */
    public static BlockDrop of(final ItemStack item) {
        return new BlockDrop(item, item.count(), item.count());
    }

    /**
     * @param item the stack to hand over; its count is ignored
     * @param min  the smallest quantity, inclusive
     * @param max  the largest quantity, inclusive
     * @return a drop that gives that stack in a quantity somewhere in the range
     * @since 0.1.0
     */
    public static BlockDrop of(final ItemStack item, final int min, final int max) {
        return new BlockDrop(item, min, max);
    }

    /**
     * @param random the source of randomness
     * @return the stack to hand over this time, or {@link ItemStack#EMPTY} when the roll came up empty
     * @since 0.1.0
     */
    public ItemStack roll(final RandomGenerator random) {
        final int count = min == max ? min : random.nextInt(min, max + 1);
        return count <= 0 ? ItemStack.EMPTY : item.withCount(count);
    }
}
