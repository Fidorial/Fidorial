package fr.fidorial.registry.keys;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.Attribute;
import java.util.List;
import java.util.stream.Stream;
import net.kyori.adventure.key.KeyPattern;

/**
 * Typed keys for entries in the {@code minecraft:attribute} registry.
 */
public final class AttributeKeys {
    /**
     * Key for {@code minecraft:air_drag_modifier}.
     */
    public static final TypedKey<Attribute> AIR_DRAG_MODIFIER = create("air_drag_modifier");

    /**
     * Key for {@code minecraft:armor}.
     */
    public static final TypedKey<Attribute> ARMOR = create("armor");

    /**
     * Key for {@code minecraft:armor_toughness}.
     */
    public static final TypedKey<Attribute> ARMOR_TOUGHNESS = create("armor_toughness");

    /**
     * Key for {@code minecraft:attack_damage}.
     */
    public static final TypedKey<Attribute> ATTACK_DAMAGE = create("attack_damage");

    /**
     * Key for {@code minecraft:attack_knockback}.
     */
    public static final TypedKey<Attribute> ATTACK_KNOCKBACK = create("attack_knockback");

    /**
     * Key for {@code minecraft:attack_speed}.
     */
    public static final TypedKey<Attribute> ATTACK_SPEED = create("attack_speed");

    /**
     * Key for {@code minecraft:below_name_distance}.
     */
    public static final TypedKey<Attribute> BELOW_NAME_DISTANCE = create("below_name_distance");

    /**
     * Key for {@code minecraft:block_break_speed}.
     */
    public static final TypedKey<Attribute> BLOCK_BREAK_SPEED = create("block_break_speed");

    /**
     * Key for {@code minecraft:block_interaction_range}.
     */
    public static final TypedKey<Attribute> BLOCK_INTERACTION_RANGE = create("block_interaction_range");

    /**
     * Key for {@code minecraft:bounciness}.
     */
    public static final TypedKey<Attribute> BOUNCINESS = create("bounciness");

    /**
     * Key for {@code minecraft:burning_time}.
     */
    public static final TypedKey<Attribute> BURNING_TIME = create("burning_time");

    /**
     * Key for {@code minecraft:camera_distance}.
     */
    public static final TypedKey<Attribute> CAMERA_DISTANCE = create("camera_distance");

    /**
     * Key for {@code minecraft:entity_interaction_range}.
     */
    public static final TypedKey<Attribute> ENTITY_INTERACTION_RANGE = create("entity_interaction_range");

    /**
     * Key for {@code minecraft:explosion_knockback_resistance}.
     */
    public static final TypedKey<Attribute> EXPLOSION_KNOCKBACK_RESISTANCE = create("explosion_knockback_resistance");

    /**
     * Key for {@code minecraft:fall_damage_multiplier}.
     */
    public static final TypedKey<Attribute> FALL_DAMAGE_MULTIPLIER = create("fall_damage_multiplier");

    /**
     * Key for {@code minecraft:flying_speed}.
     */
    public static final TypedKey<Attribute> FLYING_SPEED = create("flying_speed");

    /**
     * Key for {@code minecraft:follow_range}.
     */
    public static final TypedKey<Attribute> FOLLOW_RANGE = create("follow_range");

    /**
     * Key for {@code minecraft:friction_modifier}.
     */
    public static final TypedKey<Attribute> FRICTION_MODIFIER = create("friction_modifier");

    /**
     * Key for {@code minecraft:gravity}.
     */
    public static final TypedKey<Attribute> GRAVITY = create("gravity");

    /**
     * Key for {@code minecraft:jump_strength}.
     */
    public static final TypedKey<Attribute> JUMP_STRENGTH = create("jump_strength");

    /**
     * Key for {@code minecraft:knockback_resistance}.
     */
    public static final TypedKey<Attribute> KNOCKBACK_RESISTANCE = create("knockback_resistance");

    /**
     * Key for {@code minecraft:luck}.
     */
    public static final TypedKey<Attribute> LUCK = create("luck");

    /**
     * Key for {@code minecraft:max_absorption}.
     */
    public static final TypedKey<Attribute> MAX_ABSORPTION = create("max_absorption");

    /**
     * Key for {@code minecraft:max_health}.
     */
    public static final TypedKey<Attribute> MAX_HEALTH = create("max_health");

    /**
     * Key for {@code minecraft:mining_efficiency}.
     */
    public static final TypedKey<Attribute> MINING_EFFICIENCY = create("mining_efficiency");

    /**
     * Key for {@code minecraft:movement_efficiency}.
     */
    public static final TypedKey<Attribute> MOVEMENT_EFFICIENCY = create("movement_efficiency");

