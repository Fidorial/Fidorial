package fr.euphyllia.fidorial.server.world.loot;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.fidorial.item.ItemStack;
import fr.fidorial.item.component.ItemEnchantments;
import fr.fidorial.item.data.DataComponentTypes;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TagKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.Item;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockType;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;

/**
 * What blocks drop, rolled from the loot tables of the {@code loot_tables.json} dataset written by
 * the {@code generateLootTables} task.
 *
 * <p>Only what the tables of blocks need is understood: items, alternatives, the conditions on the
 * block state, on the tool and on chance, and the modifiers changing how many items drop. A
 * condition that cannot be evaluated here, such as one on an entity, never holds; a modifier that
 * cannot be applied, such as one copying components, is skipped. Nothing here knows about
 * explosions: every block survives, and no stack decays.</p>
 */
public final class BlockLootTables {

    public static final String RESOURCE = "/fidorial-data/loot_tables.json";

    private static final ComponentLogger LOGGER = ComponentLogger.logger(BlockLootTables.class);
    private static final String BLOCK_TABLE_PREFIX = "blocks/";

    private static volatile @Nullable BlockLootTables instance;

    private final Map<Key, JsonObject> tables;
    private final Map<String, JsonElement> predicates;
    private final Predicate<TypedKey<Item>> knownItems;
    private final BiPredicate<TypedKey<Item>, TagKey<Item>> itemTags;
    private final BiPredicate<TypedKey<BlockType>, TagKey<BlockType>> blockTags;

    private BlockLootTables(final Map<Key, JsonObject> tables, final Map<String, JsonElement> predicates,
                            final Predicate<TypedKey<Item>> knownItems,
                            final BiPredicate<TypedKey<Item>, TagKey<Item>> itemTags,
                            final BiPredicate<TypedKey<BlockType>, TagKey<BlockType>> blockTags) {
        this.tables = tables;
        this.predicates = predicates;
        this.knownItems = knownItems;
        this.itemTags = itemTags;
        this.blockTags = blockTags;
    }

    /**
     * Reads the bundled dataset and makes it the one {@link #instance()} returns.
     *
     * @param knownItems whether an item exists, the tables naming others are rolled without them
     * @param itemTags   whether an item belongs to a tag
     * @param blockTags  whether a block type belongs to a tag
     */
    public static void bootstrap(final Predicate<TypedKey<Item>> knownItems,
                                 final BiPredicate<TypedKey<Item>, TagKey<Item>> itemTags,
                                 final BiPredicate<TypedKey<BlockType>, TagKey<BlockType>> blockTags) {
        instance = load(knownItems, itemTags, blockTags);
    }

    /**
     * {@return the loot tables of the server, {@code null} before {@link #bootstrap}}
     */
    public static @Nullable BlockLootTables instance() {
        return instance;
    }

    /**
     * Reads the bundled dataset. Without it, no block drops anything.
     *
     * @param knownItems whether an item exists, the tables naming others are rolled without them
     * @param itemTags   whether an item belongs to a tag
     * @param blockTags  whether a block type belongs to a tag
     * @return the loot tables
     */
    public static BlockLootTables load(final Predicate<TypedKey<Item>> knownItems,
                                       final BiPredicate<TypedKey<Item>, TagKey<Item>> itemTags,
                                       final BiPredicate<TypedKey<BlockType>, TagKey<BlockType>> blockTags) {
        try (final InputStream input = BlockLootTables.class.getResourceAsStream(RESOURCE)) {
            if (input == null) {
                LOGGER.warn("Missing resource {}: no block will drop anything. "
                        + "Run ':fidorial-server:generateLootTables'.", RESOURCE);
                return new BlockLootTables(Map.of(), Map.of(), knownItems, itemTags, blockTags);
            }
            return read(new InputStreamReader(input, StandardCharsets.UTF_8), knownItems, itemTags, blockTags);
        } catch (final IOException exception) {
            throw new UncheckedIOException("Failed to load " + RESOURCE, exception);
        }
    }

