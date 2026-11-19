package fr.fidorial.entity.mob.monster;

import fr.fidorial.entity.mob.Mob;

/**
 * A silverfish ({@code minecraft:silverfish}): a tiny monster that hides in stone. When something
 * hurts it, it wakes the silverfish hiding in the blocks around a second later; with nothing to do,
 * it slips into a stone block next to it when mobs may change the world.
 *
 * <p>Like every entity, change it from the region thread owning its chunk.</p>
 *
 * @since 0.1.0
 */
public interface Silverfish extends Mob {

    /**
     * Makes this silverfish wake those hiding in the blocks around a second from now, as when
     * something hurts it.
     *
     * @since 0.1.0
     */
    void wakeUpFriends();
}
