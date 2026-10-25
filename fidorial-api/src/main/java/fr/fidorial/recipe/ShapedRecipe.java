package fr.fidorial.recipe;

import fr.fidorial.item.ItemStack;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/**
 * A recipe whose ingredients must sit in a given layout, anywhere in the grid.
 *
 * <p>Like vanilla, the empty rows and columns around the pattern are trimmed, the pattern may sit
 * anywhere in a grid large enough to hold it, and it also matches flipped left to right unless
 * {@linkplain Builder#mirrored(boolean) told otherwise}. Every slot outside the pattern must be
 * empty.</p>
 *
 * <p>Each ingredient takes its own {@linkplain Ingredient#count() count} from its slot, so a
 * pattern may ask for sixteen diamond blocks in one slot and a single stick in another.</p>
 *
 * @since 0.1.0
 */
@ApiStatus.NonExtendable
public interface ShapedRecipe extends CraftingRecipe {

    /**
     * The largest pattern, the size of a crafting table.
     *
     * @since 0.1.0
     */
    int MAX_SIZE = 3;

    /**
     * @param key the identifier of the recipe
     * @return a builder for a shaped recipe
     * @since 0.1.0
     */
    static Builder builder(final Key key) {
        return RecipeFactory.factory().shaped(key);
    }

    /**
     * @return the number of columns of the trimmed pattern
     * @since 0.1.0
     */
    int width();

    /**
     * @return the number of rows of the trimmed pattern
     * @since 0.1.0
     */
    int height();

    /**
     * @param x the column of the trimmed pattern
     * @param y the row of the trimmed pattern
     * @return what that cell asks for, {@code null} when it must stay empty
     * @throws IndexOutOfBoundsException if the cell is outside the pattern
     * @since 0.1.0
     */
    @Nullable Ingredient ingredient(int x, int y);

    /**
     * @return what one craft gives
     * @since 0.1.0
     */
    ItemStack result();

    /**
     * @return whether the pattern also matches flipped left to right
     * @since 0.1.0
     */
    boolean mirrored();

    /**
     * Builds a {@link ShapedRecipe}, vanilla-style: a pattern of characters and what each one
     * stands for. A space is an empty cell.
     *
     * <p>Builders are mutable and are not safe to share between threads.</p>
     *
     * @since 0.1.0
     */
    interface Builder {

        /**
         * @param rows one string per row, all as wide, at most {@value #MAX_SIZE} of at most
         *             {@value #MAX_SIZE} characters
         * @return this builder
         * @since 0.1.0
         */
        Builder pattern(String... rows);

        /**
         * @param symbol     a character of the pattern, anything but a space
         * @param ingredient what it stands for
         * @return this builder
         * @since 0.1.0
         */
        Builder define(char symbol, Ingredient ingredient);

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
         * @param mirrored whether the pattern also matches flipped left to right, {@code true} by default
         * @return this builder
         * @since 0.1.0
         */
        Builder mirrored(boolean mirrored);

        /**
         * @return the recipe
         * @throws IllegalArgumentException if the pattern is malformed, uses an undefined symbol,
         *                                  leaves a definition unused or the result is empty
         * @since 0.1.0
         */
        ShapedRecipe build();
    }
}
