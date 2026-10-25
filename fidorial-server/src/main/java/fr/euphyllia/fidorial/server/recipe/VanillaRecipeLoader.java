package fr.euphyllia.fidorial.server.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import fr.euphyllia.fidorial.server.codecs.item.ItemStackCodecs;
import fr.fidorial.item.ItemStack;
import fr.fidorial.item.data.DataComponentMap;
import fr.fidorial.item.data.DataComponentTypes;
import fr.fidorial.recipe.CraftingCategory;
import fr.fidorial.recipe.Ingredient;
import fr.fidorial.recipe.Recipe;
import fr.fidorial.recipe.ShapedRecipe;
import fr.fidorial.recipe.ShapelessRecipe;
import fr.fidorial.recipe.TransmuteRecipe;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;

public final class VanillaRecipeLoader {

    public static final String RESOURCE = "/fidorial-data/recipes.json";

    private static final ComponentLogger LOGGER = ComponentLogger.logger(VanillaRecipeLoader.class);

    private static final String SHAPED = "minecraft:crafting_shaped";
    private static final String SHAPELESS = "minecraft:crafting_shapeless";
    private static final String TRANSMUTE = "minecraft:crafting_transmute";
    private static final String TAG_PREFIX = "#";

    private final Predicate<Key> knownItem;
    private final Map<String, Integer> skipped = new TreeMap<>();

    /**
     * @param knownItem tells whether an item exists, so that recipes making unknown items are skipped
     */
    public VanillaRecipeLoader(final Predicate<Key> knownItem) {
        this.knownItem = knownItem;
    }

    /**
     * @return the recipes of the bundled dataset, empty when it is missing
     */
    public List<Recipe> load() {
        try (final InputStream input = VanillaRecipeLoader.class.getResourceAsStream(RESOURCE)) {
            if (input == null) {
                LOGGER.warn("Missing resource {}: no vanilla recipe will be available. "
                        + "Run ':fidorial-server:generateRecipes'.", RESOURCE);
                return List.of();
            }
            return load(new InputStreamReader(input, StandardCharsets.UTF_8));
        } catch (final IOException exception) {
            throw new UncheckedIOException("Failed to load " + RESOURCE, exception);
        }
    }

    /**
     * @param reader a dataset: recipe identifier &rarr; vanilla recipe
     * @return the recipes Fidorial can run
     */
    public List<Recipe> load(final Reader reader) {
        final JsonObject dataset = JsonParser.parseReader(reader).getAsJsonObject();
        final List<Recipe> recipes = new ArrayList<>();
        for (final Map.Entry<String, JsonElement> entry : dataset.entrySet()) {
            final Key key = Key.key(entry.getKey());
            final JsonObject json = entry.getValue().getAsJsonObject();
            final String type = json.get("type").getAsString();
            try {
                final Recipe recipe = switch (type) {
                    case SHAPED -> shaped(key, json);
                    case SHAPELESS -> shapeless(key, json);
                    case TRANSMUTE -> transmute(key, json);
                    default -> null;
                };
                if (recipe != null) {
                    recipes.add(recipe);
                } else {
                    skip(type);
                }
            } catch (final RuntimeException exception) {
                LOGGER.warn("Skipping the vanilla recipe {}: {}", key.asString(), exception.getMessage());
                skip(type);
            }
        }
        if (!skipped.isEmpty()) {
            LOGGER.debug("Vanilla recipes not loaded, by type: {}", skipped);
        }
        return recipes;
    }

    /**
     * @return how many recipes of each type were skipped by the last {@link #load()}
     */
    public Map<String, Integer> skipped() {
        return Map.copyOf(skipped);
    }

    private void skip(final String type) {
        skipped.merge(type, 1, Integer::sum);
    }

    private @Nullable Recipe shaped(final Key key, final JsonObject json) {
        final ItemStack result = result(json);
        if (result == null) {
            return null;
        }
        final JsonArray rows = json.getAsJsonArray("pattern");
        final String[] pattern = new String[rows.size()];
        for (int i = 0; i < pattern.length; i++) {
            pattern[i] = rows.get(i).getAsString();
        }
        final ShapedRecipe.Builder builder = new ShapedRecipeImpl.BuilderImpl(key)
                .pattern(pattern)
                .result(result)
                .group(string(json, "group"))
                .category(CraftingCategory.byName(string(json, "category")));
        for (final Map.Entry<String, JsonElement> definition : json.getAsJsonObject("key").entrySet()) {
            if (definition.getKey().length() != 1) {
                throw new IllegalArgumentException("invalid symbol '" + definition.getKey() + "'");
            }
            builder.define(definition.getKey().charAt(0), ingredient(definition.getValue()));
        }
        return builder.build();
    }

