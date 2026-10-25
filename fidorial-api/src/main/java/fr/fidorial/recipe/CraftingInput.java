package fr.fidorial.recipe;

import fr.fidorial.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

/**
 * The contents of a crafting grid, row by row from the top-left corner.
 *
 * <p>The grid of the player inventory is 2×2, the one of a crafting table 3×3. Empty slots hold
 * {@link ItemStack#EMPTY}. Instances are immutable snapshots.</p>
 *
 * @since 0.1.0
 */
@ApiStatus.NonExtendable
public interface CraftingInput {

    /**
     * @param width  the number of columns
     * @param height the number of rows
     * @param items  {@code width * height} stacks, row-major
     * @return a snapshot of that grid, to {@linkplain RecipeRegistry#matchCrafting match} or test against
     * @throws IllegalArgumentException if the grid is smaller than 1×1 or {@code items} has the wrong size
     * @since 0.1.0
     */
    static CraftingInput of(final int width, final int height, final List<ItemStack> items) {
        return RecipeFactory.factory().input(width, height, items);
    }

    /**
     * @return the number of columns
     * @since 0.1.0
     */
    int width();

    /**
     * @return the number of rows
     * @since 0.1.0
     */
    int height();

    /**
     * @return {@code width * height} stacks, row-major
     * @since 0.1.0
     */
    List<ItemStack> items();

    /**
     * @param slot the slot index, row-major
     * @return the stack in that slot, {@link ItemStack#EMPTY} when it is empty
     * @since 0.1.0
     */
    ItemStack get(int slot);

    /**
     * @param x the column, from {@code 0} on the left
     * @param y the row, from {@code 0} at the top
     * @return the stack at that position, {@link ItemStack#EMPTY} when it is empty
     * @since 0.1.0
     */
    ItemStack get(int x, int y);

    /**
     * @return {@code width * height}
     * @since 0.1.0
     */
    int size();

    /**
     * @return how many slots hold something
     * @since 0.1.0
     */
    int nonEmptyCount();

    /**
     * @return {@code true} when every slot is empty
     * @since 0.1.0
     */
    boolean isEmpty();
}