    /**
     * Key for {@code minecraft:movement_speed}.
     */
    public static final TypedKey<Attribute> MOVEMENT_SPEED = create("movement_speed");

    /**
     * Key for {@code minecraft:name_tag_distance}.
     */
    public static final TypedKey<Attribute> NAME_TAG_DISTANCE = create("name_tag_distance");

    /**
     * Key for {@code minecraft:oxygen_bonus}.
     */
    public static final TypedKey<Attribute> OXYGEN_BONUS = create("oxygen_bonus");

    /**
     * Key for {@code minecraft:safe_fall_distance}.
     */
    public static final TypedKey<Attribute> SAFE_FALL_DISTANCE = create("safe_fall_distance");

    /**
     * Key for {@code minecraft:scale}.
     */
    public static final TypedKey<Attribute> SCALE = create("scale");

    /**
     * Key for {@code minecraft:sneaking_speed}.
     */
    public static final TypedKey<Attribute> SNEAKING_SPEED = create("sneaking_speed");

    /**
     * Key for {@code minecraft:spawn_reinforcements}.
     */
    public static final TypedKey<Attribute> SPAWN_REINFORCEMENTS = create("spawn_reinforcements");

    /**
     * Key for {@code minecraft:step_height}.
     */
    public static final TypedKey<Attribute> STEP_HEIGHT = create("step_height");

    /**
     * Key for {@code minecraft:submerged_mining_speed}.
     */
    public static final TypedKey<Attribute> SUBMERGED_MINING_SPEED = create("submerged_mining_speed");

    /**
     * Key for {@code minecraft:sweeping_damage_ratio}.
     */
    public static final TypedKey<Attribute> SWEEPING_DAMAGE_RATIO = create("sweeping_damage_ratio");

    /**
     * Key for {@code minecraft:tempt_range}.
     */
    public static final TypedKey<Attribute> TEMPT_RANGE = create("tempt_range");

    /**
     * Key for {@code minecraft:water_movement_efficiency}.
     */
    public static final TypedKey<Attribute> WATER_MOVEMENT_EFFICIENCY = create("water_movement_efficiency");

    /**
     * Key for {@code minecraft:waypoint_receive_range}.
     */
    public static final TypedKey<Attribute> WAYPOINT_RECEIVE_RANGE = create("waypoint_receive_range");

    /**
     * Key for {@code minecraft:waypoint_transmit_range}.
     */
    public static final TypedKey<Attribute> WAYPOINT_TRANSMIT_RANGE = create("waypoint_transmit_range");

    private static final List<TypedKey<Attribute>> VALUES = List.of(
        AIR_DRAG_MODIFIER,
        ARMOR,
        ARMOR_TOUGHNESS,
        ATTACK_DAMAGE,
        ATTACK_KNOCKBACK,
        ATTACK_SPEED,
        BELOW_NAME_DISTANCE,
        BLOCK_BREAK_SPEED,
        BLOCK_INTERACTION_RANGE,
        BOUNCINESS,
        BURNING_TIME,
        CAMERA_DISTANCE,
        ENTITY_INTERACTION_RANGE,
        EXPLOSION_KNOCKBACK_RESISTANCE,
        FALL_DAMAGE_MULTIPLIER,
        FLYING_SPEED,
        FOLLOW_RANGE,
        FRICTION_MODIFIER,
        GRAVITY,
        JUMP_STRENGTH,
        KNOCKBACK_RESISTANCE,
        LUCK,
        MAX_ABSORPTION,
        MAX_HEALTH,
        MINING_EFFICIENCY,
        MOVEMENT_EFFICIENCY,
        MOVEMENT_SPEED,
        NAME_TAG_DISTANCE,
        OXYGEN_BONUS,
        SAFE_FALL_DISTANCE,
        SCALE,
        SNEAKING_SPEED,
        SPAWN_REINFORCEMENTS,
        STEP_HEIGHT,
        SUBMERGED_MINING_SPEED,
        SWEEPING_DAMAGE_RATIO,
        TEMPT_RANGE,
        WATER_MOVEMENT_EFFICIENCY,
        WAYPOINT_RECEIVE_RANGE,
        WAYPOINT_TRANSMIT_RANGE
    );

    private AttributeKeys() {
        throw new UnsupportedOperationException("AttributeKeys cannot be instantiated.");
    }

    private static TypedKey<Attribute> create(@KeyPattern final String value) {
        return TypedKey.withRegistry(RegistryKey.ATTRIBUTE, value);
    }

    /**
     * Returns a stream containing all keys declared by this class.
     *
     * @return a stream of registry keys
     */
    public static Stream<TypedKey<Attribute>> values() {
        return VALUES.stream();
    }
}
