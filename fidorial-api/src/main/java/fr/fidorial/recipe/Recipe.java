package fr.fidorial.recipe;

import net.kyori.adventure.key.Key;

/**
 * A recipe of the {@linkplain RecipeRegistry recipe registry}.
 *
 * @since 0.1.0
 */
public interface Recipe {

    /**
     * @return the identifier of the recipe, e.g. {@code minecraft:crafting_table}
     * @since 0.1.0
     */
    Key key();
}