    /**
     * Reads a dataset: an object holding {@code loot_tables} and {@code predicates}, each mapping
     * an identifier to its definition.
     */
    public static BlockLootTables read(final Reader reader, final Predicate<TypedKey<Item>> knownItems,
                                       final BiPredicate<TypedKey<Item>, TagKey<Item>> itemTags,
                                       final BiPredicate<TypedKey<BlockType>, TagKey<BlockType>> blockTags) {
        final JsonObject dataset = JsonParser.parseReader(reader).getAsJsonObject();
        final Map<Key, JsonObject> tables = new HashMap<>();
        final JsonObject lootTables = dataset.getAsJsonObject("loot_tables");
        if (lootTables != null) {
            for (final Map.Entry<String, JsonElement> entry : lootTables.entrySet()) {
                if (entry.getValue().isJsonObject()) {
                    tables.put(Key.key(entry.getKey()), entry.getValue().getAsJsonObject());
                }
            }
        }
        final Map<String, JsonElement> predicates = new HashMap<>();
        final JsonObject named = dataset.getAsJsonObject("predicates");
        if (named != null) {
            for (final Map.Entry<String, JsonElement> entry : named.entrySet()) {
                predicates.put(entry.getKey(), entry.getValue());
            }
        }
        LOGGER.debug("Read {} loot tables", tables.size());
        return new BlockLootTables(Map.copyOf(tables), Map.copyOf(predicates), knownItems, itemTags, blockTags);
    }

    /**
     * {@return the key of the loot table of a block type}
     *
     * @param block the block type
     */
    public static Key tableOf(final TypedKey<BlockType> block) {
        final Key key = block.key();
        return Key.key(key.namespace(), BLOCK_TABLE_PREFIX + key.value());
    }

    /**
     * Rolls what a block drops when it is broken.
     *
     * @param state  the state being broken
     * @param tool   what it is broken with, {@link ItemStack#EMPTY} for none
     * @param random the source of randomness
     * @return the stacks to drop, empty for a block without a loot table
     */
    public List<ItemStack> drops(final BlockData state, final ItemStack tool, final RandomGenerator random) {
        return roll(tableOf(state.key()), state, tool, random);
    }

    /**
     * Rolls a loot table.
     *
     * @param table  the key of the table
     * @param state  the state of the block the roll is about, {@code null} for none
     * @param tool   the tool the roll is made with, {@link ItemStack#EMPTY} for none
     * @param random the source of randomness
     * @return the stacks rolled, empty for an unknown table
     */
    public List<ItemStack> roll(final Key table, final @Nullable BlockData state, final ItemStack tool,
                                final RandomGenerator random) {
        final JsonObject definition = tables.get(table);
        if (definition == null) {
            return List.of();
        }
        final Roll roll = new Roll(state, tool, random);
        final List<ItemStack> drops = new ArrayList<>();
        try {
            final JsonElement pools = definition.get("pools");
            if (pools != null && pools.isJsonArray()) {
                for (final JsonElement pool : pools.getAsJsonArray()) {
                    rollPool(pool.getAsJsonObject(), definition.get("modifier"), roll, drops);
                }
            }
        } catch (final RuntimeException exception) {
            LOGGER.error("The loot table {} could not be rolled", table, exception);
            return List.of();
        }
        return drops;
    }

