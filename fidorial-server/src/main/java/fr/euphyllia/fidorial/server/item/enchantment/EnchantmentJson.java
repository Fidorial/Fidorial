package fr.euphyllia.fidorial.server.item.enchantment;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import fr.euphyllia.fidorial.server.codecs.adventure.ComponentCodecs;
import fr.euphyllia.fidorial.server.codecs.adventure.NbtCodecs;
import fr.euphyllia.fidorial.server.item.enchantment.effect.EffectJson;
import fr.euphyllia.fidorial.server.item.enchantment.effect.EnchantmentEffectSet;
import fr.fidorial.inventory.EquipmentSlotGroup;
import fr.fidorial.item.enchantment.EnchantmentBehaviour;
import fr.fidorial.item.enchantment.EnchantmentDefinition;
import fr.fidorial.item.enchantment.KeySet;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * Converts enchantments between their data pack JSON form, their definitions and the NBT sent to
 * clients.
 */
public final class EnchantmentJson {

    private EnchantmentJson() {
        throw new UnsupportedOperationException("EnchantmentJson cannot be instantiated.");
    }

    /**
     * Parses a JSON string.
     *
     * @throws IllegalArgumentException if it is not a JSON object
     */
    public static JsonObject parse(final Key key, final String json) {
        final JsonElement element;
        try {
            element = JsonParser.parseString(json);
        } catch (final RuntimeException exception) {
            throw new IllegalArgumentException("Enchantment " + key.asString() + " is not valid JSON", exception);
        }
        if (!element.isJsonObject()) {
            throw new IllegalArgumentException("Enchantment " + key.asString() + " must be a JSON object");
        }
        return element.getAsJsonObject();
    }

    /**
     * Reads an enchantment.
     *
     * @param key       its key
     * @param json      its data pack JSON
     * @param behaviour the Java behaviour to give it, {@code null} for none
     * @return the definition
     * @throws IllegalArgumentException if a mandatory field is missing or a value is out of range
     */
    public static EnchantmentDefinitionImpl read(final Key key, final JsonObject json,
                                                 final @Nullable EnchantmentBehaviour behaviour) {
        try {
            final Component description = ComponentCodecs.COMPONENT_CODEC
                    .parse(JsonOps.INSTANCE, required(json, "description"))
                    .getOrThrow(message -> new IllegalArgumentException("Invalid description: " + message));
            final KeySet supported = KeySetImpl.parse(required(json, "supported_items"), "'supported_items'");
            final Optional<KeySet> primary = json.has("primary_items")
                    ? Optional.of(KeySetImpl.parse(json.get("primary_items"), "'primary_items'"))
                    : Optional.empty();
            final KeySet exclusive = json.has("exclusive_set")
                    ? KeySetImpl.parse(json.get("exclusive_set"), "'exclusive_set'")
                    : KeySetImpl.EMPTY;
            final Set<EquipmentSlotGroup> slots = new LinkedHashSet<>();
            for (final JsonElement slot : EffectJson.list(json, "slots")) {
                if (!slot.isJsonPrimitive()) {
                    throw new IllegalArgumentException("A slot must be a name, got " + slot);
                }
                slots.add(slotGroup(slot.getAsString()));
            }
            final JsonObject effectsTree = EffectJson.optionalObject(json, "effects");
            final EnchantmentEffectSet effects;
            try {
                effects = EnchantmentEffectSet.compile(effectsTree);
            } catch (final IllegalArgumentException exception) {
                throw new IllegalArgumentException("Invalid effects: " + exception.getMessage(), exception);
            }
            return new EnchantmentDefinitionImpl(
                    key,
                    description,
                    supported,
                    primary,
                    exclusive,
                    EffectJson.integer(json, "max_level"),
                    EffectJson.integer(json, "weight"),
                    EffectJson.integer(json, "anvil_cost"),
                    CostImpl.parse(EffectJson.object(json, "min_cost")),
                    CostImpl.parse(EffectJson.object(json, "max_cost")),
                    slots,
                    effectsTree,
                    effects,
                    Optional.ofNullable(behaviour));
        } catch (final IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid enchantment " + key.asString() + ": " + exception.getMessage(), exception);
        }
    }

