package fr.fidorial.recipe;

import fr.fidorial.item.ItemStack;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

/**
 * A recipe whose ingredients may sit anywhere in the grid, in any order.
 *
 * <p>The grid must hold exactly one non-empty slot per ingredient. Each slot is paired with one
 * ingredient that accepts it, then gives up that ingredient's {@linkplain Ingredient#count()
 * count}.</p>
 *
 * @since 0.1.0
 */
@ApiStatus.NonExtendable
public interface ShapelessRecipe extends CraftingRecipe {

    /**
     * The most ingredients a recipe can list, one per slot of a crafting table.
     *
     * @since 0.1.0
     */
    int MAX_INGREDIENTS = 9;

    /**
     * @param key the identifier of the recipe
     * @return a builder for a shapeless recipe
     * @since 0.1.0
     */
    static Builder builder(final Key key) {
        return RecipeFactory.factory().shapeless(key);
    }

    /**
     * @return the ingredients, one per non-empty slot the grid must hold
     * @since 0.1.0
     */
    List<Ingredient> ingredients();

    /**
     * @return what one craft gives
     * @since 0.1.0
     */
    ItemStack result();

    /**
     * Builds a {@link ShapelessRecipe}.
     *
     * <p>Builders are mutable and are not safe to share between threads.</p>
     *
     * @since 0.1.0
     */
    interface Builder {

        /**
         * @param ingredient one more ingredient, fed by its own slot
         * @return this builder
         * @since 0.1.0
         */
        Builder ingredient(Ingredient ingredient);

        /**
         * @param ingredient an ingredient to list several times
         * @param slots      how many slots it fills, each with its own {@linkplain Ingredient#count() count}
         * @return this builder
         * @since 0.1.0
         */
        Builder ingredient(Ingredient ingredient, int slots);

        /**
         * @param result what one craft gives
         * @return this builder
         * @since 0.1.0
         */
        Builder result(ItemStack result);

        /**
         * @param group the recipe book group, empty for none
         * @return this builder
         * @since 0.1.0
         */
        Builder group(String group);

        /**
         * @param category the recipe book tab
         * @return this builder
         * @since 0.1.0
         */
        Builder category(CraftingCategory category);

        /**
         * @return the recipe
         * @throws IllegalArgumentException if there are no or too many ingredients, or no result
         * @since 0.1.0
         */
        ShapelessRecipe build();
    }
}
