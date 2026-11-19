package fr.fidorial.entity.mob.monster;

import fr.fidorial.entity.mob.Mob;

/**
 * An endermite ({@code minecraft:endermite}): a tiny monster that goes after players, and that the
 * endermen go after. It vanishes once it has lived two minutes, unless it is persistent.
 *
 * <p>Like every entity, change it from the region thread owning its chunk.</p>
 *
 * @since 0.1.0
 */
public interface Endermite extends Mob {

    /**
     * How many ticks an endermite lives before it vanishes: two minutes.
     *
     * @since 0.1.0
     */
    int MAX_LIFE = 2400;

    /**
     * {@return how many ticks this endermite has lived}, its time as a persistent one left out
     *
     * @since 0.1.0
     */
    int life();

    /**
     * Sets how many ticks this endermite has lived: it vanishes once that reaches {@value #MAX_LIFE}.
     *
     * @param ticks the time it lived, {@code 0} or more
     * @since 0.1.0
     */
    void setLife(int ticks);
}