    private void rollPool(final JsonObject pool, final @Nullable JsonElement tableModifiers, final Roll roll,
                          final List<ItemStack> drops) {
        if (!holds(pool.get("condition"), roll)) {
            return;
        }
        final JsonElement entries = pool.get("entries");
        if (entries == null || !entries.isJsonArray()) {
            return;
        }
        final int rolls = number(pool.get("rolls"), roll, 1);
        for (int index = 0; index < rolls; index++) {
            final List<JsonObject> candidates = new ArrayList<>();
            for (final JsonElement entry : entries.getAsJsonArray()) {
                expand(entry.getAsJsonObject(), roll, candidates);
            }
            final JsonObject picked = pick(candidates, roll.random());
            if (picked == null) {
                continue;
            }
            final TypedKey<Item> item = TypedKey.withRegistry(RegistryKey.ITEM, Key.key(picked.get("name").getAsString()));
            if (!knownItems.test(item)) {
                continue;
            }
            int count = 1;
            count = modify(picked.get("modifier"), count, roll);
            count = modify(pool.get("modifier"), count, roll);
            count = modify(tableModifiers, count, roll);
            if (count > 0) {
                drops.add(ItemStack.of(item, count));
            }
        }
    }

    /**
     * Adds the items an entry may give: the entry itself when it is an item whose condition holds,
     * the first child whose condition holds for alternatives.
     */
    private void expand(final JsonObject entry, final Roll roll, final List<JsonObject> candidates) {
        if (!holds(entry.get("condition"), roll)) {
            return;
        }
        switch (type(entry)) {
            case "item" -> candidates.add(entry);
            case "alternatives" -> {
                final JsonElement children = entry.get("children");
                if (children == null || !children.isJsonArray()) {
                    return;
                }
                for (final JsonElement child : children.getAsJsonArray()) {
                    final int before = candidates.size();
                    expand(child.getAsJsonObject(), roll, candidates);
                    if (candidates.size() > before) {
                        return;
                    }
                }
            }
            default -> {
                // an entry made of what the block holds, such as the contents of a container
            }
        }
    }

    private static @Nullable JsonObject pick(final List<JsonObject> candidates, final RandomGenerator random) {
        if (candidates.isEmpty()) {
            return null;
        }
        if (candidates.size() == 1) {
            return candidates.getFirst();
        }
        int total = 0;
        for (final JsonObject candidate : candidates) {
            total += weight(candidate);
        }
        int remaining = random.nextInt(Math.max(1, total));
        for (final JsonObject candidate : candidates) {
            remaining -= weight(candidate);
            if (remaining < 0) {
                return candidate;
            }
        }
        return candidates.getLast();
    }

    private static int weight(final JsonObject entry) {
        final JsonElement weight = entry.get("weight");
        return weight == null ? 1 : Math.max(0, weight.getAsInt());
    }

    private int modify(final @Nullable JsonElement modifiers, final int count, final Roll roll) {
        if (modifiers == null) {
            return count;
        }
        if (modifiers.isJsonArray()) {
            int result = count;
            for (final JsonElement modifier : modifiers.getAsJsonArray()) {
                result = modify(modifier, result, roll);
            }
            return result;
        }
        final JsonObject modifier = modifiers.getAsJsonObject();
        if (!holds(modifier.get("condition"), roll)) {
            return count;
        }
        return switch (type(modifier)) {
            case "set_count" -> {
                final int value = number(modifier.get("count"), roll, count);
                final JsonElement add = modifier.get("add");
                yield add != null && add.getAsBoolean() ? count + value : value;
            }
            case "apply_bonus" -> bonus(modifier, count, roll);
            case "limit_count" -> limit(modifier.get("limit"), count);
            default -> count;
        };
    }

    private static int bonus(final JsonObject modifier, final int count, final Roll roll) {
        final int level = roll.enchantment(modifier.get("enchantment").getAsString());
        final JsonObject parameters = modifier.has("parameters") ? modifier.getAsJsonObject("parameters") : new JsonObject();
        final RandomGenerator random = roll.random();
        return switch (stripNamespace(modifier.get("formula").getAsString())) {
            case "binomial_with_bonus_count" -> {
                final int tries = level + parameters.get("extra").getAsInt();
                yield count + binomial(tries, parameters.get("probability").getAsFloat(), random);
            }
            case "uniform_bonus_count" -> {
                final int multiplier = parameters.has("bonusMultiplier") ? parameters.get("bonusMultiplier").getAsInt() : 1;
                yield count + random.nextInt(multiplier * level + 1);
            }
            case "ore_drops" -> level <= 0 ? count : count * (Math.max(0, random.nextInt(level + 2) - 1) + 1);
            default -> count;
        };
    }

