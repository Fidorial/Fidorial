package fr.fidorial.registry.keys;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.RecipeBookCategory;
import java.util.List;
import java.util.stream.Stream;
import net.kyori.adventure.key.KeyPattern;

/**
 * Typed keys for entries in the {@code minecraft:recipe_book_category} registry.
 */
public final class RecipeBookCategoryKeys {
    /**
     * Key for {@code minecraft:blast_furnace_blocks}.
     */
    public static final TypedKey<RecipeBookCategory> BLAST_FURNACE_BLOCKS = create("blast_furnace_blocks");

    /**
     * Key for {@code minecraft:blast_furnace_misc}.
     */
    public static final TypedKey<RecipeBookCategory> BLAST_FURNACE_MISC = create("blast_furnace_misc");

    /**
     * Key for {@code minecraft:campfire}.
     */
    public static final TypedKey<RecipeBookCategory> CAMPFIRE = create("campfire");

    /**
     * Key for {@code minecraft:crafting_building_blocks}.
     */
    public static final TypedKey<RecipeBookCategory> CRAFTING_BUILDING_BLOCKS = create("crafting_building_blocks");

    /**
     * Key for {@code minecraft:crafting_equipment}.
     */
    public static final TypedKey<RecipeBookCategory> CRAFTING_EQUIPMENT = create("crafting_equipment");

    /**
     * Key for {@code minecraft:crafting_misc}.
     */
    public static final TypedKey<RecipeBookCategory> CRAFTING_MISC = create("crafting_misc");

    /**
     * Key for {@code minecraft:crafting_redstone}.
     */
    public static final TypedKey<RecipeBookCategory> CRAFTING_REDSTONE = create("crafting_redstone");

    /**
     * Key for {@code minecraft:furnace_blocks}.
     */
    public static final TypedKey<RecipeBookCategory> FURNACE_BLOCKS = create("furnace_blocks");

    /**
     * Key for {@code minecraft:furnace_food}.
     */
    public static final TypedKey<RecipeBookCategory> FURNACE_FOOD = create("furnace_food");

    /**
     * Key for {@code minecraft:furnace_misc}.
     */
    public static final TypedKey<RecipeBookCategory> FURNACE_MISC = create("furnace_misc");

    /**
     * Key for {@code minecraft:smithing}.
     */
    public static final TypedKey<RecipeBookCategory> SMITHING = create("smithing");

    /**
     * Key for {@code minecraft:smoker_food}.
     */
    public static final TypedKey<RecipeBookCategory> SMOKER_FOOD = create("smoker_food");

    /**
     * Key for {@code minecraft:stonecutter}.
     */
    public static final TypedKey<RecipeBookCategory> STONECUTTER = create("stonecutter");

    private static final List<TypedKey<RecipeBookCategory>> VALUES = List.of(
        BLAST_FURNACE_BLOCKS,
        BLAST_FURNACE_MISC,
        CAMPFIRE,
        CRAFTING_BUILDING_BLOCKS,
        CRAFTING_EQUIPMENT,
        CRAFTING_MISC,
        CRAFTING_REDSTONE,
        FURNACE_BLOCKS,
        FURNACE_FOOD,
        FURNACE_MISC,
        SMITHING,
        SMOKER_FOOD,
        STONECUTTER
    );

    private RecipeBookCategoryKeys() {
        throw new UnsupportedOperationException("RecipeBookCategoryKeys cannot be instantiated.");
    }

    private static TypedKey<RecipeBookCategory> create(@KeyPattern final String value) {
        return TypedKey.withRegistry(RegistryKey.RECIPE_BOOK_CATEGORY, value);
    }

    /**
     * Returns a stream containing all keys declared by this class.
     *
     * @return a stream of registry keys
     */
    public static Stream<TypedKey<RecipeBookCategory>> values() {
        return VALUES.stream();
    }
}
