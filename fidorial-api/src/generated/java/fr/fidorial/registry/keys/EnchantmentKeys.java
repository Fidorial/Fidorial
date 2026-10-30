package fr.fidorial.registry.keys;

import fr.fidorial.item.enchantment.EnchantmentDefinition;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import java.util.List;
import java.util.stream.Stream;
import net.kyori.adventure.key.KeyPattern;

/**
 * Typed keys for entries in the {@code minecraft:enchantment} registry.
 */
public final class EnchantmentKeys {
    /**
     * Key for {@code minecraft:aqua_affinity}.
     */
    public static final TypedKey<EnchantmentDefinition> AQUA_AFFINITY = create("aqua_affinity");

    /**
     * Key for {@code minecraft:bane_of_arthropods}.
     */
    public static final TypedKey<EnchantmentDefinition> BANE_OF_ARTHROPODS = create("bane_of_arthropods");

    /**
     * Key for {@code minecraft:binding_curse}.
     */
    public static final TypedKey<EnchantmentDefinition> BINDING_CURSE = create("binding_curse");

    /**
     * Key for {@code minecraft:blast_protection}.
     */
    public static final TypedKey<EnchantmentDefinition> BLAST_PROTECTION = create("blast_protection");

    /**
     * Key for {@code minecraft:breach}.
     */
    public static final TypedKey<EnchantmentDefinition> BREACH = create("breach");

    /**
     * Key for {@code minecraft:channeling}.
     */
    public static final TypedKey<EnchantmentDefinition> CHANNELING = create("channeling");

    /**
     * Key for {@code minecraft:density}.
     */
    public static final TypedKey<EnchantmentDefinition> DENSITY = create("density");

    /**
     * Key for {@code minecraft:depth_strider}.
     */
    public static final TypedKey<EnchantmentDefinition> DEPTH_STRIDER = create("depth_strider");

    /**
     * Key for {@code minecraft:efficiency}.
     */
    public static final TypedKey<EnchantmentDefinition> EFFICIENCY = create("efficiency");

    /**
     * Key for {@code minecraft:feather_falling}.
     */
    public static final TypedKey<EnchantmentDefinition> FEATHER_FALLING = create("feather_falling");

    /**
     * Key for {@code minecraft:fire_aspect}.
     */
    public static final TypedKey<EnchantmentDefinition> FIRE_ASPECT = create("fire_aspect");

    /**
     * Key for {@code minecraft:fire_protection}.
     */
    public static final TypedKey<EnchantmentDefinition> FIRE_PROTECTION = create("fire_protection");

    /**
     * Key for {@code minecraft:flame}.
     */
    public static final TypedKey<EnchantmentDefinition> FLAME = create("flame");

    /**
     * Key for {@code minecraft:fortune}.
     */
    public static final TypedKey<EnchantmentDefinition> FORTUNE = create("fortune");

    /**
     * Key for {@code minecraft:frost_walker}.
     */
    public static final TypedKey<EnchantmentDefinition> FROST_WALKER = create("frost_walker");

    /**
     * Key for {@code minecraft:impaling}.
     */
    public static final TypedKey<EnchantmentDefinition> IMPALING = create("impaling");

    /**
     * Key for {@code minecraft:infinity}.
     */
    public static final TypedKey<EnchantmentDefinition> INFINITY = create("infinity");

    /**
     * Key for {@code minecraft:knockback}.
     */
    public static final TypedKey<EnchantmentDefinition> KNOCKBACK = create("knockback");

    /**
     * Key for {@code minecraft:looting}.
     */
    public static final TypedKey<EnchantmentDefinition> LOOTING = create("looting");

    /**
     * Key for {@code minecraft:loyalty}.
     */
    public static final TypedKey<EnchantmentDefinition> LOYALTY = create("loyalty");

    /**
     * Key for {@code minecraft:luck_of_the_sea}.
     */
    public static final TypedKey<EnchantmentDefinition> LUCK_OF_THE_SEA = create("luck_of_the_sea");

    /**
     * Key for {@code minecraft:lunge}.
     */
    public static final TypedKey<EnchantmentDefinition> LUNGE = create("lunge");

    /**
     * Key for {@code minecraft:lure}.
     */
    public static final TypedKey<EnchantmentDefinition> LURE = create("lure");

    /**
     * Key for {@code minecraft:mending}.
     */
    public static final TypedKey<EnchantmentDefinition> MENDING = create("mending");

    /**
     * Key for {@code minecraft:multishot}.
     */
    public static final TypedKey<EnchantmentDefinition> MULTISHOT = create("multishot");

