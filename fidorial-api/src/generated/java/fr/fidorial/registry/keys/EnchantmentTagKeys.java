package fr.fidorial.registry.keys;

import fr.fidorial.item.enchantment.EnchantmentDefinition;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TagKey;
import net.kyori.adventure.key.KeyPattern;

/**
 * Keys of the vanilla tags of the {@code minecraft:enchantment} registry.
 */
public final class EnchantmentTagKeys {
    /**
     * Tag {@code #minecraft:curse}.
     */
    public static final TagKey<EnchantmentDefinition> CURSE = create("curse");

    /**
     * Tag {@code #minecraft:double_trade_price}.
     */
    public static final TagKey<EnchantmentDefinition> DOUBLE_TRADE_PRICE = create("double_trade_price");

    /**
     * Tag {@code #minecraft:exclusive_set/armor}.
     */
    public static final TagKey<EnchantmentDefinition> EXCLUSIVE_SET_ARMOR = create("exclusive_set/armor");

    /**
     * Tag {@code #minecraft:exclusive_set/boots}.
     */
    public static final TagKey<EnchantmentDefinition> EXCLUSIVE_SET_BOOTS = create("exclusive_set/boots");

    /**
     * Tag {@code #minecraft:exclusive_set/bow}.
     */
    public static final TagKey<EnchantmentDefinition> EXCLUSIVE_SET_BOW = create("exclusive_set/bow");

    /**
     * Tag {@code #minecraft:exclusive_set/crossbow}.
     */
    public static final TagKey<EnchantmentDefinition> EXCLUSIVE_SET_CROSSBOW = create("exclusive_set/crossbow");

    /**
     * Tag {@code #minecraft:exclusive_set/damage}.
     */
    public static final TagKey<EnchantmentDefinition> EXCLUSIVE_SET_DAMAGE = create("exclusive_set/damage");

    /**
     * Tag {@code #minecraft:exclusive_set/mining}.
     */
    public static final TagKey<EnchantmentDefinition> EXCLUSIVE_SET_MINING = create("exclusive_set/mining");

    /**
     * Tag {@code #minecraft:exclusive_set/riptide}.
     */
    public static final TagKey<EnchantmentDefinition> EXCLUSIVE_SET_RIPTIDE = create("exclusive_set/riptide");

    /**
     * Tag {@code #minecraft:in_enchanting_table}.
     */
    public static final TagKey<EnchantmentDefinition> IN_ENCHANTING_TABLE = create("in_enchanting_table");

    /**
     * Tag {@code #minecraft:non_treasure}.
     */
    public static final TagKey<EnchantmentDefinition> NON_TREASURE = create("non_treasure");

    /**
     * Tag {@code #minecraft:on_mob_spawn_equipment}.
     */
    public static final TagKey<EnchantmentDefinition> ON_MOB_SPAWN_EQUIPMENT = create("on_mob_spawn_equipment");

    /**
     * Tag {@code #minecraft:on_random_loot}.
     */
    public static final TagKey<EnchantmentDefinition> ON_RANDOM_LOOT = create("on_random_loot");

    /**
     * Tag {@code #minecraft:on_traded_equipment}.
     */
    public static final TagKey<EnchantmentDefinition> ON_TRADED_EQUIPMENT = create("on_traded_equipment");

    /**
     * Tag {@code #minecraft:prevents_bee_spawns_when_mining}.
     */
    public static final TagKey<EnchantmentDefinition> PREVENTS_BEE_SPAWNS_WHEN_MINING = create("prevents_bee_spawns_when_mining");

    /**
     * Tag {@code #minecraft:prevents_decorated_pot_shattering}.
     */
    public static final TagKey<EnchantmentDefinition> PREVENTS_DECORATED_POT_SHATTERING = create("prevents_decorated_pot_shattering");

    /**
     * Tag {@code #minecraft:prevents_ice_melting}.
     */
    public static final TagKey<EnchantmentDefinition> PREVENTS_ICE_MELTING = create("prevents_ice_melting");

    /**
     * Tag {@code #minecraft:prevents_infested_spawns}.
     */
    public static final TagKey<EnchantmentDefinition> PREVENTS_INFESTED_SPAWNS = create("prevents_infested_spawns");

    /**
     * Tag {@code #minecraft:smelts_loot}.
     */
    public static final TagKey<EnchantmentDefinition> SMELTS_LOOT = create("smelts_loot");

    /**
     * Tag {@code #minecraft:tooltip_order}.
     */
    public static final TagKey<EnchantmentDefinition> TOOLTIP_ORDER = create("tooltip_order");

    /**
     * Tag {@code #minecraft:tradeable}.
     */
    public static final TagKey<EnchantmentDefinition> TRADEABLE = create("tradeable");

    /**
     * Tag {@code #minecraft:treasure}.
     */
    public static final TagKey<EnchantmentDefinition> TREASURE = create("treasure");

    private EnchantmentTagKeys() {
        throw new UnsupportedOperationException("EnchantmentTagKeys cannot be instantiated.");
    }

    private static TagKey<EnchantmentDefinition> create(@KeyPattern final String value) {
        return TagKey.create(RegistryKey.ENCHANTMENT, value);
    }
}
