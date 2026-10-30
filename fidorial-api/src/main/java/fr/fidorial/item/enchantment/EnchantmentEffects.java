package fr.fidorial.item.enchantment;

import net.kyori.adventure.key.Key;

/**
 * The vanilla effect components an enchantment may declare in its {@code effects} object.
 *
 * <p>Every component is sent to clients. Only the components listed by
 * {@link Enchantments#supportedEffectComponents()} are run by the server.</p>
 *
 * @see EnchantmentDefinition#hasEffect(Key)
 * @since 0.1.0
 */
public interface EnchantmentEffects {

    /**
     * The fraction of the damage absorbed by the armor of the victim, such as Breach.
     *
     * @since 0.1.0
     */
    Key ARMOR_EFFECTIVENESS = Key.key("armor_effectiveness");

    /**
     * The attribute modifiers granted while equipped, such as Efficiency.
     *
     * @since 0.1.0
     */
    Key ATTRIBUTES = Key.key("attributes");

    /**
     * The damage of a melee attack, such as Sharpness.
     *
     * @since 0.1.0
     */
    Key DAMAGE = Key.key("damage");

    /**
     * The damage ignored by the wearer, such as Frost Walker on magma blocks.
     *
     * @since 0.1.0
     */
    Key DAMAGE_IMMUNITY = Key.key("damage_immunity");

    /**
     * The protection points against damage, such as Protection.
     *
     * @since 0.1.0
     */
    Key DAMAGE_PROTECTION = Key.key("damage_protection");

    /**
     * The knockback of a melee attack, such as Knockback.
     *
     * @since 0.1.0
     */
    Key KNOCKBACK = Key.key("knockback");

    /**
     * The effects applied after an attack, such as Fire Aspect or Thorns.
     *
     * @since 0.1.0
     */
    Key POST_ATTACK = Key.key("post_attack");

    /**
     * Prevents the stack from being removed from its armor slot, such as Curse of Binding.
     *
     * @since 0.1.0
     */
    Key PREVENT_ARMOR_CHANGE = Key.key("prevent_armor_change");

    /**
     * Destroys the stack instead of dropping it on death, such as Curse of Vanishing.
     *
     * @since 0.1.0
     */
    Key PREVENT_EQUIPMENT_DROP = Key.key("prevent_equipment_drop");

    /**
     * The effects applied every tick while equipped, such as Soul Speed.
     *
     * @since 0.1.0
     */
    Key TICK = Key.key("tick");

    /**
     * The ammunition used by a shot, such as Infinity.
     *
     * @since 0.1.0
     */
    Key AMMO_USE = Key.key("ammo_use");

    /**
     * The experience dropped by mined blocks, such as Silk Touch.
     *
     * @since 0.1.0
     */
    Key BLOCK_EXPERIENCE = Key.key("block_experience");

    /**
     * The charge time of a crossbow, such as Quick Charge.
     *
     * @since 0.1.0
     */
    Key CROSSBOW_CHARGE_TIME = Key.key("crossbow_charge_time");

    /**
     * The sounds of a charging crossbow, such as Quick Charge.
     *
     * @since 0.1.0
     */
    Key CROSSBOW_CHARGING_SOUNDS = Key.key("crossbow_charging_sounds");

    /**
     * The chance for a killed mob to drop its equipment, such as Looting.
     *
     * @since 0.1.0
     */
    Key EQUIPMENT_DROPS = Key.key("equipment_drops");

    /**
     * The luck when fishing, such as Luck of the Sea.
     *
     * @since 0.1.0
     */
    Key FISHING_LUCK_BONUS = Key.key("fishing_luck_bonus");

    /**
     * The time before a fish bites, such as Lure.
     *
     * @since 0.1.0
     */
    Key FISHING_TIME_REDUCTION = Key.key("fishing_time_reduction");

    /**
     * The effects applied when a projectile hits a block, such as Channeling.
     *
     * @since 0.1.0
     */
    Key HIT_BLOCK = Key.key("hit_block");

    /**
     * The durability lost by the stack, such as Unbreaking.
     *
     * @since 0.1.0
     */
    Key ITEM_DAMAGE = Key.key("item_damage");

    /**
     * The effects applied when the wearer moves, such as Frost Walker.
     *
     * @since 0.1.0
     */
    Key LOCATION_CHANGED = Key.key("location_changed");

    /**
     * The experience dropped by killed mobs.
     *
     * @since 0.1.0
     */
    Key MOB_EXPERIENCE = Key.key("mob_experience");

    /**
     * The effects applied after a piercing attack, such as Lunge.
     *
     * @since 0.1.0
     */
    Key POST_PIERCING_ATTACK = Key.key("post_piercing_attack");

    /**
     * The number of projectiles fired by a shot, such as Multishot.
     *
     * @since 0.1.0
     */
    Key PROJECTILE_COUNT = Key.key("projectile_count");

    /**
     * The number of entities a projectile goes through, such as Piercing.
     *
     * @since 0.1.0
     */
    Key PROJECTILE_PIERCING = Key.key("projectile_piercing");

    /**
     * The effects applied to a fired projectile, such as Flame.
     *
     * @since 0.1.0
     */
    Key PROJECTILE_SPAWNED = Key.key("projectile_spawned");

    /**
     * The spread of the projectiles of a shot, such as Multishot.
     *
     * @since 0.1.0
     */
    Key PROJECTILE_SPREAD = Key.key("projectile_spread");

    /**
     * The durability repaired by experience orbs, such as Mending.
     *
     * @since 0.1.0
     */
    Key REPAIR_WITH_XP = Key.key("repair_with_xp");

    /**
     * The bonus damage of a mace per fallen block, such as Density.
     *
     * @since 0.1.0
     */
    Key SMASH_DAMAGE_PER_FALLEN_BLOCK = Key.key("smash_damage_per_fallen_block");

    /**
     * The return speed of a thrown trident, such as Loyalty.
     *
     * @since 0.1.0
     */
    Key TRIDENT_RETURN_ACCELERATION = Key.key("trident_return_acceleration");

    /**
     * The sound of a riptide, such as Riptide.
     *
     * @since 0.1.0
     */
    Key TRIDENT_SOUND = Key.key("trident_sound");

    /**
     * The strength of a riptide, such as Riptide.
     *
     * @since 0.1.0
     */
    Key TRIDENT_SPIN_ATTACK_STRENGTH = Key.key("trident_spin_attack_strength");
}
