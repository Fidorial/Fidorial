package fr.fidorial.entity.mob.monster;

import fr.fidorial.entity.mob.Mob;

/**
 * A zombie ({@code minecraft:zombie}), or one of its kin: the husk, the drowned and the frostbite.
 *
 * <p>Some zombies are babies, faster and smaller; some lead the others. A zombie that keeps its
 * eyes under water for 30 seconds turns into its drowned form, and one that stays in powder snow
 * for 7 seconds into its frozen form.</p>
 *
 * <p>Like every entity, change it from the region thread owning its chunk.</p>
 *
 * @since 0.1.0
 */
public interface Zombie extends Mob {

    /**
     * {@return {@code true} for a baby zombie}
     *
     * @since 0.1.0
     */
    boolean isBaby();

    /**
     * Makes this zombie a baby, or an adult again.
     *
     * @param baby {@code true} for a baby
     * @since 0.1.0
     */
    void setBaby(boolean baby);

    /**
     * {@return {@code true} if this zombie leads the others}: it has more health, calls for
     * reinforcements far more often and breaks doors
     *
     * @since 0.1.0
     */
    boolean isLeader();

    /**
     * {@return {@code true} if this zombie breaks the wooden doors in its way}, which it only does on
     * hard difficulty
     *
     * @since 0.1.0
     */
    boolean canBreakDoors();

    /**
     * Lets this zombie break the wooden doors in its way, on hard difficulty.
     *
     * @param canBreakDoors {@code true} to let it
     * @since 0.1.0
     */
    void setCanBreakDoors(boolean canBreakDoors);

    /**
     * {@return the chance, between {@code 0} and {@code 1}, that this zombie calls a reinforcement
     * when it is hurt on hard difficulty}
     *
     * @since 0.1.0
     */
    double reinforcementChance();

    /**
     * Sets how likely this zombie calls a reinforcement, whatever was rolled for it.
     *
     * @param chance the chance, between {@code 0} and {@code 1}
     * @since 0.1.0
     */
    void setReinforcementChance(double chance);

    /**
     * {@return {@code true} while this zombie shakes under water, about to turn into its drowned
     * form}
     *
     * @since 0.1.0
     */
    boolean isUnderWaterConverting();

    /**
     * {@return {@code true} while this zombie shakes in powder snow, about to turn into its frozen
     * form}
     *
     * @since 0.1.0
     */
    boolean isFreezeConverting();

    /**
     * Sets for how many ticks this zombie has had its eyes under water; it starts to turn into its
     * drowned form at 600.
     *
     * @param ticks the time under water
     * @since 0.1.0
     */
    void setInWaterTime(int ticks);

    /**
     * Sets for how many ticks this zombie has been in powder snow; it starts to turn into its frozen
     * form at 140.
     *
     * @param ticks the time in powder snow
     * @since 0.1.0
     */
    void setFreezingTime(int ticks);
}
