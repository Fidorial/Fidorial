package fr.fidorial.recipe;

import net.kyori.adventure.key.Key;

/**
 * Answers whether an item belongs to an item tag, such as {@code minecraft:planks}.
 *
 * <p>The server hands one to every {@linkplain CraftingRecipe#match match}, so that recipes can
 * accept {@linkplain Ingredient#tag(Key) tags} without knowing where tags are stored.</p>
 *
 * @since 0.1.0
 */
@FunctionalInterface
public interface ItemTagLookup {

    /**
     * A lookup for which every tag is empty.
     *
     * @since 0.1.0
     */
    ItemTagLookup EMPTY = (tag, item) -> false;

    /**
     * @param tag  the tag, e.g. {@code minecraft:planks}, without the leading {@code #}
     * @param item the item identifier
     * @return {@code true} when {@code item} is a member of {@code tag}
     * @since 0.1.0
     */
    boolean isTagged(Key tag, Key item);
}
