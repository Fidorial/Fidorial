package fr.fidorial.attribute;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.ApiStatus;

/**
 * @deprecated Use {@link fr.fidorial.registry.keys.AttributeKeys} instead.
 */
@Deprecated
@ApiStatus.Obsolete
public class Attribute {

    /**
     * This attribute determines the air drag an entity experiences.
     */
    public static final Key AIR_DRAG_MODIFIER = Key.key("air_drag_modifier");
    /**
     * This attribute determines the number of armor points an entity has.
     */
    public static final Key ARMOR = Key.key("armor");
    /**
     * This attribute determines an entity's armor toughness.
     */
    public static final Key ARMOR_TOUGHNESS = Key.key("armor_toughness");
    /**
     * This attribute determines the damage dealt by an entity's melee attack.
     * Only the entities listed below have this attribute.
     */
    public static final Key ATTACK_DAMAGE = Key.key("attack_damage");
    /**
     * This attribute determines additional knockback applied to an entity's melee attack
     */
    public static final Key ATTACK_KNOCKBACK = Key.key("attack_knockback");
    /**
     * This attribute determines the rate at which a player's attack cooldown recharges.
     * Its value is the number of full-strength attacks per second.
     * Only players have this attribute.
     */
    public static final Key ATTACK_SPEED = Key.key("attack_speed");
    /**
     * This attribute determines the distance within which a scoreboard objective with a below_name display slot is visible above an entity.
     */
    public static final Key BELOW_NAME_DISTANCE = Key.key("below_name_distance");
    /**
     * This attribute multiplies the speed at which a player breaks blocks. Only players have this attribute.
     */
    public static final Key BLOCK_BREAK_SPEED = Key.key("block_break_speed");
    /**
     * This attribute determines the block interaction range for players. Only players have this attribute.
     */
    public static final Key BLOCK_INTERACTION_RANGE = Key.key("block_interaction_range");
    /**
     * This attribute determines the restitution of motion an entity is given when landing on the ground.
     */
    public static final Key BOUNCINESS = Key.key("bounciness");
    /**
     * This attribute multiplies the duration for which an entity should remain on fire after being ignited.
     */
    public static final Key BURNING_TIME = Key.key("burning_time");
    /**
     * This attribute determines the maximum distance at which the camera is placed away from the player or spectated entity when in a third-person view.
     * This distance is multiplied by the scale attribute to get a final target camera distance.
     * If the entity being ridden has a larger camera_distance attribute, that distance will be used.
     * The final distance can be reduced by colliding certain blocks.
     */
    public static final Key CAMERA_DISTANCE = Key.key("camera_distance");
    /**
     * This attribute determines the entity interaction range for players.
     */
    public static final Key ENTITY_INTERACTION_RANGE = Key.key("entity_interaction_range");
    /**
     * This attribute determines the proportion of knockback from explosions an entity resists.
     * A value of 1 eliminates the knockback.
     */
    public static final Key EXPLOSION_KNOCKBACK_RESISTANCE = Key.key("explosion_knockback_resistance");
    /**
     * This attribute multiplies the amount of fall damage an entity receives.
     */
    public static final Key FALL_DAMAGE_MULTIPLIER = Key.key("fall_damage_multiplier");
    /**
     * This attribute determines the flying speed of an entity.
     * Only the entities listed below have this attribute.
     */
    public static final Key FLYING_SPEED = Key.key("flying_speed");
    /**
     * This attribute determines the range in blocks within which a mob targets players or other mobs to track.
     * Exiting this range causes the mob to cease following the player/mob.
     * Only mobs have this attribute.
     */
    public static final Key FOLLOW_RANGE = Key.key("follow_range");
    /**
     * This attribute determines the friction experienced by an entity.
     */
    public static final Key FRICTION_MODIFIER = Key.key("friction_modifier");
    /**
     * This attribute determines the gravity affecting an entity in blocks per tick squared.
     */
    public static final Key GRAVITY = Key.key("gravity");
    /**
     * This attribute determines the initial vertical velocity of an entity when they jump, in blocks per tick.
     */
    public static final Key JUMP_STRENGTH = Key.key("jump_strength");
    /**
     * This attribute determines the proportion of horizontal knockback from attacks and projectiles an entity resists.
     * A value of 1 eliminates the knockback.
     * Vertical knockback is not affected.
     * It does not affect explosions.
     */
    public static final Key KNOCKBACK_RESISTANCE = Key.key("knockback_resistance");
    /**
     * This attribute affects the results of loot tables, with a higher attribute value increasing the amount and quality of results.
     * The number of rolls is increased by the pools "bonus rolls"-value times this attributes value, rounded down,
     * and the weight of each entry is increased by its quality times this attributes value, also rounded down.
     * Only players have this attribute.
     */
    public static final Key LUCK = Key.key("luck");
    /**
     * This attribute determines an entity's maximum absorption.
     */
    public static final Key MAX_ABSORPTION = Key.key("max_absorption");
    /**
     * This attribute determines an entity's maximum health.
     * It determines the highest health they may be healed to.
     */
    public static final Key MAX_HEALTH = Key.key("max_health");
    /**
     * This attribute increases the speed at which a player breaks blocks.
     * Only players have this attribute.
     */
    public static final Key MINING_EFFICIENCY = Key.key("mining_efficiency");
    /**
     * This attribute is a factor to improve walking on terrain that slows down movement, such as soul sand.
     * A value of 1 removes the slowdown.
     */
    public static final Key MOVEMENT_EFFICIENCY = Key.key("movement_efficiency");
    /**
     * This attribute determines the speed at which an entity moves.
     * It represents a multiplier for acceleration per tick.
     * A mob's actual speed in blocks/second is approximately 43 times this value (see <a href="https://minecraft.wiki/w/Entity#Formulas">Entity#Formulas</a>.)
     */
    public static final Key MOVEMENT_SPEED = Key.key("movement_speed");
    /**
     * This attribute determines the distance within which an entity's custom name is visible.
     */
    public static final Key NAME_TAG_DISTANCE = Key.key("name_tag_distance");
    /**
     * This attribute determines the chance that an entity's remaining air decreases while underwater in any given game tick.
     * The chance is given by 1⁄(oxygen_bonus + 1).
     */
    public static final Key OXYGEN_BONUS = Key.key("oxygen_bonus");
    /**
     * This attribute determines the distance in blocks an entity can fall before fall damage starts to accumulate.
     */
    public static final Key SAFE_FALL_DISTANCE = Key.key("safe_fall_distance");
    /**
     * This attribute multiplies an entity's size and hitbox dimensions.
     */
    public static final Key SCALE = Key.key("scale");
    /**
     * This attribute multiplies a player's movement speed when sneaking or crawling.
     * Only players have this attribute.
     */
    public static final Key SNEAKING_SPEED = Key.key("sneaking_speed");
    /**
     * This attribute determines the chance for a zombie to spawn another zombie when attacked.
     * Only the entities listed below have this attribute.
     */
    public static final Key SPAWN_REINFORCEMENTS = Key.key("spawn_reinforcements");
    /**
     * This attribute determines the maximum number of blocks that an entity can step up without jumping.
     * Sneaking prevents drops from heights that are higher than this value.
     */
    public static final Key STEP_HEIGHT = Key.key("step_height");
    /**
     * This attribute multiplies a player's mining speed when underwater.
     * Only players have this attribute.
     */
    public static final Key SUBMERGED_MINING_SPEED = Key.key("submerged_mining_speed");
    /**
     * This attribute determines the proportion of the base attack damage dealt to secondary targets when a player performs a sweep attack.
     * This is in addition to the base damage of the sweep damage itself.
     * Only players have this attribute.
     */
    public static final Key SWEEPING_DAMAGE_RATIO = Key.key("sweeping_damage_ratio");
    /**
     * This attribute determines the range in blocks in which a mob can be tempted, following a player holding a specific item.
     */
    public static final Key TEMPT_RANGE = Key.key("tempt_range");
    /**
     * This attribute determines movement speed and drag when submerged.
     */
    public static final Key WATER_MOVEMENT_EFFICIENCY = Key.key("water_movement_efficiency");
    /**
     * This attribute determines the maximum distance from a player to a waypoint at which the waypoint is displayed on the locator bar.
     * Only players have this attribute.
     */
    public static final Key WAYPOINT_RECEIVE_RANGE = Key.key("waypoint_receive_range");
    /**
     * This attribute determines the distance at which an entity displays as a waypoint on the locator bar.
     */
    public static final Key WAYPOINT_TRANSMIT_RANGE = Key.key("waypoint_transmit_range");

    private Attribute() {
    }
}