    private static int limit(final @Nullable JsonElement limit, final int count) {
        if (limit == null) {
            return count;
        }
        if (limit.isJsonPrimitive()) {
            return Math.min(count, limit.getAsInt());
        }
        final JsonObject bounds = limit.getAsJsonObject();
        int result = count;
        if (bounds.has("min")) {
            result = Math.max(result, bounds.get("min").getAsInt());
        }
        if (bounds.has("max")) {
            result = Math.min(result, bounds.get("max").getAsInt());
        }
        return result;
    }

    private int number(final @Nullable JsonElement provider, final Roll roll, final int fallback) {
        if (provider == null) {
            return fallback;
        }
        if (provider.isJsonPrimitive()) {
            return Math.round(provider.getAsFloat());
        }
        final JsonObject definition = provider.getAsJsonObject();
        return switch (type(definition)) {
            case "constant" -> Math.round(definition.get("value").getAsFloat());
            case "uniform" -> {
                final int min = number(definition.get("min"), roll, 0);
                final int max = number(definition.get("max"), roll, min);
                yield max <= min ? min : min + roll.random().nextInt(max - min + 1);
            }
            case "binomial" -> binomial(number(definition.get("n"), roll, 0), definition.get("p").getAsFloat(), roll.random());
            default -> fallback;
        };
    }

    private static int binomial(final int tries, final float probability, final RandomGenerator random) {
        int successes = 0;
        for (int index = 0; index < tries; index++) {
            if (random.nextFloat() < probability) {
                successes++;
            }
        }
        return successes;
    }

    /**
     * {@return {@code true} if a condition holds}: an object, the name of a predicate of the
     * dataset, or a list of conditions that must all hold; no condition always holds
     */
    private boolean holds(final @Nullable JsonElement condition, final Roll roll) {
        if (condition == null) {
            return true;
        }
        if (condition.isJsonArray()) {
            for (final JsonElement term : condition.getAsJsonArray()) {
                if (!holds(term, roll)) {
                    return false;
                }
            }
            return true;
        }
        if (condition.isJsonPrimitive()) {
            final JsonElement named = predicates.get(condition.getAsString());
            return named != null && holds(named, roll);
        }
        final JsonObject definition = condition.getAsJsonObject();
        return switch (type(definition)) {
            case "survives_explosion" -> true;
            case "all_of" -> holds(definition.get("terms"), roll);
            case "any_of" -> anyHolds(definition.get("terms"), roll);
            case "inverted" -> !holds(definition.get("term"), roll);
            case "random_chance" -> roll.random().nextFloat() < definition.get("chance").getAsFloat();
            case "table_bonus" -> {
                final JsonArray chances = definition.getAsJsonArray("chances");
                final int level = Math.min(roll.enchantment(definition.get("enchantment").getAsString()), chances.size() - 1);
                yield roll.random().nextFloat() < chances.get(level).getAsFloat();
            }
            case "match_block" -> matchesBlock(definition, roll.state());
            case "match_tool" -> matchesTool(definition.getAsJsonObject("predicate"), roll.tool());
            default -> false;
        };
    }

