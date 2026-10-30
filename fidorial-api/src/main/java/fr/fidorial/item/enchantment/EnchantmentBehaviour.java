package fr.fidorial.item.enchantment;

import fr.fidorial.attribute.AttributeModifier;
import fr.fidorial.combat.DamageSource;
import fr.fidorial.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Server-side behaviour of an enchantment.
 *
 * <p>The behaviours of an enchantment run in the following order: the
 * {@linkplain Enchantments#supportedEffectComponents() supported} effect components of its
 * data, the behaviour of its {@linkplain EnchantmentDefinition.Builder#behaviour definition}, then the
 * behaviours {@linkplain Enchantments#attach attached} to it. Each modifying hook receives the
 * value returned by the previous one.</p>
 *
 * <p>Hooks related to an attack only consider the weapon held in the main hand. Other hooks consider
 * every stack worn or held in one of the {@linkplain EnchantmentDefinition#slots() slots} of the
 * enchantment.</p>
 *
 * <p>Every hook does nothing by default. Hooks run on the thread of the region owning
 * {@link EnchantedItem#owner()}; an exception thrown by a hook is logged and ignored.</p>
 *
 * @since 0.1.0
 */
public interface EnchantmentBehaviour {

    /**
     * Modifies the damage of a melee attack dealt with the enchanted weapon, like
     * {@code minecraft:damage}.
     *
     * <p>The difference with the base damage is scaled by the attack strength and is not affected by
     * critical hits.</p>
     *
     * @param item   the enchanted weapon
     * @param victim the entity attacked
     * @param source the source of the damage
     * @param damage the current damage
     * @return the modified damage
     * @since 0.1.0
     */
    default float modifyDamage(final EnchantedItem item, final LivingEntity victim,
                               final DamageSource source, final float damage) {
        return damage;
    }

    /**
     * Modifies the protection points against damage taken by the wearer, like
     * {@code minecraft:damage_protection}.
     *
     * <p>Protection points are summed over all stacks and capped at {@code 20}; each point reduces the
     * damage by {@code 4%}.</p>
     *
     * @param item       the enchanted stack
     * @param source     the source of the damage
     * @param protection the current protection points
     * @return the modified protection points
     * @since 0.1.0
     */
    default float modifyProtection(final EnchantedItem item, final DamageSource source, final float protection) {
        return protection;
    }

    /**
     * Tests whether the wearer ignores the damage entirely, like {@code minecraft:damage_immunity}.
     *
     * @param item   the enchanted stack
     * @param source the source of the damage
     * @return {@code true} to cancel the damage
     * @since 0.1.0
     */
    default boolean isImmuneTo(final EnchantedItem item, final DamageSource source) {
        return false;
    }

    /**
     * Modifies the knockback bonus of a melee attack dealt with the enchanted weapon, like
     * {@code minecraft:knockback}.
     *
     * <p>The bonus uses the vanilla scale: Knockback I adds {@code 1}, and each point adds
     * {@code 0.5} to the knockback strength.</p>
     *
     * @param item      the enchanted weapon
     * @param victim    the entity attacked
     * @param source    the source of the damage
     * @param knockback the current knockback bonus, {@code 0} by default
     * @return the modified knockback bonus
     * @since 0.1.0
     */
    default float modifyKnockback(final EnchantedItem item, final LivingEntity victim,
                                  final DamageSource source, final float knockback) {
        return knockback;
    }

    /**
     * Modifies the fraction of the damage absorbed by the armor of the victim, like
     * {@code minecraft:armor_effectiveness}.
     *
     * @param item          the enchanted weapon
     * @param victim        the entity attacked
     * @param source        the source of the damage
     * @param effectiveness the current fraction, between {@code 0} and {@code 1}
     * @return the modified fraction, clamped between {@code 0} and {@code 1} once every hook ran
     * @since 0.1.0
     */
    default float modifyArmorEffectiveness(final EnchantedItem item, final LivingEntity victim,
                                           final DamageSource source, final float effectiveness) {
        return effectiveness;
    }

    /**
     * Called after the owner damaged an entity with a melee attack, like the
     * {@code minecraft:post_attack} effects targeting the attacker.
     *
     * @param item   the enchanted stack of the attacker
     * @param victim the entity damaged
     * @param source the source of the damage
     * @param damage the damage dealt
     * @since 0.1.0
     */
    default void onAttack(final EnchantedItem item, final LivingEntity victim,
                          final DamageSource source, final float damage) {
    }

    /**
     * Called after the owner was damaged by an attack, like the {@code minecraft:post_attack} effects
     * targeting the victim.
     *
     * @param item     the enchanted stack of the victim
     * @param attacker the living entity responsible for the attack, or {@code null}
     * @param source   the source of the damage
     * @param damage   the damage taken
     * @since 0.1.0
     */
    default void onHurt(final EnchantedItem item, final @Nullable LivingEntity attacker,
                        final DamageSource source, final float damage) {
    }

    /**
     * Called every tick while the stack is worn or held in one of the slots of the enchantment, like
     * {@code minecraft:tick}.
     *
     * @param item the enchanted stack
     * @since 0.1.0
     */
    default void onTick(final EnchantedItem item) {
    }

    /**
     * Called when the stack starts being worn or held in one of the slots of the enchantment, including
     * when the owner joins the server.
     *
     * @param item the enchanted stack
     * @since 0.1.0
     */
    default void onEquip(final EnchantedItem item) {
    }

    /**
     * Called when the stack stops being worn or held in one of the slots of the enchantment, when it is
     * modified, or when the owner leaves the server or dies.
     *
     * @param item the enchanted stack, as it was while equipped
     * @since 0.1.0
     */
    default void onUnequip(final EnchantedItem item) {
    }

    /**
     * Gets the attribute modifiers granted while the stack is worn or held in one of the slots of the
     * enchantment, like {@code minecraft:attributes}. They are synchronised with the client of a
     * player.
     *
     * <p>{@link AttributeModifier#id()} must be unique per attribute; the server appends the slot to it.
     * {@link AttributeModifier#slot()} is ignored.</p>
     *
     * @param item the enchanted stack
     * @return the attribute modifiers, possibly empty
     * @since 0.1.0
     */
    default List<AttributeModifier> attributeModifiers(final EnchantedItem item) {
        return List.of();
    }

    /**
     * Tests whether a player outside of creative mode is prevented from removing the stack from its
     * armor slot, like {@code minecraft:prevent_armor_change}.
     *
     * @param item the enchanted stack, worn in an armor slot
     * @return {@code true} to keep the stack in its slot
     * @since 0.1.0
     */
    default boolean preventsArmorChange(final EnchantedItem item) {
        return false;
    }

    /**
     * Tests whether the stack is destroyed instead of dropped when the owner dies without keeping their
     * inventory, like {@code minecraft:prevent_equipment_drop}.
     *
     * @param item the enchanted stack, anywhere in the inventory
     * @return {@code true} to destroy the stack
     * @since 0.1.0
     */
    default boolean preventsDrop(final EnchantedItem item) {
        return false;
    }
}
