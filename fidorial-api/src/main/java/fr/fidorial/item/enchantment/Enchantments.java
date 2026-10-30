package fr.fidorial.item.enchantment;

import fr.fidorial.entity.LivingEntity;
import fr.fidorial.item.ItemStack;
import fr.fidorial.plugin.Plugin;
import fr.fidorial.registry.GameRegistries;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Contract;

import java.util.Set;

/**
 * The behaviour of the enchantments, and the helpers applying them to stacks.
 *
 * <p>The enchantments themselves are held by the {@link RegistryKey#ENCHANTMENT} registry: a
 * definition built by {@link #define(TypedKey)} is registered through
 * {@link GameRegistries#writable(RegistryKey)}.</p>
 *
 * @see GameRegistries#enchantments()
 * @since 0.1.0
 */
public interface Enchantments {

    /**
     * {@return a new builder for the enchantment identified by {@code key}}
     *
     * @param key the key of the enchantment
     * @since 0.1.0
     */
    EnchantmentDefinition.Builder define(TypedKey<EnchantmentDefinition> key);

    /**
     * Adds a behaviour to an enchantment, on top of its existing ones.
     *
     * @param key       the key of the enchantment, vanilla or not
     * @param behaviour the behaviour to add
     * @param owner     the plugin adding it
     * @throws IllegalArgumentException if no enchantment is registered under {@code key}
     * @since 0.1.0
     */
    void attach(TypedKey<EnchantmentDefinition> key, EnchantmentBehaviour behaviour, Plugin owner);

    /**
     * Removes the behaviours a plugin added to an enchantment.
     *
     * @param key   the key of the enchantment
     * @param owner the plugin that added them
     * @return {@code true} if at least one behaviour was removed
     * @since 0.1.0
     */
    boolean detach(TypedKey<EnchantmentDefinition> key, Plugin owner);

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
     * {@return whether {@code enchantment} can be applied to {@code item}}
     *
     * <p>An item registered by a plugin is also tested as the vanilla item it is
     * {@linkplain fr.fidorial.item.ItemDefinition#networkType() rendered as}.</p>
     *
     * @param enchantment the key of the enchantment
     * @param item        the item, such as {@code minecraft:diamond_sword}
     * @since 0.1.0
     */
    boolean isSupportedItem(TypedKey<EnchantmentDefinition> enchantment, Key item);

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
    boolean areCompatible(TypedKey<EnchantmentDefinition> first, TypedKey<EnchantmentDefinition> second);

    /**
     * {@return whether {@code enchantment} can be applied to {@code stack}, as {@code /enchant} checks
     * it: the item is supported and the enchantment is compatible with those already on the stack}
     *
     * @param stack       the stack
     * @param enchantment the key of the enchantment
     * @since 0.1.0
     */
    boolean canEnchant(ItemStack stack, TypedKey<EnchantmentDefinition> enchantment);

    /**
     * {@return the level of {@code enchantment} on {@code stack}, or {@code 0} if absent}
     *
     * @param stack       the stack
     * @param enchantment the key of the enchantment
     * @since 0.1.0
     */
    int level(ItemStack stack, TypedKey<EnchantmentDefinition> enchantment);

    /**
     * {@return the highest level of {@code enchantment} among the stacks {@code entity} wears or holds in
     * the slots of the enchantment, or {@code 0} if absent}
     *
     * @param entity      the entity
     * @param enchantment the key of the enchantment
     * @since 0.1.0
     */
    int level(LivingEntity entity, TypedKey<EnchantmentDefinition> enchantment);

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
    ItemStack enchant(ItemStack stack, TypedKey<EnchantmentDefinition> enchantment, int level);

    /**
     * Removes an enchantment from a stack.
     *
     * @param stack       the stack
     * @param enchantment the key of the enchantment
     * @return the stack without the enchantment, or {@code stack} if it was absent
     * @since 0.1.0
     */
    ItemStack disenchant(ItemStack stack, TypedKey<EnchantmentDefinition> enchantment);

    /**
     * {@return an enchanted book storing {@code enchantment}}
     *
     * @param enchantment the key of the enchantment
     * @param level       the level, between {@code 1} and {@value EnchantmentDefinition#MAX_LEVEL}
     * @throws IllegalArgumentException if the enchantment is not registered or the level is out of range
     * @since 0.1.0
     */
    ItemStack enchantedBook(TypedKey<EnchantmentDefinition> enchantment, int level);

    /**
     * {@return the name of an enchantment followed by its level, as displayed in item tooltips}
     *
     * <p>The name is gray, or red for a curse ({@code #minecraft:curse}). The level is omitted
     * when both the level and the maximum level are {@code 1}.</p>
     *
     * @param enchantment the key of the enchantment
     * @param level       the level
     * @since 0.1.0
     */
    Component displayName(TypedKey<EnchantmentDefinition> enchantment, int level);
}
