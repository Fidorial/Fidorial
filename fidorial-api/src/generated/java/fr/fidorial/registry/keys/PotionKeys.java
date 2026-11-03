package fr.fidorial.registry.keys;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.Potion;
import java.util.List;
import java.util.stream.Stream;
import net.kyori.adventure.key.KeyPattern;

/**
 * Typed keys for entries in the {@code minecraft:potion} registry.
 */
public final class PotionKeys {
    /**
     * Key for {@code minecraft:awkward}.
     */
    public static final TypedKey<Potion> AWKWARD = create("awkward");

    /**
     * Key for {@code minecraft:fire_resistance}.
     */
    public static final TypedKey<Potion> FIRE_RESISTANCE = create("fire_resistance");

    /**
     * Key for {@code minecraft:freezing}.
     */
    public static final TypedKey<Potion> FREEZING = create("freezing");

    /**
     * Key for {@code minecraft:harming}.
     */
    public static final TypedKey<Potion> HARMING = create("harming");

    /**
     * Key for {@code minecraft:healing}.
     */
    public static final TypedKey<Potion> HEALING = create("healing");

    /**
     * Key for {@code minecraft:infested}.
     */
    public static final TypedKey<Potion> INFESTED = create("infested");

    /**
     * Key for {@code minecraft:invisibility}.
     */
    public static final TypedKey<Potion> INVISIBILITY = create("invisibility");

    /**
     * Key for {@code minecraft:leaping}.
     */
    public static final TypedKey<Potion> LEAPING = create("leaping");

    /**
     * Key for {@code minecraft:long_fire_resistance}.
     */
    public static final TypedKey<Potion> LONG_FIRE_RESISTANCE = create("long_fire_resistance");

    /**
     * Key for {@code minecraft:long_freezing}.
     */
    public static final TypedKey<Potion> LONG_FREEZING = create("long_freezing");

    /**
     * Key for {@code minecraft:long_invisibility}.
     */
    public static final TypedKey<Potion> LONG_INVISIBILITY = create("long_invisibility");

    /**
     * Key for {@code minecraft:long_leaping}.
     */
    public static final TypedKey<Potion> LONG_LEAPING = create("long_leaping");

    /**
     * Key for {@code minecraft:long_night_vision}.
     */
    public static final TypedKey<Potion> LONG_NIGHT_VISION = create("long_night_vision");

    /**
     * Key for {@code minecraft:long_poison}.
     */
    public static final TypedKey<Potion> LONG_POISON = create("long_poison");

    /**
     * Key for {@code minecraft:long_regeneration}.
     */
    public static final TypedKey<Potion> LONG_REGENERATION = create("long_regeneration");

    /**
     * Key for {@code minecraft:long_slow_falling}.
     */
    public static final TypedKey<Potion> LONG_SLOW_FALLING = create("long_slow_falling");

    /**
     * Key for {@code minecraft:long_slowness}.
     */
    public static final TypedKey<Potion> LONG_SLOWNESS = create("long_slowness");

    /**
     * Key for {@code minecraft:long_strength}.
     */
    public static final TypedKey<Potion> LONG_STRENGTH = create("long_strength");

    /**
     * Key for {@code minecraft:long_swiftness}.
     */
    public static final TypedKey<Potion> LONG_SWIFTNESS = create("long_swiftness");

    /**
     * Key for {@code minecraft:long_turtle_master}.
     */
    public static final TypedKey<Potion> LONG_TURTLE_MASTER = create("long_turtle_master");

    /**
     * Key for {@code minecraft:long_water_breathing}.
     */
    public static final TypedKey<Potion> LONG_WATER_BREATHING = create("long_water_breathing");

    /**
     * Key for {@code minecraft:long_weakness}.
     */
    public static final TypedKey<Potion> LONG_WEAKNESS = create("long_weakness");

    /**
     * Key for {@code minecraft:luck}.
     */
    public static final TypedKey<Potion> LUCK = create("luck");

    /**
     * Key for {@code minecraft:mundane}.
     */
    public static final TypedKey<Potion> MUNDANE = create("mundane");

    /**
     * Key for {@code minecraft:night_vision}.
     */
    public static final TypedKey<Potion> NIGHT_VISION = create("night_vision");

    /**
     * Key for {@code minecraft:oozing}.
     */
    public static final TypedKey<Potion> OOZING = create("oozing");

    /**
     * Key for {@code minecraft:poison}.
     */
    public static final TypedKey<Potion> POISON = create("poison");

    /**
     * Key for {@code minecraft:regeneration}.
     */
    public static final TypedKey<Potion> REGENERATION = create("regeneration");

    /**
     * Key for {@code minecraft:slow_falling}.
     */
    public static final TypedKey<Potion> SLOW_FALLING = create("slow_falling");

