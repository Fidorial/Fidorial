package fr.euphyllia.fidorial.server.entity.player;

import fr.fidorial.registry.keys.AttributeKeys;
import net.kyori.adventure.key.Key;

import java.util.Map;

/**
 * <a href="https://minecraft.wiki/w/Attribute">Attribute Wiki</a>
 */
final class PlayerAttributeDefaults {

    private static final Map<Key, Double> BASES = Map.ofEntries(
            Map.entry(AttributeKeys.AIR_DRAG_MODIFIER.key(), 1.0),
            Map.entry(AttributeKeys.ARMOR.key(), 0.0),
            Map.entry(AttributeKeys.ARMOR_TOUGHNESS.key(), 0.0),
            Map.entry(AttributeKeys.ATTACK_DAMAGE.key(), 1.0),
            Map.entry(AttributeKeys.ATTACK_KNOCKBACK.key(), 0.0),
            Map.entry(AttributeKeys.ATTACK_SPEED.key(), 4.0),
            Map.entry(AttributeKeys.BELOW_NAME_DISTANCE.key(), 10.0),
            Map.entry(AttributeKeys.BLOCK_BREAK_SPEED.key(), 1.0),
            Map.entry(AttributeKeys.BLOCK_INTERACTION_RANGE.key(), 4.5),
            Map.entry(AttributeKeys.BOUNCINESS.key(), 0.0),
            Map.entry(AttributeKeys.BURNING_TIME.key(), 1.0),
            Map.entry(AttributeKeys.CAMERA_DISTANCE.key(), 4.0),
            Map.entry(AttributeKeys.ENTITY_INTERACTION_RANGE.key(), 3.0),
            Map.entry(AttributeKeys.EXPLOSION_KNOCKBACK_RESISTANCE.key(), 0.0),
            Map.entry(AttributeKeys.FALL_DAMAGE_MULTIPLIER.key(), 1.0),
            Map.entry(AttributeKeys.FLYING_SPEED.key(), 0.4),
            Map.entry(AttributeKeys.FOLLOW_RANGE.key(), 32.0),
            Map.entry(AttributeKeys.FRICTION_MODIFIER.key(), 1.0),
            Map.entry(AttributeKeys.GRAVITY.key(), 0.08),
            Map.entry(AttributeKeys.JUMP_STRENGTH.key(), 0.42),
            Map.entry(AttributeKeys.KNOCKBACK_RESISTANCE.key(), 0.0),
            Map.entry(AttributeKeys.LUCK.key(), 0.0),
            Map.entry(AttributeKeys.MAX_ABSORPTION.key(), 0.0),
            Map.entry(AttributeKeys.MAX_HEALTH.key(), 20.0),
            Map.entry(AttributeKeys.MINING_EFFICIENCY.key(), 0.0),
            Map.entry(AttributeKeys.MOVEMENT_EFFICIENCY.key(), 0.0),
            Map.entry(AttributeKeys.MOVEMENT_SPEED.key(), 1.0),
            Map.entry(AttributeKeys.NAME_TAG_DISTANCE.key(), 64.0),
            Map.entry(AttributeKeys.OXYGEN_BONUS.key(), 0.0),
            Map.entry(AttributeKeys.SAFE_FALL_DISTANCE.key(), 3.0),
            Map.entry(AttributeKeys.SCALE.key(), 1.0),
            Map.entry(AttributeKeys.SNEAKING_SPEED.key(), 0.3),
            Map.entry(AttributeKeys.SPAWN_REINFORCEMENTS.key(), 0.0),
            Map.entry(AttributeKeys.STEP_HEIGHT.key(), 0.6),
            Map.entry(AttributeKeys.SUBMERGED_MINING_SPEED.key(), 0.2),
            Map.entry(AttributeKeys.SWEEPING_DAMAGE_RATIO.key(), 0.0),
            Map.entry(AttributeKeys.TEMPT_RANGE.key(), 10.0),
            Map.entry(AttributeKeys.WATER_MOVEMENT_EFFICIENCY.key(), 0.0),
            Map.entry(AttributeKeys.WAYPOINT_TRANSMIT_RANGE.key(), 60_000_000.0),
            Map.entry(AttributeKeys.WAYPOINT_RECEIVE_RANGE.key(), 60_000_000.0));

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
