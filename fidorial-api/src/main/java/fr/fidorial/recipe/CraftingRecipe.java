package fr.fidorial.recipe;

import org.jspecify.annotations.Nullable;

/**
 * A recipe made in a crafting grid, the 2×2 one of the inventory or the 3×3 one of a crafting
 * table.
 *
 * <p>{@link ShapedRecipe}, {@link ShapelessRecipe} and {@link TransmuteRecipe} cover what vanilla
 * does. A plugin needing something else implements this interface: {@link #match} sees the whole
 * grid and decides, through {@link CraftingMatch#of}, what comes out and what each slot loses.</p>
 *
 * <p>Implementations must be thread-safe: the server matches recipes from the threads of the
 * players crafting.</p>
 *
 * @since 0.1.0
 */
public interface CraftingRecipe extends Recipe {

    /**
     * Checks a grid against this recipe.
     *
     * <p>Called each time a grid changes, so keep it cheap. It must not change anything: the server
     * takes the items only once the player picks the result up.</p>
     *
     * @param input the grid
     * @param tags  resolves item tags
     * @return what one craft gives and takes, or {@code null} when the grid does not match
     * @since 0.1.0
     */
    @Nullable CraftingMatch match(CraftingInput input, ItemTagLookup tags);

    /**
     * @return the recipes sharing a group are shown as one entry of the recipe book; empty for none
     * @since 0.1.0
     */
    default String group() {
        return "";
    }

    /**
     * @return the tab of the recipe book this recipe is filed under
     * @since 0.1.0
     */
    default CraftingCategory category() {
        return CraftingCategory.MISC;
    }
}
