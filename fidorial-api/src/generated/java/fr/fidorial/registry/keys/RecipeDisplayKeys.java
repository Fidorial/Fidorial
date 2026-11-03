package fr.fidorial.registry.keys;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.RecipeDisplay;
import java.util.List;
import java.util.stream.Stream;
import net.kyori.adventure.key.KeyPattern;

/**
 * Typed keys for entries in the {@code minecraft:recipe_display} registry.
 */
public final class RecipeDisplayKeys {
    /**
     * Key for {@code minecraft:crafting_shaped}.
     */
    public static final TypedKey<RecipeDisplay> CRAFTING_SHAPED = create("crafting_shaped");

    /**
     * Key for {@code minecraft:crafting_shapeless}.
     */
    public static final TypedKey<RecipeDisplay> CRAFTING_SHAPELESS = create("crafting_shapeless");

    /**
     * Key for {@code minecraft:furnace}.
     */
    public static final TypedKey<RecipeDisplay> FURNACE = create("furnace");

    /**
     * Key for {@code minecraft:smithing}.
     */
    public static final TypedKey<RecipeDisplay> SMITHING = create("smithing");

    /**
     * Key for {@code minecraft:stonecutter}.
     */
    public static final TypedKey<RecipeDisplay> STONECUTTER = create("stonecutter");

    private static final List<TypedKey<RecipeDisplay>> VALUES = List.of(
        CRAFTING_SHAPED,
        CRAFTING_SHAPELESS,
        FURNACE,
        SMITHING,
        STONECUTTER
    );

    private RecipeDisplayKeys() {
        throw new UnsupportedOperationException("RecipeDisplayKeys cannot be instantiated.");
    }

    private static TypedKey<RecipeDisplay> create(@KeyPattern final String value) {
        return TypedKey.withRegistry(RegistryKey.RECIPE_DISPLAY, value);
    }

    /**
     * Returns a stream containing all keys declared by this class.
     *
     * @return a stream of registry keys
     */
    public static Stream<TypedKey<RecipeDisplay>> values() {
        return VALUES.stream();
    }
}
