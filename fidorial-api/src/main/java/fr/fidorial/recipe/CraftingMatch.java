package fr.fidorial.recipe;

import fr.fidorial.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

/**
 * A recipe that matches a crafting grid: what one craft gives, and what it takes from each slot.
 *
 * <p>Instances are immutable.</p>
 *
 * @since 0.1.0
 */
@ApiStatus.NonExtendable
public interface CraftingMatch {

    /**
     * What a {@link CraftingRecipe} written by a plugin returns from {@link CraftingRecipe#match}.
     *
     * @param recipe   the recipe that matched
     * @param result   what one craft gives, not empty
     * @param consumed how many items one craft takes from each slot of the input, row-major
     * @return the match
     * @throws IllegalArgumentException if {@code result} is empty or a count is negative
     * @since 0.1.0
     */
    static CraftingMatch of(final CraftingRecipe recipe, final ItemStack result, final int[] consumed) {
        return RecipeFactory.factory().match(recipe, result, consumed);
    }

    /**
     * @return the recipe that matched
     * @since 0.1.0
     */
    CraftingRecipe recipe();

    /**
     * @return what one craft gives
     * @since 0.1.0
     */
    ItemStack result();

    /**
     * @param slot the slot index of the input, row-major
     * @return how many items one craft takes from that slot, {@code 0} outside the grid
     * @since 0.1.0
     */
    int consumed(int slot);

    /**
     * @param newResult what one craft gives instead
     * @return a match taking the same items but giving {@code newResult}
     * @since 0.1.0
     */
    CraftingMatch withResult(ItemStack newResult);
}