    private @Nullable Recipe shapeless(final Key key, final JsonObject json) {
        final ItemStack result = result(json);
        if (result == null) {
            return null;
        }
        final ShapelessRecipe.Builder builder = new ShapelessRecipeImpl.BuilderImpl(key)
                .result(result)
                .group(string(json, "group"))
                .category(CraftingCategory.byName(string(json, "category")));
        for (final JsonElement ingredient : json.getAsJsonArray("ingredients")) {
            builder.ingredient(ingredient(ingredient));
        }
        return builder.build();
    }

    private @Nullable Recipe transmute(final Key key, final JsonObject json) {
        final TransmuteRecipe.Builder builder = new TransmuteRecipeImpl.BuilderImpl(key)
                .input(ingredient(json.get("input")))
                .material(ingredient(json.get("material")))
                .group(string(json, "group"))
                .category(CraftingCategory.byName(string(json, "category")))
                .addMaterialCountToResult(json.has("add_material_count_to_result")
                        && json.get("add_material_count_to_result").getAsBoolean());

        if (json.has("material_count")) {
            final JsonElement count = json.get("material_count");
            if (count.isJsonPrimitive()) {
                builder.materialCount(count.getAsInt(), count.getAsInt());
            } else {
                final JsonObject range = count.getAsJsonObject();
                builder.materialCount(
                        range.has("min") ? range.get("min").getAsInt() : 1,
                        range.has("max") ? range.get("max").getAsInt() : ShapelessRecipe.MAX_INGREDIENTS - 1);
            }
        }

        final JsonObject resultJson = json.getAsJsonObject("result");
        if (resultJson == null || !resultJson.has("id")) {
            // No item: the input stays the same item, as when copying a map.
            builder.keepInputItem(resultJson != null && resultJson.has("count") ? resultJson.get("count").getAsInt() : 1);
        } else {
            final ItemStack result = result(json);
            if (result == null) {
                return null;
            }
            builder.result(result);
        }
        return builder.build();
    }

    private @Nullable ItemStack result(final JsonObject recipe) {
        final JsonObject json = recipe.getAsJsonObject("result");
        final Key id = Key.key(json.get("id").getAsString());
        if (!knownItem.test(id)) {
            throw new IllegalArgumentException("unknown result item " + id.asString());
        }
        final int count = json.has("count") ? json.get("count").getAsInt() : 1;
        DataComponentMap components = DataComponentMap.EMPTY;
        if (json.has("components")) {
            final JsonObject raw = json.getAsJsonObject("components");
            for (final String name : raw.keySet()) {
                final String stripped = name.startsWith(ItemStackCodecs.REMOVED_PREFIX)
                        ? name.substring(ItemStackCodecs.REMOVED_PREFIX.length())
                        : name;
                if (!DataComponentTypes.isModelled(Key.key(stripped))) {
                    // Making the item without it would hand out something else than vanilla does.
                    return null;
                }
            }
            components = ItemStackCodecs.COMPONENT_MAP_CODEC.parse(JsonOps.INSTANCE, raw)
                    .getOrThrow(message -> new IllegalArgumentException("invalid result components: " + message));
        }
        return new ItemStack(id, count, components);
    }

    private Ingredient ingredient(final JsonElement json) {
        if (json.isJsonArray()) {
            final List<Key> items = new ArrayList<>();
            for (final JsonElement item : json.getAsJsonArray()) {
                final String id = item.getAsString();
                if (id.startsWith(TAG_PREFIX)) {
                    throw new IllegalArgumentException("a tag inside an item list: " + id);
                }
                items.add(Key.key(id));
            }
            return IngredientImpl.items(new LinkedHashSet<>(items));
        }
        final String id = json.getAsString();
        return id.startsWith(TAG_PREFIX)
                ? IngredientImpl.tag(Key.key(id.substring(TAG_PREFIX.length())))
                : IngredientImpl.items(Set.of(Key.key(id)));
    }

    private static String string(final JsonObject json, final String member) {
        return json.has(member) ? json.get(member).getAsString() : "";
    }
}
