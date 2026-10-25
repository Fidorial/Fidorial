package fr.fidorial.recipe;

import fr.fidorial.item.ItemStack;
import fr.fidorial.item.data.DataComponentMap;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/**
 * A recipe turning one item into another while keeping its components, such as dyeing a shulker
 * box or a bundle without emptying it, or copying a filled map.
 *
 * <p>The grid must hold exactly one {@linkplain #input() input} and between
 * {@link #minMaterials()} and {@link #maxMaterials()} {@linkplain #material() materials}, anywhere.
 * The result takes the components of the input, then those of the {@linkplain #resultComponents()
 * result} on top.</p>
 *
 * @since 0.1.0
 */
@ApiStatus.NonExtendable
public interface TransmuteRecipe extends CraftingRecipe {

    /**
     * @param key the identifier of the recipe
     * @return a builder for a transmute recipe
     * @since 0.1.0
     */
    static Builder builder(final Key key) {
        return RecipeFactory.factory().transmute(key);
    }

    /**
     * @return the item transformed, whose components the result keeps
     * @since 0.1.0
     */
    Ingredient input();

    /**
     * @return what transforms it, each material in its own slot
     * @since 0.1.0
     */
    Ingredient material();

    /**
     * @return the fewest materials the grid may hold, at least {@code 1}
     * @since 0.1.0
     */
    int minMaterials();

    /**
     * @return the most materials the grid may hold
     * @since 0.1.0
     */
    int maxMaterials();

    /**
     * @return the item the input becomes, {@code null} when it stays the same item
     * @since 0.1.0
     */
    @Nullable Key resultItem();

    /**
     * @return how many items one craft gives, before {@linkplain #addMaterialCountToResult() materials are added}
     * @since 0.1.0
     */
    int resultCount();

    /**
     * @return the components applied on top of those of the input
     * @since 0.1.0
     */
    DataComponentMap resultComponents();

    /**
     * @return whether the result gains one item per material, as when copying a map
     * @since 0.1.0
     */
    boolean addMaterialCountToResult();

    /**
     * Builds a {@link TransmuteRecipe}.
     *
     * <p>Builders are mutable and are not safe to share between threads.</p>
     *
     * @since 0.1.0
     */
    interface Builder {

        /**
         * @param input the item transformed
         * @return this builder
         * @since 0.1.0
         */
        Builder input(Ingredient input);

        /**
         * @param material what transforms it
         * @return this builder
         * @since 0.1.0
         */
        Builder material(Ingredient material);

        /**
         * @param min the fewest materials, at least {@code 1}
         * @param max the most materials, at least {@code min} and at most {@code 8}
         * @return this builder
         * @since 0.1.0
         */
        Builder materialCount(int min, int max);

        /**
         * Makes the input become another item. Its count is how many items one craft gives and its
         * components go on top of those of the input.
         *
         * @param result the item the input becomes
         * @return this builder
         * @since 0.1.0
         */
        Builder result(ItemStack result);

        /**
         * Keeps the item of the input: one craft gives {@code count} copies of it.
         *
         * @param count how many items one craft gives
         * @return this builder
         * @since 0.1.0
         */
        Builder keepInputItem(int count);

        /**
         * @param add whether the result gains one item per material
         * @return this builder
         * @since 0.1.0
         */
        Builder addMaterialCountToResult(boolean add);

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
         * @throws IllegalArgumentException if the input or the material is missing
         * @since 0.1.0
         */
        TransmuteRecipe build();
    }
}
