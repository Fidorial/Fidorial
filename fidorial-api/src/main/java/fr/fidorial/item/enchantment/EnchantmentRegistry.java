package fr.fidorial.item.enchantment;

import fr.fidorial.entity.LivingEntity;
import fr.fidorial.item.ItemStack;
import fr.fidorial.plugin.Plugin;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.Enchantment;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Contract;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/**
 * Server-wide registry of enchantments, holding both the vanilla ones and those added by plugins.
 *
 * <p>Enchantments are synchronised with clients during the configuration phase. They should be
 * registered while the plugin loads: changing the registry once players are connected sends them back
 * to the configuration phase.</p>
 *
 * @since 0.1.0
 */
public interface EnchantmentRegistry {

    /**
     * {@return a new builder for the enchantment identified by {@code key}}
     *
     * @param key the key of the enchantment
     * @since 0.1.0
     */
    EnchantmentDefinition.Builder define(TypedKey<Enchantment> key);

    /**
     * Registers a new enchantment.
     *
     * @param definition the enchantment to add
     * @param owner      the plugin registering it
     * @return the registered definition
     * @throws IllegalArgumentException if an enchantment is already registered under the same key; use
     *                                  {@link #overwrite(EnchantmentDefinition, Plugin)} to replace it on
     *                                  purpose
     * @since 0.1.0
     */
    @Contract("_, _ -> param1")
    EnchantmentDefinition register(EnchantmentDefinition definition, Plugin owner);

    /**
     * Reads an enchantment from its data pack JSON representation and registers it.
     *
     * @param key   the key to register the enchantment under
     * @param json  the JSON object describing the enchantment
     * @param owner the plugin registering it
     * @return the registered definition
     * @throws IllegalArgumentException if the JSON is malformed, misses a mandatory field, contains an
     *                                  unknown effect component, or if an enchantment is already
     *                                  registered under the same key
     * @since 0.1.0
     */
    EnchantmentDefinition registerFromJson(TypedKey<Enchantment> key, String json, Plugin owner);

    /**
     * Registers an enchantment, replacing any existing one sharing its key, vanilla ones included.
     *
     * @param definition the enchantment to add or replace
     * @param owner      the plugin registering it
     * @return the definition previously registered under that key, if any
     * @since 0.1.0
     */
    Optional<EnchantmentDefinition> overwrite(EnchantmentDefinition definition, Plugin owner);

    /**
     * Removes an enchantment added by a plugin, or restores the vanilla definition of an overwritten
     * vanilla enchantment.
     *
     * <p>Stacks keep the removed enchantment, which has no effect until it is registered again.</p>
     *
     * @param key the key of the enchantment
     * @return {@code true} if an enchantment was removed or restored, {@code false} otherwise
     * @since 0.1.0
     */
    boolean unregister(TypedKey<Enchantment> key);

    /**
     * Removes every enchantment, overwrite and behaviour registered by a plugin.
     *
     * @param owner the plugin to clean up after
     * @since 0.1.0
     */
    void unregisterAll(Plugin owner);

    /**
     * Adds a behaviour to an enchantment, on top of its existing ones.
     *
     * @param key       the key of the enchantment, vanilla or not
     * @param behaviour the behaviour to add
     * @param owner     the plugin adding it
     * @throws IllegalArgumentException if no enchantment is registered under {@code key}
     * @since 0.1.0
     */
    void attach(TypedKey<Enchantment> key, EnchantmentBehaviour behaviour, Plugin owner);

    /**
     * Removes the behaviours a plugin added to an enchantment.
     *
     * @param key   the key of the enchantment
     * @param owner the plugin that added them
     * @return {@code true} if at least one behaviour was removed
     * @since 0.1.0
     */
    boolean detach(TypedKey<Enchantment> key, Plugin owner);

    /**
     * {@return the definition registered under {@code key}, vanilla enchantments included}
     *
     * @param key the key of the enchantment
     * @since 0.1.0
     */
    Optional<EnchantmentDefinition> definition(TypedKey<Enchantment> key);

    /**
     * {@return every registered enchantment, in network order}
     *
     * @since 0.1.0
     */
    @Contract(pure = true)
    Collection<EnchantmentDefinition> definitions();

    /**
     * {@return every registered enchantment key, in network order}
     *
     * @since 0.1.0
     */
    @Contract(pure = true)
    Collection<TypedKey<Enchantment>> keys();

    /**
     * {@return whether an enchantment is registered under {@code key}}
     *
     * @param key the key of the enchantment
     * @since 0.1.0
     */
    boolean contains(TypedKey<Enchantment> key);

    /**
     * {@return whether {@code key} maps to an enchantment registered or overwritten by a plugin rather
     * than a stock vanilla one}
     *
     * @param key the key of the enchantment
     * @since 0.1.0
     */
    boolean isCustom(TypedKey<Enchantment> key);

    /**
     * {@return the network identifier of {@code key}, or {@code -1} if it is not registered}
     *
     * @param key the key of the enchantment
     * @since 0.1.0
     */
    int networkId(TypedKey<Enchantment> key);

    /**
     * {@return the effect components run by the server}
     *
     * <p>Other components are still sent to clients, but have no effect on the server.</p>
     *
     * @see EnchantmentEffects
     * @since 0.1.0
     */
    @Contract(pure = true)
    Set<Key> supportedEffectComponents();

