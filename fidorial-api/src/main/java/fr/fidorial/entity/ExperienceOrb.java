package fr.fidorial.entity;

import fr.fidorial.math.Location;
import fr.fidorial.world.World;

/**
 * An orb of experience on the ground ({@code minecraft:experience_orb}), left by a mob a player
 * killed, by two animals that bred or by a player who died.
 *
 * <p>It falls, flies to the closest player within {@value #FOLLOW_RANGE} blocks, merges with orbs
 * of the same value nearby and despawns after {@value #DEFAULT_LIFETIME} ticks. A player who touches
 * it takes its {@linkplain #value() value} once every other tick, as many times as its
 * {@linkplain #count() count}.</p>
 *
 * <p>Like every entity, change it from the region thread owning its chunk.</p>
 *
 * @see World#dropExperience(Location, int)
 * @since 0.1.0
 */
public interface ExperienceOrb extends Entity {

    /**
     * How many ticks an orb stays on the ground before it despawns: five minutes.
     *
     * @since 0.1.0
     */
    int DEFAULT_LIFETIME = 6000;

    /**
     * How far an orb notices a player to fly to, in blocks.
     *
     * @since 0.1.0
     */
    int FOLLOW_RANGE = 8;

    /**
     * {@return how many experience points a player gets each time they take this orb}
     *
     * @since 0.1.0
     */
    int value();

    /**
     * {@return how many times this orb gives its value}: orbs of the same value merge into one
     *
     * @since 0.1.0
     */
    int count();

    /**
     * {@return for how many ticks this orb has been on the ground}
     *
     * @since 0.1.0
     */
    int age();

    /**
     * Changes for how long this orb has been on the ground; it despawns at
     * {@value #DEFAULT_LIFETIME} ticks.
     *
     * @param ticks the new age, in ticks
     * @since 0.1.0
     */
    void setAge(int ticks);

    /**
     * {@return the value of the biggest orb that fits in an amount of experience}
     *
     * <p>As in Vanilla, an amount is split into orbs of 2477, 1237, 617, 307, 149, 73, 37, 17, 7, 3
     * and 1 points, the biggest first.</p>
     *
     * @param amount the experience left to give out, positive
     * @since 0.1.0
     */
    static int biggestOrbValue(final int amount) {
        if (amount >= 2477) {
            return 2477;
        } else if (amount >= 1237) {
            return 1237;
        } else if (amount >= 617) {
            return 617;
        } else if (amount >= 307) {
            return 307;
        } else if (amount >= 149) {
            return 149;
        } else if (amount >= 73) {
            return 73;
        } else if (amount >= 37) {
            return 37;
        } else if (amount >= 17) {
            return 17;
        } else if (amount >= 7) {
            return 7;
        }
        return amount >= 3 ? 3 : 1;
    }
}
