package fr.fidorial.item.enchantment;

import fr.fidorial.inventory.EquipmentSlot;
import fr.fidorial.inventory.EquipmentSlotGroup;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.Enchantment;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * A complete enchantment definition, ready to be sent to clients.
 *
 * <p>Definitions are created through {@link EnchantmentRegistry#define(TypedKey)} or read from data
 * pack JSON. Vanilla enchantments are exposed as definitions as well.</p>
 *
 * <p>The {@code minecraft:enchantment} registry is synchronised with clients during the configuration
 * phase, so vanilla clients display custom enchantments without any mod. Their behaviour runs on the
 * server, through the {@linkplain #effectComponents() effect components} of their data and their
 * {@link EnchantmentBehaviour}s.</p>
 *
 * @since 0.1.0
 */
@ApiStatus.NonExtendable
public interface EnchantmentDefinition extends Enchantment {

    /**
     * The highest level of an enchantment.
     *
     * @since 0.1.0
     */
    int MAX_LEVEL = 255;

    /**
     * The highest weight of an enchantment.
     *
     * @since 0.1.0
     */
    int MAX_WEIGHT = 1024;

    /**
     * {@return this enchantment's key, typed against the {@code minecraft:enchantment} registry}
     *
     * @since 0.1.0
     */
    @Contract(pure = true)
    default TypedKey<Enchantment> typedKey() {
        return TypedKey.create(RegistryKey.ENCHANTMENT, key());
    }

    /**
     * {@return the name of the enchantment, without its level}
     *
     * @since 0.1.0
     */
    Component description();

    /**
     * {@return the items the enchantment can be applied to}
     *
     * @since 0.1.0
     */
    KeySet supportedItems();

    /**
     * {@return the items the enchanting table offers the enchantment for, or empty to use
     * {@link #supportedItems()}}
     *
     * @since 0.1.0
     */
    Optional<KeySet> primaryItems();

    /**
     * {@return the enchantments incompatible with this one, possibly empty}
     *
     * @since 0.1.0
     */
    KeySet exclusiveSet();

    /**
     * {@return the maximum level, between {@code 1} and {@value #MAX_LEVEL}}
     *
     * @since 0.1.0
     */
    int maxLevel();

    /**
     * {@return the weight of the enchantment in random selections, between {@code 1} and
     * {@value #MAX_WEIGHT}}
     *
     * @since 0.1.0
     */
    int weight();

    /**
     * {@return the base anvil cost per level}
     *
     * @since 0.1.0
     */
    int anvilCost();

    /**
     * {@return the minimum enchanting cost of each level}
     *
     * @since 0.1.0
     */
    Cost minCost();

    /**
     * {@return the maximum enchanting cost of each level}
     *
     * @since 0.1.0
     */
    Cost maxCost();

    /**
     * {@return the equipment slots in which the enchantment is active}
     *
     * @since 0.1.0
     */
    Set<EquipmentSlotGroup> slots();

    /**
     * {@return {@code true} if the enchantment is active in {@code slot}}
     *
     * @param slot the equipment slot
     * @since 0.1.0
     */
    default boolean matchesSlot(final EquipmentSlot slot) {
        for (final EquipmentSlotGroup group : slots()) {
            if (group.test(slot)) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@return the effect components declared by the enchantment, such as {@code minecraft:damage}}
     *
     * @see EnchantmentEffects
     * @see EnchantmentRegistry#supportedEffectComponents()
     * @since 0.1.0
     */
    Set<Key> effectComponents();

    /**
     * {@return {@code true} if the enchantment declares the effect component {@code component}}
     *
     * @param component the effect component, see {@link EnchantmentEffects}
     * @since 0.1.0
     */
    default boolean hasEffect(final Key component) {
        return effectComponents().contains(component);
    }

    /**
     * {@return the {@code effects} object of the enchantment as JSON, or empty if it declares no effect
     * component}
     *
     * @since 0.1.0
     */
    Optional<String> effectsJson();

    /**
     * {@return the behaviour of this definition, if any}
     *
     * <p>Behaviours {@linkplain EnchantmentRegistry#attach attached} to the enchantment are not
     * included.</p>
     *
     * @since 0.1.0
     */
    Optional<EnchantmentBehaviour> behaviour();

    /**
     * {@return this definition in its data pack JSON representation}
     *
     * @since 0.1.0
     */
    String toJson();

    /**
     * {@return a new builder pre-filled with the values of this definition}
     *
     * @since 0.1.0
     */
    Builder toBuilder();

    /**
     * An enchanting cost depending on the level, equal to
     * {@code base + perLevelAboveFirst * (level - 1)}.
     *
     * @since 0.1.0
     */
    @ApiStatus.NonExtendable
    interface Cost {

        /**
         * {@return the cost at level {@code 1}}
         *
         * @since 0.1.0
         */
        int base();

        /**
         * {@return the cost added by each level above the first}
         *
         * @since 0.1.0
         */
        int perLevelAboveFirst();

        /**
         * {@return the cost at {@code level}}
         *
         * @param level the level, starting at {@code 1}
         * @since 0.1.0
         */
        default int at(final int level) {
            return base() + perLevelAboveFirst() * (level - 1);
        }
    }

    /**
     * Builds an {@link EnchantmentDefinition}.
     *
     * <p>The supported items are mandatory. Other values default to: the translatable description
     * {@code enchantment.<namespace>.<value>}, a maximum level of {@code 1}, a weight of {@code 1}, an
     * anvil cost of {@code 1}, a minimum cost of {@code 1 + 10 per level}, a maximum cost of
     * {@code 21 + 10 per level} and the {@link EquipmentSlotGroup#ANY} slot group.</p>
     *
     * @since 0.1.0
     */
    @ApiStatus.NonExtendable
    interface Builder {

        /**
         * @param description the name of the enchantment, without its level
         * @return this builder
         * @since 0.1.0
         */
        Builder description(Component description);

        /**
         * @param tag the item tag the enchantment can be applied to, such as
         *            {@code minecraft:enchantable/weapon}
         * @return this builder
         * @since 0.1.0
         */
        Builder supportedItemTag(Key tag);

        /**
         * @param items the items the enchantment can be applied to
         * @return this builder
         * @throws IllegalArgumentException if {@code items} is empty
         * @since 0.1.0
         */
        Builder supportedItems(Collection<Key> items);

        /**
         * @param tag the item tag the enchanting table offers the enchantment for
         * @return this builder
         * @since 0.1.0
         */
        Builder primaryItemTag(Key tag);

        /**
         * @param items the items the enchanting table offers the enchantment for
         * @return this builder
         * @since 0.1.0
         */
        Builder primaryItems(Collection<Key> items);

        /**
         * Makes the enchantment incompatible with every enchantment of a tag, such as
         * {@code minecraft:exclusive_set/damage}. The tag is created if it does not exist.
         *
         * @param tag the enchantment tag
         * @return this builder
         * @since 0.1.0
         */
        Builder exclusiveSetTag(Key tag);

        /**
         * Makes the enchantment incompatible with the given enchantments.
         *
         * @param enchantments the incompatible enchantments
         * @return this builder
         * @since 0.1.0
         */
        Builder exclusiveWith(Collection<TypedKey<Enchantment>> enchantments);

        /**
         * @param maxLevel the maximum level, between {@code 1} and {@value EnchantmentDefinition#MAX_LEVEL}
         * @return this builder
         * @throws IllegalArgumentException if the level is out of range
         * @since 0.1.0
         */
        Builder maxLevel(int maxLevel);

        /**
         * @param weight the weight in random selections, between {@code 1} and
         *               {@value EnchantmentDefinition#MAX_WEIGHT}
         * @return this builder
         * @throws IllegalArgumentException if the weight is out of range
         * @since 0.1.0
         */
        Builder weight(int weight);

        /**
         * @param anvilCost the base anvil cost per level
         * @return this builder
         * @throws IllegalArgumentException if the cost is negative
         * @since 0.1.0
         */
        Builder anvilCost(int anvilCost);

        /**
         * @param base               the minimum cost at level {@code 1}
         * @param perLevelAboveFirst the cost added by each level above the first
         * @return this builder
         * @since 0.1.0
         */
        Builder minCost(int base, int perLevelAboveFirst);

        /**
         * @param base               the maximum cost at level {@code 1}
         * @param perLevelAboveFirst the cost added by each level above the first
         * @return this builder
         * @since 0.1.0
         */
        Builder maxCost(int base, int perLevelAboveFirst);

        /**
         * @param slots the equipment slots in which the enchantment is active
         * @return this builder
         * @throws IllegalArgumentException if {@code slots} is empty
         * @since 0.1.0
         */
        Builder slots(Collection<EquipmentSlotGroup> slots);

        /**
         * @param slots the equipment slots in which the enchantment is active
         * @return this builder
         * @throws IllegalArgumentException if no slot is given
         * @since 0.1.0
         */
        default Builder slots(final EquipmentSlotGroup... slots) {
            return slots(List.of(slots));
        }

        /**
         * Sets the effect components of the enchantment, in the format of the {@code effects} object of
         * a data pack enchantment, for instance
         * {@code {"minecraft:damage": [{"effect": {"type": "minecraft:add", "value": 2}}]}}.
         *
         * <p>Every component is sent to clients; the server runs the
         * {@linkplain EnchantmentRegistry#supportedEffectComponents() supported} ones.</p>
         *
         * @param json the {@code effects} JSON object, or {@code null} to remove every effect component
         * @return this builder
         * @throws IllegalArgumentException if the JSON is malformed or contains an unknown component
         * @since 0.1.0
         */
        Builder effectsJson(@Nullable String json);

        /**
         * @param behaviour the server-side behaviour of the enchantment
         * @return this builder
         * @since 0.1.0
         */
        Builder behaviour(EnchantmentBehaviour behaviour);

        /**
         * {@return the built definition}
         *
         * @throws IllegalStateException if no supported item was set
         * @since 0.1.0
         */
        EnchantmentDefinition build();
    }
}
