package fr.fidorial.registry.keys;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TagKey;
import fr.fidorial.registry.data.DamageType;
import net.kyori.adventure.key.KeyPattern;

/**
 * Keys of the vanilla tags of the {@code minecraft:damage_type} registry.
 */
public final class DamageTypeTagKeys {
    /**
     * Tag {@code #minecraft:always_hurts_ender_dragons}.
     */
    public static final TagKey<DamageType> ALWAYS_HURTS_ENDER_DRAGONS = create("always_hurts_ender_dragons");

    /**
     * Tag {@code #minecraft:always_kills_armor_stands}.
     */
    public static final TagKey<DamageType> ALWAYS_KILLS_ARMOR_STANDS = create("always_kills_armor_stands");

    /**
     * Tag {@code #minecraft:always_most_significant_fall}.
     */
    public static final TagKey<DamageType> ALWAYS_MOST_SIGNIFICANT_FALL = create("always_most_significant_fall");

    /**
     * Tag {@code #minecraft:always_triggers_silverfish}.
     */
    public static final TagKey<DamageType> ALWAYS_TRIGGERS_SILVERFISH = create("always_triggers_silverfish");

    /**
     * Tag {@code #minecraft:avoids_guardian_thorns}.
     */
    public static final TagKey<DamageType> AVOIDS_GUARDIAN_THORNS = create("avoids_guardian_thorns");

    /**
     * Tag {@code #minecraft:burn_from_stepping}.
     */
    public static final TagKey<DamageType> BURN_FROM_STEPPING = create("burn_from_stepping");

    /**
     * Tag {@code #minecraft:burns_armor_stands}.
     */
    public static final TagKey<DamageType> BURNS_ARMOR_STANDS = create("burns_armor_stands");

    /**
     * Tag {@code #minecraft:bypasses_armor}.
     */
    public static final TagKey<DamageType> BYPASSES_ARMOR = create("bypasses_armor");

    /**
     * Tag {@code #minecraft:bypasses_cooldown}.
     */
    public static final TagKey<DamageType> BYPASSES_COOLDOWN = create("bypasses_cooldown");

    /**
     * Tag {@code #minecraft:bypasses_effects}.
     */
    public static final TagKey<DamageType> BYPASSES_EFFECTS = create("bypasses_effects");

    /**
     * Tag {@code #minecraft:bypasses_enchantments}.
     */
    public static final TagKey<DamageType> BYPASSES_ENCHANTMENTS = create("bypasses_enchantments");

    /**
     * Tag {@code #minecraft:bypasses_invulnerability}.
     */
    public static final TagKey<DamageType> BYPASSES_INVULNERABILITY = create("bypasses_invulnerability");

    /**
     * Tag {@code #minecraft:bypasses_resistance}.
     */
    public static final TagKey<DamageType> BYPASSES_RESISTANCE = create("bypasses_resistance");

    /**
     * Tag {@code #minecraft:bypasses_shield}.
     */
    public static final TagKey<DamageType> BYPASSES_SHIELD = create("bypasses_shield");

    /**
     * Tag {@code #minecraft:bypasses_wolf_armor}.
     */
    public static final TagKey<DamageType> BYPASSES_WOLF_ARMOR = create("bypasses_wolf_armor");

    /**
     * Tag {@code #minecraft:can_break_armor_stand}.
     */
    public static final TagKey<DamageType> CAN_BREAK_ARMOR_STAND = create("can_break_armor_stand");

    /**
     * Tag {@code #minecraft:damages_helmet}.
     */
    public static final TagKey<DamageType> DAMAGES_HELMET = create("damages_helmet");

    /**
     * Tag {@code #minecraft:ignites_armor_stands}.
     */
    public static final TagKey<DamageType> IGNITES_ARMOR_STANDS = create("ignites_armor_stands");

    /**
     * Tag {@code #minecraft:is_drowning}.
     */
    public static final TagKey<DamageType> IS_DROWNING = create("is_drowning");

    /**
     * Tag {@code #minecraft:is_explosion}.
     */
    public static final TagKey<DamageType> IS_EXPLOSION = create("is_explosion");

    /**
     * Tag {@code #minecraft:is_fall}.
     */
    public static final TagKey<DamageType> IS_FALL = create("is_fall");

    /**
     * Tag {@code #minecraft:is_fire}.
     */
    public static final TagKey<DamageType> IS_FIRE = create("is_fire");

    /**
     * Tag {@code #minecraft:is_freezing}.
     */
    public static final TagKey<DamageType> IS_FREEZING = create("is_freezing");

    /**
     * Tag {@code #minecraft:is_lightning}.
     */
    public static final TagKey<DamageType> IS_LIGHTNING = create("is_lightning");

    /**
     * Tag {@code #minecraft:is_player_attack}.
     */
    public static final TagKey<DamageType> IS_PLAYER_ATTACK = create("is_player_attack");

    /**
     * Tag {@code #minecraft:is_projectile}.
     */
    public static final TagKey<DamageType> IS_PROJECTILE = create("is_projectile");

    /**
     * Tag {@code #minecraft:mace_smash}.
     */
    public static final TagKey<DamageType> MACE_SMASH = create("mace_smash");

    /**
     * Tag {@code #minecraft:no_anger}.
     */
    public static final TagKey<DamageType> NO_ANGER = create("no_anger");

    /**
     * Tag {@code #minecraft:no_hurt_sound}.
     */
    public static final TagKey<DamageType> NO_HURT_SOUND = create("no_hurt_sound");

    /**
     * Tag {@code #minecraft:no_impact}.
     */
    public static final TagKey<DamageType> NO_IMPACT = create("no_impact");

    /**
     * Tag {@code #minecraft:no_knockback}.
     */
    public static final TagKey<DamageType> NO_KNOCKBACK = create("no_knockback");

    /**
     * Tag {@code #minecraft:no_wolf_retaliation}.
     */
    public static final TagKey<DamageType> NO_WOLF_RETALIATION = create("no_wolf_retaliation");

    /**
     * Tag {@code #minecraft:panic_causes}.
     */
    public static final TagKey<DamageType> PANIC_CAUSES = create("panic_causes");

    /**
     * Tag {@code #minecraft:panic_environmental_causes}.
     */
    public static final TagKey<DamageType> PANIC_ENVIRONMENTAL_CAUSES = create("panic_environmental_causes");

    /**
     * Tag {@code #minecraft:sulfur_cube_with_block_immune_to}.
     */
    public static final TagKey<DamageType> SULFUR_CUBE_WITH_BLOCK_IMMUNE_TO = create("sulfur_cube_with_block_immune_to");

    /**
     * Tag {@code #minecraft:witch_resistant_to}.
     */
    public static final TagKey<DamageType> WITCH_RESISTANT_TO = create("witch_resistant_to");

    /**
     * Tag {@code #minecraft:wither_immune_to}.
     */
    public static final TagKey<DamageType> WITHER_IMMUNE_TO = create("wither_immune_to");

    private DamageTypeTagKeys() {
        throw new UnsupportedOperationException("DamageTypeTagKeys cannot be instantiated.");
    }

    private static TagKey<DamageType> create(@KeyPattern final String value) {
        return TagKey.create(RegistryKey.DAMAGE_TYPE, value);
    }
}
