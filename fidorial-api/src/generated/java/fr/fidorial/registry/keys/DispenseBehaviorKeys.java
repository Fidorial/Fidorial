package fr.fidorial.registry.keys;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.DispenseBehavior;
import java.util.List;
import java.util.stream.Stream;
import net.kyori.adventure.key.KeyPattern;

/**
 * Typed keys for entries in the {@code minecraft:dispense_behavior} registry.
 */
public final class DispenseBehaviorKeys {
    /**
     * Key for {@code minecraft:armor_stand}.
     */
    public static final TypedKey<DispenseBehavior> ARMOR_STAND = create("armor_stand");

    /**
     * Key for {@code minecraft:boat}.
     */
    public static final TypedKey<DispenseBehavior> BOAT = create("boat");

    /**
     * Key for {@code minecraft:bone_meal}.
     */
    public static final TypedKey<DispenseBehavior> BONE_MEAL = create("bone_meal");

    /**
     * Key for {@code minecraft:brush}.
     */
    public static final TypedKey<DispenseBehavior> BRUSH = create("brush");

    /**
     * Key for {@code minecraft:carved_pumpkin}.
     */
    public static final TypedKey<DispenseBehavior> CARVED_PUMPKIN = create("carved_pumpkin");

    /**
     * Key for {@code minecraft:chest}.
     */
    public static final TypedKey<DispenseBehavior> CHEST = create("chest");

    /**
     * Key for {@code minecraft:default}.
     */
    public static final TypedKey<DispenseBehavior> DEFAULT = create("default");

    /**
     * Key for {@code minecraft:default_projectile}.
     */
    public static final TypedKey<DispenseBehavior> DEFAULT_PROJECTILE = create("default_projectile");

    /**
     * Key for {@code minecraft:dropper}.
     */
    public static final TypedKey<DispenseBehavior> DROPPER = create("dropper");

    /**
     * Key for {@code minecraft:empty_bucket}.
     */
    public static final TypedKey<DispenseBehavior> EMPTY_BUCKET = create("empty_bucket");

    /**
     * Key for {@code minecraft:equipment}.
     */
    public static final TypedKey<DispenseBehavior> EQUIPMENT = create("equipment");

    /**
     * Key for {@code minecraft:filled_bucket}.
     */
    public static final TypedKey<DispenseBehavior> FILLED_BUCKET = create("filled_bucket");

    /**
     * Key for {@code minecraft:fire_charge}.
     */
    public static final TypedKey<DispenseBehavior> FIRE_CHARGE = create("fire_charge");

    /**
     * Key for {@code minecraft:firework_rocket}.
     */
    public static final TypedKey<DispenseBehavior> FIREWORK_ROCKET = create("firework_rocket");

    /**
     * Key for {@code minecraft:flint_and_steel}.
     */
    public static final TypedKey<DispenseBehavior> FLINT_AND_STEEL = create("flint_and_steel");

    /**
     * Key for {@code minecraft:glass_bottle}.
     */
    public static final TypedKey<DispenseBehavior> GLASS_BOTTLE = create("glass_bottle");

    /**
     * Key for {@code minecraft:glowstone}.
     */
    public static final TypedKey<DispenseBehavior> GLOWSTONE = create("glowstone");

    /**
     * Key for {@code minecraft:honeycomb}.
     */
    public static final TypedKey<DispenseBehavior> HONEYCOMB = create("honeycomb");

    /**
     * Key for {@code minecraft:minecart}.
     */
    public static final TypedKey<DispenseBehavior> MINECART = create("minecart");

    /**
     * Key for {@code minecraft:potion}.
     */
    public static final TypedKey<DispenseBehavior> POTION = create("potion");

    /**
     * Key for {@code minecraft:shears}.
     */
    public static final TypedKey<DispenseBehavior> SHEARS = create("shears");

    /**
     * Key for {@code minecraft:shulker_box}.
     */
    public static final TypedKey<DispenseBehavior> SHULKER_BOX = create("shulker_box");

    /**
     * Key for {@code minecraft:spawn_egg}.
     */
    public static final TypedKey<DispenseBehavior> SPAWN_EGG = create("spawn_egg");

    /**
     * Key for {@code minecraft:thrown_potion}.
     */
    public static final TypedKey<DispenseBehavior> THROWN_POTION = create("thrown_potion");

    /**
     * Key for {@code minecraft:tnt}.
     */
    public static final TypedKey<DispenseBehavior> TNT = create("tnt");

    /**
     * Key for {@code minecraft:wind_charge}.
     */
    public static final TypedKey<DispenseBehavior> WIND_CHARGE = create("wind_charge");

    /**
     * Key for {@code minecraft:wither_skeleton_skull}.
     */
    public static final TypedKey<DispenseBehavior> WITHER_SKELETON_SKULL = create("wither_skeleton_skull");

    private static final List<TypedKey<DispenseBehavior>> VALUES = List.of(
        ARMOR_STAND,
        BOAT,
        BONE_MEAL,
        BRUSH,
        CARVED_PUMPKIN,
        CHEST,
        DEFAULT,
        DEFAULT_PROJECTILE,
        DROPPER,
        EMPTY_BUCKET,
        EQUIPMENT,
        FILLED_BUCKET,
        FIRE_CHARGE,
        FIREWORK_ROCKET,
        FLINT_AND_STEEL,
        GLASS_BOTTLE,
        GLOWSTONE,
        HONEYCOMB,
        MINECART,
        POTION,
        SHEARS,
        SHULKER_BOX,
        SPAWN_EGG,
        THROWN_POTION,
        TNT,
        WIND_CHARGE,
        WITHER_SKELETON_SKULL
    );

    private DispenseBehaviorKeys() {
        throw new UnsupportedOperationException("DispenseBehaviorKeys cannot be instantiated.");
    }

    private static TypedKey<DispenseBehavior> create(@KeyPattern final String value) {
        return TypedKey.withRegistry(RegistryKey.DISPENSE_BEHAVIOR, value);
    }

    /**
     * Returns a stream containing all keys declared by this class.
     *
     * @return a stream of registry keys
     */
    public static Stream<TypedKey<DispenseBehavior>> values() {
        return VALUES.stream();
    }
}
