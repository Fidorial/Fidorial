package fr.fidorial.world.block.crop;

import fr.fidorial.item.ItemStack;
import net.kyori.adventure.key.Key;

import java.util.Objects;
import java.util.random.RandomGenerator;

/**
 * One thing a crop gives back when it is broken: a stack to hand over, and how
 * many of it to hand over this time.
 *
 * @param item the stack to hand over; its count is ignored
 * @param min  the smallest quantity, inclusive; {@code 0} lets the roll give nothing
 * @param max  the largest quantity, inclusive
 * @since 0.1.0
 */
public record CropDrop(ItemStack item, int min, int max) {

    public CropDrop {
        Objects.requireNonNull(item, "item");
        if (min < 0) {
            throw new IllegalArgumentException("min cannot be negative, got " + min);
        }
        if (max < min) {
            throw new IllegalArgumentException("max (" + max + ") cannot be below min (" + min + ")");
        }
    }

    /**
     * @param item  an item identifier
     * @param count a fixed quantity
     * @return a drop that always gives the same amount of a plain item
     * @since 0.1.0
     */
    public static CropDrop of(final Key item, final int count) {
        return new CropDrop(ItemStack.of(item), count, count);
    }

    /**
     * @param item an item identifier
     * @param min  the smallest quantity, inclusive
     * @param max  the largest quantity, inclusive
     * @return a drop that gives a quantity somewhere in that range
     * @since 0.1.0
     */
    public static CropDrop of(final Key item, final int min, final int max) {
        return new CropDrop(ItemStack.of(item), min, max);
    }

    /**
     * Takes the quantity from the stack itself.
     *
     * @param item the stack to hand over
     * @return a drop that always gives that stack as-is
     * @since 0.1.0
     */
    public static CropDrop of(final ItemStack item) {
        return new CropDrop(item, item.count(), item.count());
    }

    /**
     * @param item the stack to hand over; its count is ignored
     * @param min  the smallest quantity, inclusive
     * @param max  the largest quantity, inclusive
     * @return a drop that gives that stack in a quantity somewhere in the range
     * @since 0.1.0
     */
    public static CropDrop of(final ItemStack item, final int min, final int max) {
        return new CropDrop(item, min, max);
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
