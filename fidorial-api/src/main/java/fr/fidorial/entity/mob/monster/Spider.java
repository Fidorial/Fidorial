package fr.fidorial.entity.mob.monster;

import fr.fidorial.entity.mob.Mob;

/**
 * A spider ({@code minecraft:spider}), or a cave spider: it climbs walls, leaps at its target, and
 * leaves the players alone in daylight unless they hurt it.
 *
 * <p>Like every entity, change it from the region thread owning its chunk.</p>
 *
 * @since 0.1.0
 */
public interface Spider extends Mob {

    /**
     * {@return {@code true} while this spider climbs a wall}
     *
     * @since 0.1.0
     */
    boolean isClimbing();
}
