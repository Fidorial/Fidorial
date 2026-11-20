package fr.fidorial.event.mob;

import fr.fidorial.entity.mob.Mob;
import fr.fidorial.event.Event;

/**
 * An {@link Event} about what a single mob does.
 *
 * <p>Subscribing to this interface observes every mob event.</p>
 *
 * @since 0.1.0
 */
public interface MobEvent extends Event {

    /**
     * {@return the mob involved in this event}
     *
     * @since 0.1.0
     */
    Mob mob();
}
