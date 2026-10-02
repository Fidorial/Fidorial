package fr.fidorial.event;

import fr.fidorial.event.player.PlayerEvent;

/**
 * Marker for everything that can be {@linkplain EventBus#post(Event) posted} on the
 * {@link EventBus}.
 *
 * <p>A listener subscribed to a type also receives its subtypes, so subscribing to an interface
 * such as {@link PlayerEvent} observes every player event.</p>
 *
 * @since 0.1.0
 */
public interface Event {
}
