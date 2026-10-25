package fr.euphyllia.fidorial.server.item.enchantment;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.euphyllia.fidorial.server.item.enchantment.effect.EnchantmentEffectSet;
import fr.fidorial.inventory.EquipmentSlotGroup;
import fr.fidorial.item.enchantment.EnchantmentBehaviour;
import fr.fidorial.item.enchantment.EnchantmentDefinition;
import fr.fidorial.item.enchantment.KeySet;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.Enchantment;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Server implementation of {@link EnchantmentDefinition}, with its effect components compiled.
 */
public record EnchantmentDefinitionImpl(
        Key key,
        Component description,
        KeySet supportedItems,
        Optional<KeySet> primaryItems,
        KeySet exclusiveSet,
        int maxLevel,
        int weight,
        int anvilCost,
        EnchantmentDefinition.Cost minCost,
        EnchantmentDefinition.Cost maxCost,
        Set<EquipmentSlotGroup> slots,
        @Nullable JsonObject effectsTree,
        EnchantmentEffectSet effects,
        Optional<EnchantmentBehaviour> behaviour
) implements EnchantmentDefinition {

    public EnchantmentDefinitionImpl {
        Preconditions.checkArgument(key != null, "The key of an enchantment must not be null");
        Preconditions.checkArgument(description != null, "The description of %s must not be null", key);
        Preconditions.checkArgument(supportedItems != null && !supportedItems.isEmpty(),
                "The supported items of %s must not be empty", key);
        Preconditions.checkArgument(maxLevel >= 1 && maxLevel <= MAX_LEVEL,
                "The maximum level of %s must be between 1 and %s, got %s", key, MAX_LEVEL, maxLevel);
        Preconditions.checkArgument(weight >= 1 && weight <= MAX_WEIGHT,
                "The weight of %s must be between 1 and %s, got %s", key, MAX_WEIGHT, weight);
        Preconditions.checkArgument(anvilCost >= 0, "The anvil cost of %s must not be negative, got %s", key, anvilCost);
        slots = Collections.unmodifiableSet(new LinkedHashSet<>(slots));
        effectsTree = effectsTree == null || effectsTree.isEmpty() ? null : effectsTree.deepCopy();
    }

    @Override
    public Set<Key> effectComponents() {
        return effects.components();
    }

    @Override
    public Optional<String> effectsJson() {
        return effectsTree == null ? Optional.empty() : Optional.of(effectsTree.toString());
    }

    @Override
    public String toJson() {
        return EnchantmentJson.write(this, item -> item, enchantment -> enchantment).toString();
    }

    @Override
    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    @Override
    public String toString() {
        return "EnchantmentDefinition[" + key.asString() + "]";
    }

    /**
     * {@return the translation key vanilla gives an enchantment, {@code enchantment.<namespace>.<value>}}
     */
    static String descriptionId(final Key key) {
        return "enchantment." + key.namespace() + "." + key.value().replace('/', '.');
    }

    public static final class BuilderImpl implements EnchantmentDefinition.Builder {

        private final Key key;
        private Component description;
        private @Nullable KeySet supportedItems;
        private @Nullable KeySet primaryItems;
        private KeySet exclusiveSet = KeySetImpl.EMPTY;
        private int maxLevel = 1;
        private int weight = 1;
        private int anvilCost = 1;
        private Cost minCost = new CostImpl(1, 10);
        private Cost maxCost = new CostImpl(21, 10);
        private List<EquipmentSlotGroup> slots = List.of(EquipmentSlotGroup.ANY);
        private @Nullable JsonObject effectsTree;
        private EnchantmentEffectSet effects = EnchantmentEffectSet.EMPTY;
        private @Nullable EnchantmentBehaviour behaviour;

        public BuilderImpl(final TypedKey<Enchantment> key) {
            Preconditions.checkArgument(key != null, "The key of an enchantment must not be null");
            this.key = key.key();
            this.description = Component.translatable(descriptionId(this.key));
        }

        private BuilderImpl(final EnchantmentDefinitionImpl definition) {
            this.key = definition.key();
            this.description = definition.description();
            this.supportedItems = definition.supportedItems();
            this.primaryItems = definition.primaryItems().orElse(null);
            this.exclusiveSet = definition.exclusiveSet();
            this.maxLevel = definition.maxLevel();
            this.weight = definition.weight();
            this.anvilCost = definition.anvilCost();
            this.minCost = definition.minCost();
            this.maxCost = definition.maxCost();
            this.slots = List.copyOf(definition.slots());
            this.effectsTree = definition.effectsTree();
            this.effects = definition.effects();
            this.behaviour = definition.behaviour().orElse(null);
        }

        @Override
        public Builder description(final Component description) {
            Preconditions.checkArgument(description != null, "The description of %s must not be null", key);
            this.description = description;
            return this;
        }

        @Override
        public Builder supportedItemTag(final Key tag) {
            Preconditions.checkArgument(tag != null, "The supported item tag of %s must not be null", key);
            this.supportedItems = KeySetImpl.ofTag(tag);
            return this;
        }

        @Override
        public Builder supportedItems(final Collection<Key> items) {
            Preconditions.checkArgument(items != null && !items.isEmpty(), "The supported items of %s must not be empty", key);
            this.supportedItems = KeySetImpl.of(copy(items, "supported item"));
            return this;
        }

        @Override
        public Builder primaryItemTag(final Key tag) {
            Preconditions.checkArgument(tag != null, "The primary item tag of %s must not be null", key);
            this.primaryItems = KeySetImpl.ofTag(tag);
            return this;
        }

        @Override
        public Builder primaryItems(final Collection<Key> items) {
            Preconditions.checkArgument(items != null, "The primary items of %s must not be null", key);
            this.primaryItems = KeySetImpl.of(copy(items, "primary item"));
            return this;
        }

        @Override
        public Builder exclusiveSetTag(final Key tag) {
            Preconditions.checkArgument(tag != null, "The exclusive set tag of %s must not be null", key);
            this.exclusiveSet = KeySetImpl.ofTag(tag);
            return this;
        }

        @Override
        public Builder exclusiveWith(final Collection<TypedKey<Enchantment>> enchantments) {
            Preconditions.checkArgument(enchantments != null, "The exclusive set of %s must not be null", key);
            final List<Key> keys = new ArrayList<>(enchantments.size());
            for (final TypedKey<Enchantment> enchantment : enchantments) {
                Preconditions.checkArgument(enchantment != null, "The exclusive set of %s must not hold null", key);
                keys.add(enchantment.key());
            }
            this.exclusiveSet = KeySetImpl.of(keys);
            return this;
        }

        @Override
        public Builder maxLevel(final int maxLevel) {
            Preconditions.checkArgument(maxLevel >= 1 && maxLevel <= MAX_LEVEL,
                    "The maximum level of %s must be between 1 and %s, got %s", key, MAX_LEVEL, maxLevel);
            this.maxLevel = maxLevel;
            return this;
        }

        @Override
        public Builder weight(final int weight) {
            Preconditions.checkArgument(weight >= 1 && weight <= MAX_WEIGHT,
                    "The weight of %s must be between 1 and %s, got %s", key, MAX_WEIGHT, weight);
            this.weight = weight;
            return this;
        }

        @Override
        public Builder anvilCost(final int anvilCost) {
            Preconditions.checkArgument(anvilCost >= 0, "The anvil cost of %s must not be negative, got %s", key, anvilCost);
            this.anvilCost = anvilCost;
            return this;
        }

        @Override
        public Builder minCost(final int base, final int perLevelAboveFirst) {
            this.minCost = new CostImpl(base, perLevelAboveFirst);
            return this;
        }

        @Override
        public Builder maxCost(final int base, final int perLevelAboveFirst) {
            this.maxCost = new CostImpl(base, perLevelAboveFirst);
            return this;
        }

        @Override
        public Builder slots(final Collection<EquipmentSlotGroup> slots) {
            Preconditions.checkArgument(slots != null && !slots.isEmpty(), "The slots of %s must not be empty", key);
            for (final EquipmentSlotGroup slot : slots) {
                Preconditions.checkArgument(slot != null, "The slots of %s must not hold null", key);
            }
            this.slots = List.copyOf(new LinkedHashSet<>(slots));
            return this;
        }

        @Override
        public Builder effectsJson(final @Nullable String json) {
            if (json == null || json.isBlank()) {
                this.effectsTree = null;
                this.effects = EnchantmentEffectSet.EMPTY;
                return this;
            }
            final JsonElement element;
            try {
                element = JsonParser.parseString(json);
            } catch (final RuntimeException exception) {
                throw new IllegalArgumentException("The effects of " + key.asString() + " are not valid JSON", exception);
            }
            if (!element.isJsonObject()) {
                throw new IllegalArgumentException("The effects of " + key.asString() + " must be a JSON object");
            }
            try {
                this.effects = EnchantmentEffectSet.compile(element.getAsJsonObject());
            } catch (final IllegalArgumentException exception) {
                throw new IllegalArgumentException("Invalid effects for " + key.asString() + ": " + exception.getMessage(), exception);
            }
            this.effectsTree = element.getAsJsonObject();
            return this;
        }

        @Override
        public Builder behaviour(final EnchantmentBehaviour behaviour) {
            Preconditions.checkArgument(behaviour != null, "The behaviour of %s must not be null", key);
            this.behaviour = behaviour;
            return this;
        }

        @Override
        public EnchantmentDefinition build() {
            if (supportedItems == null || supportedItems.isEmpty()) {
                throw new IllegalStateException("The enchantment " + key.asString() + " needs supported items");
            }
            return new EnchantmentDefinitionImpl(key, description, supportedItems, Optional.ofNullable(primaryItems),
                    exclusiveSet, maxLevel, weight, anvilCost, minCost, maxCost, new LinkedHashSet<>(slots),
                    effectsTree, effects, Optional.ofNullable(behaviour));
        }

        private List<Key> copy(final Collection<Key> keys, final String what) {
            final List<Key> copy = new ArrayList<>(keys.size());
            for (final Key item : keys) {
                Preconditions.checkArgument(item != null, "A %s of %s must not be null", what, key);
                copy.add(item);
            }
            return copy;
        }
    }
}
