package fr.euphyllia.fidorial.server.combat;

import net.kyori.adventure.key.Key;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The cooldowns of the items of a player, by cooldown group: a shield an axe disabled, an ender
 * pearl just thrown.
 */
public final class UseCooldowns {

    private final Map<Key, Long> ends = new ConcurrentHashMap<>();

    /**
     * {@return {@code true} while a cooldown group is cooling down}
     *
     * @param group the cooldown group
     * @param now   the current tick of the player
     */
    public boolean isCoolingDown(final Key group, final long now) {
        final Long end = ends.get(group);
        if (end == null) {
            return false;
        }
        if (end <= now) {
            ends.remove(group, end);
            return false;
        }
        return true;
    }

    /**
     * Starts the cooldown of a group, or extends it.
     *
     * @param group the cooldown group
     * @param ticks how long it lasts
     * @param now   the current tick of the player
     */
    public void start(final Key group, final int ticks, final long now) {
        ends.merge(group, now + ticks, Math::max);
    }

    /**
     * Forgets every cooldown.
     */
    public void clear() {
        ends.clear();
    }
}