    private static JsonElement required(final JsonObject json, final String field) {
        final JsonElement element = json.get(field);
        if (element == null || element.isJsonNull()) {
            throw new IllegalArgumentException("'" + field + "' is missing");
        }
        return element;
    }

    private static EquipmentSlotGroup slotGroup(final String name) {
        for (final EquipmentSlotGroup group : EquipmentSlotGroup.values()) {
            if (group.serializedName().equals(name)) {
                return group;
            }
        }
        throw new IllegalArgumentException("Unknown slot group " + name);
    }

    /**
     * Writes an enchantment as data pack JSON.
     *
     * @param definition  the enchantment
     * @param items       maps a listed item to the one written, {@code null} to leave it out
     * @param enchantments maps a listed enchantment to the one written, {@code null} to leave it out
     * @return the JSON object
     */
    public static JsonObject write(final EnchantmentDefinition definition,
                                   final Function<Key, @Nullable Key> items,
                                   final Function<Key, @Nullable Key> enchantments) {
        final JsonObject json = new JsonObject();
        json.add("description", ComponentCodecs.COMPONENT_CODEC
                .encodeStart(JsonOps.INSTANCE, definition.description())
                .getOrThrow(message -> new IllegalStateException(
                        "Failed to encode the description of " + definition.key().asString() + ": " + message)));
        json.add("supported_items", keySet(definition.supportedItems(), items));
        definition.primaryItems().ifPresent(primary -> json.add("primary_items", keySet(primary, items)));
        if (!definition.exclusiveSet().isEmpty()) {
            json.add("exclusive_set", keySet(definition.exclusiveSet(), enchantments));
        }
        json.addProperty("weight", definition.weight());
        json.addProperty("max_level", definition.maxLevel());
        json.add("min_cost", cost(definition.minCost()));
        json.add("max_cost", cost(definition.maxCost()));
        json.addProperty("anvil_cost", definition.anvilCost());
        final JsonArray slots = new JsonArray();
        for (final EquipmentSlotGroup slot : definition.slots()) {
            slots.add(slot.serializedName());
        }
        json.add("slots", slots);
        definition.effectsJson().ifPresent(effects -> json.add("effects", JsonParser.parseString(effects)));
        return json;
    }

    private static JsonElement keySet(final KeySet set, final Function<Key, @Nullable Key> mapper) {
        final Optional<Key> tag = set.tag();
        if (tag.isPresent()) {
            return new JsonPrimitive("#" + tag.get().asString());
        }
        final Set<String> written = new LinkedHashSet<>();
        for (final Key key : set.keys()) {
            final Key mapped = mapper.apply(key);
            if (mapped != null) {
                written.add(mapped.asString());
            }
        }
        final JsonArray array = new JsonArray();
        written.forEach(array::add);
        return array;
    }

    private static JsonObject cost(final EnchantmentDefinition.Cost cost) {
        final JsonObject object = new JsonObject();
        object.addProperty("base", cost.base());
        object.addProperty("per_level_above_first", cost.perLevelAboveFirst());
        return object;
    }

    /**
     * {@return the NBT form of a JSON tree, as sent in the registry data}
     */
    public static BinaryTag toNbt(final JsonElement json) {
        return NbtCodecs.BINARY_TAG_CODEC
                .parse(JsonOps.INSTANCE, json)
                .getOrThrow(message -> new IllegalArgumentException("Cannot convert to NBT: " + message));
    }

    /**
     * {@return the JSON form of an NBT tree, as read from the vanilla registry data}
     */
    public static JsonElement fromNbt(final BinaryTag tag) {
        return NbtCodecs.BINARY_TAG_CODEC
                .encodeStart(JsonOps.INSTANCE, tag)
                .getOrThrow(message -> new IllegalArgumentException("Cannot convert from NBT: " + message));
    }
}
