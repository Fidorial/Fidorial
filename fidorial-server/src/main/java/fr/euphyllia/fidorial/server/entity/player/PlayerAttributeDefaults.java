package fr.euphyllia.fidorial.server.entity.player;

import fr.fidorial.attribute.Attribute;
import net.kyori.adventure.key.Key;

import java.util.Map;

/**
 * <a href="https://minecraft.wiki/w/Attribute">Attribute Wiki</a>
 */
final class PlayerAttributeDefaults {

    private static final Map<Key, Double> BASES = Map.ofEntries(
            Map.entry(Attribute.AIR_DRAG_MODIFIER, 1.0),
            Map.entry(Attribute.ARMOR, 0.0),
            Map.entry(Attribute.ARMOR_TOUGHNESS, 0.0),
            Map.entry(Attribute.ATTACK_DAMAGE, 1.0),
            Map.entry(Attribute.ATTACK_KNOCKBACK, 0.0),
            Map.entry(Attribute.ATTACK_SPEED, 4.0),
            Map.entry(Attribute.BELOW_NAME_DISTANCE, 10.0),
            Map.entry(Attribute.BLOCK_BREAK_SPEED, 1.0),
            Map.entry(Attribute.BLOCK_INTERACTION_RANGE, 4.5),
            Map.entry(Attribute.BOUNCINESS, 0.0),
            Map.entry(Attribute.BURNING_TIME, 1.0),
            Map.entry(Attribute.CAMERA_DISTANCE, 4.0),
            Map.entry(Attribute.ENTITY_INTERACTION_RANGE, 3.0),
            Map.entry(Attribute.EXPLOSION_KNOCKBACK_RESISTANCE, 0.0),
            Map.entry(Attribute.FALL_DAMAGE_MULTIPLIER, 1.0),
            Map.entry(Attribute.FLYING_SPEED, 0.4),
            Map.entry(Attribute.FOLLOW_RANGE, 32.0),
            Map.entry(Attribute.FRICTION_MODIFIER, 1.0),
            Map.entry(Attribute.GRAVITY, 0.08),
            Map.entry(Attribute.JUMP_STRENGTH, 0.42),
            Map.entry(Attribute.KNOCKBACK_RESISTANCE, 0.0),
            Map.entry(Attribute.LUCK, 0.0),
            Map.entry(Attribute.MAX_ABSORPTION, 0.0),
            Map.entry(Attribute.MAX_HEALTH, 20.0),
            Map.entry(Attribute.MINING_EFFICIENCY, 0.0),
            Map.entry(Attribute.MOVEMENT_EFFICIENCY, 0.0),
            Map.entry(Attribute.MOVEMENT_SPEED, 1.0),
            Map.entry(Attribute.NAME_TAG_DISTANCE, 64.0),
            Map.entry(Attribute.OXYGEN_BONUS, 0.0),
            Map.entry(Attribute.SAFE_FALL_DISTANCE, 3.0),
            Map.entry(Attribute.SCALE, 1.0),
            Map.entry(Attribute.SNEAKING_SPEED, 0.3),
            Map.entry(Attribute.SPAWN_REINFORCEMENTS, 0.0),
            Map.entry(Attribute.STEP_HEIGHT, 0.6),
            Map.entry(Attribute.SUBMERGED_MINING_SPEED, 0.2),
            Map.entry(Attribute.SWEEPING_DAMAGE_RATIO, 0.0),
            Map.entry(Attribute.TEMPT_RANGE, 10.0),
            Map.entry(Attribute.WATER_MOVEMENT_EFFICIENCY, 0.0),
            Map.entry(Attribute.WAYPOINT_TRANSMIT_RANGE, 60_000_000.0),
            Map.entry(Attribute.WAYPOINT_RECEIVE_RANGE, 60_000_000.0));

    private PlayerAttributeDefaults() {
    }

    /**
     * {@return the base value of an attribute on a vanilla player, {@code 0} when it is not known}
     *
     * @param attribute the attribute
     */
    static double base(final Key attribute) {
        return BASES.getOrDefault(attribute, 0.0);
    }
}
