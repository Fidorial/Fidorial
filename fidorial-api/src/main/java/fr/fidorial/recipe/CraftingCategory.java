package fr.fidorial.recipe;

import java.util.Locale;

/**
 * The tab of the recipe book a crafting recipe is filed under.
 *
 * @since 0.1.0
 */
public enum CraftingCategory {

    /**
     * Blocks to build with.
     */
    BUILDING,

    /**
     * Redstone components.
     */
    REDSTONE,

    /**
     * Tools, weapons and armor.
     */
    EQUIPMENT,

    /**
     * Everything else, the default.
     */
    MISC;

    /**
     * @param name the vanilla name, e.g. {@code building}
     * @return the matching category, {@link #MISC} when the name is unknown
     * @since 0.1.0
     */
    public static CraftingCategory byName(final String name) {
        for (final CraftingCategory category : values()) {
            if (category.name().equalsIgnoreCase(name)) {
                return category;
            }
        }
        return MISC;
    }

    /**
     * @return the vanilla name, e.g. {@code building}
     * @since 0.1.0
     */
    public String serializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
