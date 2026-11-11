package fr.euphyllia.fidorial.server.world.block.farming;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.euphyllia.fidorial.server.world.farming.FidorialFarmingRegistry;
import fr.euphyllia.fidorial.server.world.loot.BlockLootTables;
import fr.fidorial.item.ItemStack;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TagKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockRegistry;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.farming.Tillage;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.random.RandomGenerator;

/**
 * What hoes do to the built-in blocks, read from the {@code minecraft:hoe} entry of the
 * {@code minecraft:block_transformer} registry: a list of transformations, each a set of rules
 * turning the blocks matching a condition into a state, and optionally a loot table rolled when
 * the transformation happens.
 *
 * <p>The rules understood are those matching a block, a list of blocks or a tag of blocks, alone or
 * together with the condition that the block right above is air. Any other rule is left out.</p>
 */
final class HoeTillage {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(HoeTillage.class);

    private static final Key AIR_TAG = Key.key("minecraft:air");

    private HoeTillage() {
        throw new UnsupportedOperationException("HoeTillage cannot be instantiated.");
    }

    /**
     * Declares every rule of the definition as a built-in tillage.
     *
     * @param registry   where to declare them
     * @param definition the definition of the hoe, {@code null} when the registry has none
     * @param blocks     the block registry, to resolve the states the rules name
     * @return how many rules were declared
     */
    static int declare(final FidorialFarmingRegistry registry, final @Nullable JsonElement definition,
                       final BlockRegistry blocks) {
        if (definition == null || !definition.isJsonArray()) {
            LOGGER.warn("No definition of what hoes do: no block can be tilled");
            return 0;
        }
        int declared = 0;
        for (final JsonElement transformation : definition.getAsJsonArray()) {
            if (!transformation.isJsonObject()) {
                continue;
            }
            final JsonObject object = transformation.getAsJsonObject();
            final JsonElement provider = object.get("block_state_provider");
            final JsonElement rules = provider != null && provider.isJsonObject() ? provider.getAsJsonObject().get("rules") : null;
            if (rules == null || !rules.isJsonArray()) {
                continue;
            }
            final Key loot = object.has("loot") ? Key.key(object.get("loot").getAsString()) : null;
            for (final JsonElement rule : rules.getAsJsonArray()) {
                try {
                    if (declare(registry, rule.getAsJsonObject(), loot, blocks)) {
                        declared++;
                    }
                } catch (final RuntimeException exception) {
                    LOGGER.warn("A rule of what hoes do could not be read: {}", rule, exception);
                }
            }
        }
        return declared;
    }

    private static boolean declare(final FidorialFarmingRegistry registry, final JsonObject rule, final @Nullable Key loot,
                                   final BlockRegistry blocks) {
        final BlockData tilled = state(rule.getAsJsonObject("then"), blocks);
        final Condition condition = condition(rule.get("if_true"));
        if (tilled == null || condition == null) {
            return false;
        }
        final Tillage tillage = new DataTillage(tilled, condition.needsRoom(), loot);
        if (condition.tag() != null) {
            registry.tillableTagBuiltin(condition.tag(), tillage);
        }
        for (final TypedKey<BlockType> block : condition.blocks()) {
            registry.tillableBuiltin(block, tillage);
        }
        return true;
    }

    /**
     * {@return the state a rule names: a block and the values of some of its properties}
     */
    private static @Nullable BlockData state(final @Nullable JsonObject definition, final BlockRegistry blocks) {
        if (definition == null || !definition.has("id")) {
            return null;
        }
        final BlockType type = blocks.type(TypedKey.withRegistry(RegistryKey.BLOCK, Key.key(definition.get("id").getAsString())))
                .orElse(null);
        if (type == null) {
            return null;
        }
        final Map<String, String> values = new LinkedHashMap<>();
        if (definition.has("properties") && definition.get("properties").isJsonObject()) {
            for (final Map.Entry<String, JsonElement> property : definition.getAsJsonObject("properties").entrySet()) {
                values.put(property.getKey(), property.getValue().getAsString());
            }
        }
        return type.dataOrNull(values);
    }

    /**
     * {@return what a rule asks of the block, {@code null} for a condition that is not understood}
     */
    private static @Nullable Condition condition(final @Nullable JsonElement definition) {
        if (definition == null || !definition.isJsonObject()) {
            return null;
        }
        final JsonObject object = definition.getAsJsonObject();
        if (!"all_of".equals(type(object))) {
            return object.has("offset") ? null : soil(object, false);
        }
        final JsonElement predicates = object.get("predicates");
        if (predicates == null || !predicates.isJsonArray()) {
            return null;
        }
        JsonObject soil = null;
        boolean needsRoom = false;
        for (final JsonElement predicate : predicates.getAsJsonArray()) {
            final JsonObject term = predicate.getAsJsonObject();
            if (!term.has("offset")) {
                if (soil != null) {
                    return null;
                }
                soil = term;
            } else if (isAirAbove(term)) {
                needsRoom = true;
            } else {
                return null;
            }
        }
        return soil == null ? null : soil(soil, needsRoom);
    }

    private static @Nullable Condition soil(final JsonObject term, final boolean needsRoom) {
        return switch (type(term)) {
            case "matching_block_tag" ->
                    new Condition(TagKey.withRegistry(RegistryKey.BLOCK, Key.key(term.get("tag").getAsString())), List.of(), needsRoom);
            case "matching_blocks" -> {
                final JsonElement names = term.get("blocks");
                final JsonArray list = new JsonArray();
                if (names.isJsonArray()) {
                    list.addAll(names.getAsJsonArray());
                } else {
                    list.add(names);
                }
                yield new Condition(null, list.asList().stream()
                        .map(name -> TypedKey.withRegistry(RegistryKey.BLOCK, Key.key(name.getAsString()))).toList(), needsRoom);
            }
            default -> null;
        };
    }

    private static boolean isAirAbove(final JsonObject term) {
        final JsonArray offset = term.getAsJsonArray("offset");
        return "matching_block_tag".equals(type(term))
                && Key.key(term.get("tag").getAsString()).equals(AIR_TAG)
                && offset.size() == 3
                && offset.get(0).getAsInt() == 0 && offset.get(1).getAsInt() == 1 && offset.get(2).getAsInt() == 0;
    }

    private static String type(final JsonObject definition) {
        final JsonElement type = definition.get("type");
        if (type == null) {
            return "";
        }
        final String id = type.getAsString();
        final int colon = id.indexOf(':');
        return colon < 0 ? id : id.substring(colon + 1);
    }

    /**
     * What a rule asks of the block a hoe is used on: belonging to a tag, or being one of some
     * blocks, and maybe having air above.
     */
    private record Condition(@Nullable TagKey<BlockType> tag, List<TypedKey<BlockType>> blocks, boolean needsRoom) {
    }

    /**
     * A tillage read from the data: what falls out of the block comes from a loot table.
     */
    private record DataTillage(BlockData tilled, boolean needsRoom, @Nullable Key loot) implements Tillage {

        @Override
        public List<ItemStack> drops(final RandomGenerator random) {
            final BlockLootTables tables = BlockLootTables.instance();
            return loot == null || tables == null ? List.of() : tables.roll(loot, null, ItemStack.EMPTY, random);
        }
    }
}