    /**
     * Key for {@code minecraft:piercing}.
     */
    public static final TypedKey<EnchantmentDefinition> PIERCING = create("piercing");

    /**
     * Key for {@code minecraft:power}.
     */
    public static final TypedKey<EnchantmentDefinition> POWER = create("power");

    /**
     * Key for {@code minecraft:projectile_protection}.
     */
    public static final TypedKey<EnchantmentDefinition> PROJECTILE_PROTECTION = create("projectile_protection");

    /**
     * Key for {@code minecraft:protection}.
     */
    public static final TypedKey<EnchantmentDefinition> PROTECTION = create("protection");

    /**
     * Key for {@code minecraft:punch}.
     */
    public static final TypedKey<EnchantmentDefinition> PUNCH = create("punch");

    /**
     * Key for {@code minecraft:quick_charge}.
     */
    public static final TypedKey<EnchantmentDefinition> QUICK_CHARGE = create("quick_charge");

    /**
     * Key for {@code minecraft:respiration}.
     */
    public static final TypedKey<EnchantmentDefinition> RESPIRATION = create("respiration");

    /**
     * Key for {@code minecraft:riptide}.
     */
    public static final TypedKey<EnchantmentDefinition> RIPTIDE = create("riptide");

    /**
     * Key for {@code minecraft:sharpness}.
     */
    public static final TypedKey<EnchantmentDefinition> SHARPNESS = create("sharpness");

    /**
     * Key for {@code minecraft:silk_touch}.
     */
    public static final TypedKey<EnchantmentDefinition> SILK_TOUCH = create("silk_touch");

    /**
     * Key for {@code minecraft:smite}.
     */
    public static final TypedKey<EnchantmentDefinition> SMITE = create("smite");

    /**
     * Key for {@code minecraft:soul_speed}.
     */
    public static final TypedKey<EnchantmentDefinition> SOUL_SPEED = create("soul_speed");

    /**
     * Key for {@code minecraft:sweeping_edge}.
     */
    public static final TypedKey<EnchantmentDefinition> SWEEPING_EDGE = create("sweeping_edge");

    /**
     * Key for {@code minecraft:swift_sneak}.
     */
    public static final TypedKey<EnchantmentDefinition> SWIFT_SNEAK = create("swift_sneak");

    /**
     * Key for {@code minecraft:thorns}.
     */
    public static final TypedKey<EnchantmentDefinition> THORNS = create("thorns");

    /**
     * Key for {@code minecraft:unbreaking}.
     */
    public static final TypedKey<EnchantmentDefinition> UNBREAKING = create("unbreaking");

    /**
     * Key for {@code minecraft:vanishing_curse}.
     */
    public static final TypedKey<EnchantmentDefinition> VANISHING_CURSE = create("vanishing_curse");

    /**
     * Key for {@code minecraft:wind_burst}.
     */
    public static final TypedKey<EnchantmentDefinition> WIND_BURST = create("wind_burst");

    private static final List<TypedKey<EnchantmentDefinition>> VALUES = List.of(
        AQUA_AFFINITY,
        BANE_OF_ARTHROPODS,
        BINDING_CURSE,
        BLAST_PROTECTION,
        BREACH,
        CHANNELING,
        DENSITY,
        DEPTH_STRIDER,
        EFFICIENCY,
        FEATHER_FALLING,
        FIRE_ASPECT,
        FIRE_PROTECTION,
        FLAME,
        FORTUNE,
        FROST_WALKER,
        IMPALING,
        INFINITY,
        KNOCKBACK,
        LOOTING,
        LOYALTY,
        LUCK_OF_THE_SEA,
        LUNGE,
        LURE,
        MENDING,
        MULTISHOT,
        PIERCING,
        POWER,
        PROJECTILE_PROTECTION,
        PROTECTION,
        PUNCH,
        QUICK_CHARGE,
        RESPIRATION,
        RIPTIDE,
        SHARPNESS,
        SILK_TOUCH,
        SMITE,
        SOUL_SPEED,
        SWEEPING_EDGE,
        SWIFT_SNEAK,
        THORNS,
        UNBREAKING,
        VANISHING_CURSE,
        WIND_BURST
    );

    private EnchantmentKeys() {
        throw new UnsupportedOperationException("EnchantmentKeys cannot be instantiated.");
    }

    private static TypedKey<EnchantmentDefinition> create(@KeyPattern final String value) {
        return TypedKey.create(RegistryKey.ENCHANTMENT, value);
    }

    /**
     * Returns a stream containing all keys declared by this class.
     *
     * @return a stream of registry keys
     */
    public static Stream<TypedKey<EnchantmentDefinition>> values() {
        return VALUES.stream();
    }
}