    private boolean anyHolds(final @Nullable JsonElement terms, final Roll roll) {
        if (terms == null || !terms.isJsonArray()) {
            return false;
        }
        for (final JsonElement term : terms.getAsJsonArray()) {
            if (holds(term, roll)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesBlock(final JsonObject condition, final @Nullable BlockData state) {
        if (state == null) {
            return false;
        }
        final JsonElement blocks = condition.get("blocks");
        if (blocks != null && !matches(blocks, name -> matchesBlockName(name, state.key()))) {
            return false;
        }
        final JsonElement properties = condition.get("state");
        if (properties == null || !properties.isJsonObject()) {
            return true;
        }
        for (final Map.Entry<String, JsonElement> property : properties.getAsJsonObject().entrySet()) {
            final String value = state.get(property.getKey());
            if (value == null || !matchesValue(property.getValue(), value)) {
                return false;
            }
        }
        return true;
    }

    private boolean matchesBlockName(final String name, final TypedKey<BlockType> block) {
        if (name.startsWith("#")) {
            return blockTags.test(block, TagKey.withRegistry(RegistryKey.BLOCK, Key.key(name.substring(1))));
        }
        return block.key().equals(Key.key(name));
    }

    /**
     * {@return {@code true} if the value of a property is the expected one, or lies in the expected
     * range}
     */
    private static boolean matchesValue(final JsonElement expected, final String value) {
        if (expected.isJsonPrimitive()) {
            return expected.getAsString().equals(value);
        }
        final JsonObject range = expected.getAsJsonObject();
        try {
            final int number = Integer.parseInt(value);
            return (!range.has("min") || number >= range.get("min").getAsInt())
                    && (!range.has("max") || number <= range.get("max").getAsInt());
        } catch (final NumberFormatException exception) {
            return false;
        }
    }

    private boolean matchesTool(final @Nullable JsonObject predicate, final ItemStack tool) {
        if (predicate == null) {
            return true;
        }
        final JsonElement items = predicate.get("items");
        if (items != null && (tool.isEmpty() || !matches(items, name -> matchesItemName(name, tool.id())))) {
            return false;
        }
        final JsonElement nested = predicate.get("predicates");
        if (nested == null || !nested.isJsonObject()) {
            return true;
        }
        final JsonElement enchantments = nested.getAsJsonObject().get("minecraft:enchantments");
        if (enchantments == null || !enchantments.isJsonArray()) {
            return true;
        }
        for (final JsonElement required : enchantments.getAsJsonArray()) {
            final JsonObject requirement = required.getAsJsonObject();
            final JsonElement names = requirement.get("enchantments");
            final JsonObject levels = requirement.has("levels") && requirement.get("levels").isJsonObject()
                    ? requirement.getAsJsonObject("levels") : new JsonObject();
            final int min = levels.has("min") ? levels.get("min").getAsInt() : 1;
            final int max = levels.has("max") ? levels.get("max").getAsInt() : Integer.MAX_VALUE;
            if (names == null || !matches(names, name -> {
                final int level = enchantment(tool, name);
                return level >= min && level <= max;
            })) {
                return false;
            }
        }
        return true;
    }

    private boolean matchesItemName(final String name, final TypedKey<Item> item) {
        if (name.startsWith("#")) {
            return itemTags.test(item, TagKey.withRegistry(RegistryKey.ITEM, Key.key(name.substring(1))));
        }
        return item.key().equals(Key.key(name));
    }

    /**
     * {@return {@code true} if a name, or one of a list of names, passes a test}
     */
    private static boolean matches(final JsonElement names, final Predicate<String> test) {
        if (names.isJsonArray()) {
            for (final JsonElement name : names.getAsJsonArray()) {
                if (test.test(name.getAsString())) {
                    return true;
                }
            }
            return false;
        }
        return test.test(names.getAsString());
    }

    private static int enchantment(final ItemStack tool, final String name) {
        if (tool.isEmpty()) {
            return 0;
        }
        return tool.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantments.EMPTY)
                .level(TypedKey.withRegistry(RegistryKey.ENCHANTMENT, Key.key(name)));
    }

    private static String type(final JsonObject definition) {
        final JsonElement type = definition.get("type");
        return type == null ? "" : stripNamespace(type.getAsString());
    }

    private static String stripNamespace(final String id) {
        final int colon = id.indexOf(':');
        return colon < 0 ? id : id.substring(colon + 1);
    }

    /**
     * What one roll of a table is made about.
     */
    private record Roll(@Nullable BlockData state, ItemStack tool, RandomGenerator random) {

        int enchantment(final String name) {
            return BlockLootTables.enchantment(tool, name);
        }
    }
}
