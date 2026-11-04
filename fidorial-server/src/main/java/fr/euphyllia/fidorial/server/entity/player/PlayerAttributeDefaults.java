package fr.euphyllia.fidorial.server.entity.player;

import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.Attribute;
import fr.fidorial.registry.keys.AttributeKeys;

import java.util.Map;

/**
 * <a href="https://minecraft.wiki/w/Attribute">Attribute Wiki</a>
 */
final class PlayerAttributeDefaults {

    private static final Map<TypedKey<Attribute>, Double> BASES = Map.ofEntries(
            Map.entry(AttributeKeys.AIR_DRAG_MODIFIER, 1.0),
            Map.entry(AttributeKeys.ARMOR, 0.0),
            Map.entry(AttributeKeys.ARMOR_TOUGHNESS, 0.0),
            Map.entry(AttributeKeys.ATTACK_DAMAGE, 1.0),
            Map.entry(AttributeKeys.ATTACK_KNOCKBACK, 0.0),
            Map.entry(AttributeKeys.ATTACK_SPEED, 4.0),
            Map.entry(AttributeKeys.BELOW_NAME_DISTANCE, 10.0),
            Map.entry(AttributeKeys.BLOCK_BREAK_SPEED, 1.0),
            Map.entry(AttributeKeys.BLOCK_INTERACTION_RANGE, 4.5),
            Map.entry(AttributeKeys.BOUNCINESS, 0.0),
            Map.entry(AttributeKeys.BURNING_TIME, 1.0),
            Map.entry(AttributeKeys.CAMERA_DISTANCE, 4.0),
            Map.entry(AttributeKeys.ENTITY_INTERACTION_RANGE, 3.0),
            Map.entry(AttributeKeys.EXPLOSION_KNOCKBACK_RESISTANCE, 0.0),
            Map.entry(AttributeKeys.FALL_DAMAGE_MULTIPLIER, 1.0),
            Map.entry(AttributeKeys.FLYING_SPEED, 0.4),
            Map.entry(AttributeKeys.FOLLOW_RANGE, 32.0),
            Map.entry(AttributeKeys.FRICTION_MODIFIER, 1.0),
            Map.entry(AttributeKeys.GRAVITY, 0.08),
            Map.entry(AttributeKeys.JUMP_STRENGTH, 0.42),
            Map.entry(AttributeKeys.KNOCKBACK_RESISTANCE, 0.0),
            Map.entry(AttributeKeys.LUCK, 0.0),
            Map.entry(AttributeKeys.MAX_ABSORPTION, 0.0),
            Map.entry(AttributeKeys.MAX_HEALTH, 20.0),
            Map.entry(AttributeKeys.MINING_EFFICIENCY, 0.0),
            Map.entry(AttributeKeys.MOVEMENT_EFFICIENCY, 0.0),
            Map.entry(AttributeKeys.MOVEMENT_SPEED, 1.0),
            Map.entry(AttributeKeys.NAME_TAG_DISTANCE, 64.0),
            Map.entry(AttributeKeys.OXYGEN_BONUS, 0.0),
            Map.entry(AttributeKeys.SAFE_FALL_DISTANCE, 3.0),
            Map.entry(AttributeKeys.SCALE, 1.0),
            Map.entry(AttributeKeys.SNEAKING_SPEED, 0.3),
            Map.entry(AttributeKeys.SPAWN_REINFORCEMENTS, 0.0),
            Map.entry(AttributeKeys.STEP_HEIGHT, 0.6),
            Map.entry(AttributeKeys.SUBMERGED_MINING_SPEED, 0.2),
            Map.entry(AttributeKeys.SWEEPING_DAMAGE_RATIO, 0.0),
            Map.entry(AttributeKeys.TEMPT_RANGE, 10.0),
            Map.entry(AttributeKeys.WATER_MOVEMENT_EFFICIENCY, 0.0),
            Map.entry(AttributeKeys.WAYPOINT_TRANSMIT_RANGE, 60_000_000.0),
            Map.entry(AttributeKeys.WAYPOINT_RECEIVE_RANGE, 60_000_000.0));

    private PlayerAttributeDefaults() {
    }

    /**
     * {@return the base value of an attribute on a vanilla player, {@code 0} when it is not known}
     *
     * @param attribute the attribute
     */
    static double base(final TypedKey<Attribute> attribute) {
        return BASES.getOrDefault(attribute, 0.0);
    }
}
