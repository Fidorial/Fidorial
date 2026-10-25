package fr.fidorial.item.enchantment;

import fr.fidorial.registry.TypedKey;
import net.kyori.adventure.key.Key;

/**
 * The vanilla tags of the {@code minecraft:enchantment} registry.
 *
 * @see EnchantmentRegistry#addToTag(Key, TypedKey)
 * @since 0.1.0
 */
public interface EnchantmentTags {

    /**
     * Curses, displayed in red in item tooltips.
     *
     * @since 0.1.0
     */
    Key CURSE = Key.key("curse");

    /**
     * Enchantments whose enchanted books cost twice as much in villager trades.
     *
     * @since 0.1.0
     */
    Key DOUBLE_TRADE_PRICE = Key.key("double_trade_price");

    /**
     * Enchantments the enchanting table may offer.
     *
     * @since 0.1.0
     */
    Key IN_ENCHANTING_TABLE = Key.key("in_enchanting_table");

    /**
     * Enchantments that are not treasure enchantments.
     *
     * @since 0.1.0
     */
    Key NON_TREASURE = Key.key("non_treasure");

    /**
     * Enchantments that may be found on the equipment of spawning mobs.
     *
     * @since 0.1.0
     */
    Key ON_MOB_SPAWN_EQUIPMENT = Key.key("on_mob_spawn_equipment");

    /**
     * Enchantments that may be found on random loot.
     *
     * @since 0.1.0
     */
    Key ON_RANDOM_LOOT = Key.key("on_random_loot");

    /**
     * Enchantments that may be found on the equipment sold by villagers.
     *
     * @since 0.1.0
     */
    Key ON_TRADED_EQUIPMENT = Key.key("on_traded_equipment");

    /**
     * Enchantments preventing bees from leaving a mined nest.
     *
     * @since 0.1.0
     */
    Key PREVENTS_BEE_SPAWNS_WHEN_MINING = Key.key("prevents_bee_spawns_when_mining");

    /**
     * Enchantments preventing a mined decorated pot from shattering.
     *
     * @since 0.1.0
     */
    Key PREVENTS_DECORATED_POT_SHATTERING = Key.key("prevents_decorated_pot_shattering");

    /**
     * Enchantments preventing mined ice from turning into water.
     *
     * @since 0.1.0
     */
    Key PREVENTS_ICE_MELTING = Key.key("prevents_ice_melting");

    /**
     * Enchantments preventing silverfish from leaving a mined infested block.
     *
     * @since 0.1.0
     */
    Key PREVENTS_INFESTED_SPAWNS = Key.key("prevents_infested_spawns");

    /**
     * Enchantments smelting the loot of killed mobs.
     *
     * @since 0.1.0
     */
    Key SMELTS_LOOT = Key.key("smelts_loot");

    /**
     * The order of the enchantments in item tooltips; enchantments outside this tag are listed last.
     *
     * @since 0.1.0
     */
    Key TOOLTIP_ORDER = Key.key("tooltip_order");

    /**
     * Enchantments that may be sold by villagers on enchanted books.
     *
     * @since 0.1.0
     */
    Key TRADEABLE = Key.key("tradeable");

    /**
     * Treasure enchantments, never offered by the enchanting table.
     *
     * @since 0.1.0
     */
    Key TREASURE = Key.key("treasure");

    /**
     * Protection enchantments, mutually exclusive.
     *
     * @since 0.1.0
     */
    Key EXCLUSIVE_SET_ARMOR = Key.key("exclusive_set/armor");

    /**
     * Boots enchantments, mutually exclusive.
     *
     * @since 0.1.0
     */
    Key EXCLUSIVE_SET_BOOTS = Key.key("exclusive_set/boots");

    /**
     * Bow enchantments, mutually exclusive.
     *
     * @since 0.1.0
     */
    Key EXCLUSIVE_SET_BOW = Key.key("exclusive_set/bow");

    /**
     * Crossbow enchantments, mutually exclusive.
     *
     * @since 0.1.0
     */
    Key EXCLUSIVE_SET_CROSSBOW = Key.key("exclusive_set/crossbow");

    /**
     * Damage enchantments, mutually exclusive.
     *
     * @since 0.1.0
     */
    Key EXCLUSIVE_SET_DAMAGE = Key.key("exclusive_set/damage");

    /**
     * Mining enchantments, mutually exclusive.
     *
     * @since 0.1.0
     */
    Key EXCLUSIVE_SET_MINING = Key.key("exclusive_set/mining");

    /**
     * Trident enchantments incompatible with Riptide.
     *
     * @since 0.1.0
     */
    Key EXCLUSIVE_SET_RIPTIDE = Key.key("exclusive_set/riptide");
}