    /**
     * {@return every enchantment tag}
     *
     * @see EnchantmentTags
     * @since 0.1.0
     */
    @Contract(pure = true)
    Collection<Key> tags();

    /**
     * {@return the enchantments of {@code tag}, in order, or an empty collection if the tag does not
     * exist}
     *
     * @param tag the tag, such as {@link EnchantmentTags#IN_ENCHANTING_TABLE}
     * @since 0.1.0
     */
    Collection<TypedKey<Enchantment>> tag(Key tag);

    /**
     * {@return whether {@code enchantment} belongs to {@code tag}}
     *
     * @param enchantment the key of the enchantment
     * @param tag         the tag
     * @since 0.1.0
     */
    boolean isTagged(TypedKey<Enchantment> enchantment, Key tag);

    /**
     * Adds an enchantment to a tag, creating the tag if needed.
     *
     * @param tag         the tag, such as {@link EnchantmentTags#IN_ENCHANTING_TABLE}
     * @param enchantment the key of the enchantment
     * @return {@code true} if the enchantment was added, {@code false} if it already belonged to the tag
     * @throws IllegalArgumentException if no enchantment is registered under {@code enchantment}
     * @since 0.1.0
     */
    boolean addToTag(Key tag, TypedKey<Enchantment> enchantment);

    /**
     * Removes an enchantment from a tag.
     *
     * @param tag         the tag
     * @param enchantment the key of the enchantment
     * @return {@code true} if the enchantment was removed, {@code false} if it did not belong to the tag
     * @since 0.1.0
     */
    boolean removeFromTag(Key tag, TypedKey<Enchantment> enchantment);

    /**
     * {@return whether {@code enchantment} can be applied to {@code item}}
     *
     * <p>An item registered by a plugin is also tested as the vanilla item it is
     * {@linkplain fr.fidorial.item.ItemDefinition#networkType() rendered as}.</p>
     *
     * @param enchantment the key of the enchantment
     * @param item        the item, such as {@code minecraft:diamond_sword}
     * @since 0.1.0
     */
    boolean isSupportedItem(TypedKey<Enchantment> enchantment, Key item);

    /**
     * {@return whether the two enchantments can be applied to the same stack}
     *
     * <p>Two enchantments are compatible when they differ and neither belongs to the
     * {@linkplain EnchantmentDefinition#exclusiveSet() exclusive set} of the other.</p>
     *
     * @param first  the key of an enchantment
     * @param second the key of another enchantment
     * @since 0.1.0
     */
    boolean areCompatible(TypedKey<Enchantment> first, TypedKey<Enchantment> second);

    /**
     * {@return whether {@code enchantment} can be applied to {@code stack}, as {@code /enchant} checks
     * it: the item is supported and the enchantment is compatible with those already on the stack}
     *
     * @param stack       the stack
     * @param enchantment the key of the enchantment
     * @since 0.1.0
     */
    boolean canEnchant(ItemStack stack, TypedKey<Enchantment> enchantment);

    /**
     * {@return the level of {@code enchantment} on {@code stack}, or {@code 0} if absent}
     *
     * @param stack       the stack
     * @param enchantment the key of the enchantment
     * @since 0.1.0
     */
    int level(ItemStack stack, TypedKey<Enchantment> enchantment);

    /**
     * {@return the highest level of {@code enchantment} among the stacks {@code entity} wears or holds in
     * the slots of the enchantment, or {@code 0} if absent}
     *
     * @param entity      the entity
     * @param enchantment the key of the enchantment
     * @since 0.1.0
     */
    int level(LivingEntity entity, TypedKey<Enchantment> enchantment);

    /**
     * Applies an enchantment to a stack, or changes its level. Enchanted books receive it as a stored
     * enchantment.
     *
     * <p>No compatibility check is performed; see {@link #canEnchant(ItemStack, TypedKey)}.</p>
     *
     * @param stack       the stack
     * @param enchantment the key of the enchantment
     * @param level       the level, between {@code 1} and {@value EnchantmentDefinition#MAX_LEVEL}
     * @return the enchanted stack
     * @throws IllegalArgumentException if the enchantment is not registered or the level is out of range
     * @since 0.1.0
     */
    ItemStack enchant(ItemStack stack, TypedKey<Enchantment> enchantment, int level);

    /**
     * Removes an enchantment from a stack.
     *
     * @param stack       the stack
     * @param enchantment the key of the enchantment
     * @return the stack without the enchantment, or {@code stack} if it was absent
     * @since 0.1.0
     */
    ItemStack disenchant(ItemStack stack, TypedKey<Enchantment> enchantment);

    /**
     * {@return an enchanted book storing {@code enchantment}}
     *
     * @param enchantment the key of the enchantment
     * @param level       the level, between {@code 1} and {@value EnchantmentDefinition#MAX_LEVEL}
     * @throws IllegalArgumentException if the enchantment is not registered or the level is out of range
     * @since 0.1.0
     */
    ItemStack enchantedBook(TypedKey<Enchantment> enchantment, int level);

    /**
     * {@return the name of an enchantment followed by its level, as displayed in item tooltips}
     *
     * <p>The name is gray, or red for a {@linkplain EnchantmentTags#CURSE curse}. The level is omitted
     * when both the level and the maximum level are {@code 1}.</p>
     *
     * @param enchantment the key of the enchantment
     * @param level       the level
     * @since 0.1.0
     */
    Component displayName(TypedKey<Enchantment> enchantment, int level);
}
