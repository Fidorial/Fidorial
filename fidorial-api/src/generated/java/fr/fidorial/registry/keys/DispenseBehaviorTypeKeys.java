package fr.fidorial.registry.keys;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.DispenseBehaviorType;
import java.util.List;
import java.util.stream.Stream;
import net.kyori.adventure.key.KeyPattern;

/**
 * Typed keys for entries in the {@code minecraft:dispense_behavior_type} registry.
 */
public final class DispenseBehaviorTypeKeys {
    /**
     * Key for {@code minecraft:brush_armadillo}.
     */
    public static final TypedKey<DispenseBehaviorType> BRUSH_ARMADILLO = create("brush_armadillo");

    /**
     * Key for {@code minecraft:charge_respawn_anchor}.
     */
    public static final TypedKey<DispenseBehaviorType> CHARGE_RESPAWN_ANCHOR = create("charge_respawn_anchor");

    /**
     * Key for {@code minecraft:convert_to_mud}.
     */
    public static final TypedKey<DispenseBehaviorType> CONVERT_TO_MUD = create("convert_to_mud");

    /**
     * Key for {@code minecraft:drop_item}.
     */
    public static final TypedKey<DispenseBehaviorType> DROP_ITEM = create("drop_item");

    /**
     * Key for {@code minecraft:empty_bucket}.
     */
    public static final TypedKey<DispenseBehaviorType> EMPTY_BUCKET = create("empty_bucket");

    /**
     * Key for {@code minecraft:equip}.
     */
    public static final TypedKey<DispenseBehaviorType> EQUIP = create("equip");

    /**
     * Key for {@code minecraft:equip_chest}.
     */
    public static final TypedKey<DispenseBehaviorType> EQUIP_CHEST = create("equip_chest");

    /**
     * Key for {@code minecraft:equip_sulfur_cube}.
     */
    public static final TypedKey<DispenseBehaviorType> EQUIP_SULFUR_CUBE = create("equip_sulfur_cube");

    /**
     * Key for {@code minecraft:fill_bottle}.
     */
    public static final TypedKey<DispenseBehaviorType> FILL_BOTTLE = create("fill_bottle");

    /**
     * Key for {@code minecraft:fill_bucket}.
     */
    public static final TypedKey<DispenseBehaviorType> FILL_BUCKET = create("fill_bucket");

    /**
     * Key for {@code minecraft:grow_plant}.
     */
    public static final TypedKey<DispenseBehaviorType> GROW_PLANT = create("grow_plant");

    /**
     * Key for {@code minecraft:ignite}.
     */
    public static final TypedKey<DispenseBehaviorType> IGNITE = create("ignite");

    /**
     * Key for {@code minecraft:insert_into_container}.
     */
    public static final TypedKey<DispenseBehaviorType> INSERT_INTO_CONTAINER = create("insert_into_container");

    /**
     * Key for {@code minecraft:place_armor_stand}.
     */
    public static final TypedKey<DispenseBehaviorType> PLACE_ARMOR_STAND = create("place_armor_stand");

    /**
     * Key for {@code minecraft:place_block}.
     */
    public static final TypedKey<DispenseBehaviorType> PLACE_BLOCK = create("place_block");

    /**
     * Key for {@code minecraft:place_boat}.
     */
    public static final TypedKey<DispenseBehaviorType> PLACE_BOAT = create("place_boat");

    /**
     * Key for {@code minecraft:place_minecart}.
     */
    public static final TypedKey<DispenseBehaviorType> PLACE_MINECART = create("place_minecart");

    /**
     * Key for {@code minecraft:shear}.
     */
    public static final TypedKey<DispenseBehaviorType> SHEAR = create("shear");

    /**
     * Key for {@code minecraft:shoot_projectile}.
     */
    public static final TypedKey<DispenseBehaviorType> SHOOT_PROJECTILE = create("shoot_projectile");

    /**
     * Key for {@code minecraft:spawn_golem_from_carved_pumpkin}.
     */
    public static final TypedKey<DispenseBehaviorType> SPAWN_GOLEM_FROM_CARVED_PUMPKIN = create("spawn_golem_from_carved_pumpkin");

    /**
     * Key for {@code minecraft:spawn_mob}.
     */
    public static final TypedKey<DispenseBehaviorType> SPAWN_MOB = create("spawn_mob");

    /**
     * Key for {@code minecraft:spawn_tnt}.
     */
    public static final TypedKey<DispenseBehaviorType> SPAWN_TNT = create("spawn_tnt");

    /**
     * Key for {@code minecraft:spawn_wither_from_wither_skeleton_skull}.
     */
    public static final TypedKey<DispenseBehaviorType> SPAWN_WITHER_FROM_WITHER_SKELETON_SKULL = create("spawn_wither_from_wither_skeleton_skull");

    /**
     * Key for {@code minecraft:transform_block}.
     */
    public static final TypedKey<DispenseBehaviorType> TRANSFORM_BLOCK = create("transform_block");

    private static final List<TypedKey<DispenseBehaviorType>> VALUES = List.of(
        BRUSH_ARMADILLO,
        CHARGE_RESPAWN_ANCHOR,
        CONVERT_TO_MUD,
        DROP_ITEM,
        EMPTY_BUCKET,
        EQUIP,
        EQUIP_CHEST,
        EQUIP_SULFUR_CUBE,
        FILL_BOTTLE,
        FILL_BUCKET,
        GROW_PLANT,
        IGNITE,
        INSERT_INTO_CONTAINER,
        PLACE_ARMOR_STAND,
        PLACE_BLOCK,
        PLACE_BOAT,
        PLACE_MINECART,
        SHEAR,
        SHOOT_PROJECTILE,
        SPAWN_GOLEM_FROM_CARVED_PUMPKIN,
        SPAWN_MOB,
        SPAWN_TNT,
        SPAWN_WITHER_FROM_WITHER_SKELETON_SKULL,
        TRANSFORM_BLOCK
    );

    private DispenseBehaviorTypeKeys() {
        throw new UnsupportedOperationException("DispenseBehaviorTypeKeys cannot be instantiated.");
    }

    private static TypedKey<DispenseBehaviorType> create(@KeyPattern final String value) {
        return TypedKey.withRegistry(RegistryKey.DISPENSE_BEHAVIOR_TYPE, value);
    }

    /**
     * Returns a stream containing all keys declared by this class.
     *
     * @return a stream of registry keys
     */
    public static Stream<TypedKey<DispenseBehaviorType>> values() {
        return VALUES.stream();
    }
}