    /**
     * Key for {@code minecraft:slowness}.
     */
    public static final TypedKey<Potion> SLOWNESS = create("slowness");

    /**
     * Key for {@code minecraft:strength}.
     */
    public static final TypedKey<Potion> STRENGTH = create("strength");

    /**
     * Key for {@code minecraft:strong_harming}.
     */
    public static final TypedKey<Potion> STRONG_HARMING = create("strong_harming");

    /**
     * Key for {@code minecraft:strong_healing}.
     */
    public static final TypedKey<Potion> STRONG_HEALING = create("strong_healing");

    /**
     * Key for {@code minecraft:strong_leaping}.
     */
    public static final TypedKey<Potion> STRONG_LEAPING = create("strong_leaping");

    /**
     * Key for {@code minecraft:strong_poison}.
     */
    public static final TypedKey<Potion> STRONG_POISON = create("strong_poison");

    /**
     * Key for {@code minecraft:strong_regeneration}.
     */
    public static final TypedKey<Potion> STRONG_REGENERATION = create("strong_regeneration");

    /**
     * Key for {@code minecraft:strong_slowness}.
     */
    public static final TypedKey<Potion> STRONG_SLOWNESS = create("strong_slowness");

    /**
     * Key for {@code minecraft:strong_strength}.
     */
    public static final TypedKey<Potion> STRONG_STRENGTH = create("strong_strength");

    /**
     * Key for {@code minecraft:strong_swiftness}.
     */
    public static final TypedKey<Potion> STRONG_SWIFTNESS = create("strong_swiftness");

    /**
     * Key for {@code minecraft:strong_turtle_master}.
     */
    public static final TypedKey<Potion> STRONG_TURTLE_MASTER = create("strong_turtle_master");

    /**
     * Key for {@code minecraft:swiftness}.
     */
    public static final TypedKey<Potion> SWIFTNESS = create("swiftness");

    /**
     * Key for {@code minecraft:thick}.
     */
    public static final TypedKey<Potion> THICK = create("thick");

    /**
     * Key for {@code minecraft:turtle_master}.
     */
    public static final TypedKey<Potion> TURTLE_MASTER = create("turtle_master");

    /**
     * Key for {@code minecraft:water}.
     */
    public static final TypedKey<Potion> WATER = create("water");

    /**
     * Key for {@code minecraft:water_breathing}.
     */
    public static final TypedKey<Potion> WATER_BREATHING = create("water_breathing");

    /**
     * Key for {@code minecraft:weakness}.
     */
    public static final TypedKey<Potion> WEAKNESS = create("weakness");

    /**
     * Key for {@code minecraft:weaving}.
     */
    public static final TypedKey<Potion> WEAVING = create("weaving");

    /**
     * Key for {@code minecraft:wind_charged}.
     */
    public static final TypedKey<Potion> WIND_CHARGED = create("wind_charged");

    private static final List<TypedKey<Potion>> VALUES = List.of(
        AWKWARD,
        FIRE_RESISTANCE,
        FREEZING,
        HARMING,
        HEALING,
        INFESTED,
        INVISIBILITY,
        LEAPING,
        LONG_FIRE_RESISTANCE,
        LONG_FREEZING,
        LONG_INVISIBILITY,
        LONG_LEAPING,
        LONG_NIGHT_VISION,
        LONG_POISON,
        LONG_REGENERATION,
        LONG_SLOW_FALLING,
        LONG_SLOWNESS,
        LONG_STRENGTH,
        LONG_SWIFTNESS,
        LONG_TURTLE_MASTER,
        LONG_WATER_BREATHING,
        LONG_WEAKNESS,
        LUCK,
        MUNDANE,
        NIGHT_VISION,
        OOZING,
        POISON,
        REGENERATION,
        SLOW_FALLING,
        SLOWNESS,
        STRENGTH,
        STRONG_HARMING,
        STRONG_HEALING,
        STRONG_LEAPING,
        STRONG_POISON,
        STRONG_REGENERATION,
        STRONG_SLOWNESS,
        STRONG_STRENGTH,
        STRONG_SWIFTNESS,
        STRONG_TURTLE_MASTER,
        SWIFTNESS,
        THICK,
        TURTLE_MASTER,
        WATER,
        WATER_BREATHING,
        WEAKNESS,
        WEAVING,
        WIND_CHARGED
    );

    private PotionKeys() {
        throw new UnsupportedOperationException("PotionKeys cannot be instantiated.");
    }

    private static TypedKey<Potion> create(@KeyPattern final String value) {
        return TypedKey.withRegistry(RegistryKey.POTION, value);
    }

    /**
     * Returns a stream containing all keys declared by this class.
     *
     * @return a stream of registry keys
     */
    public static Stream<TypedKey<Potion>> values() {
        return VALUES.stream();
    }
}
