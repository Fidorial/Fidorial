package fr.fidorial.event.player;

import fr.fidorial.entity.Player;
import fr.fidorial.event.Event;

/**
 * An {@link Event} involving a single player.
 *
 * @since 0.1.0
 */
public interface PlayerEvent extends Event {

    /**
     * {@return the player involved in this event}
     *
     * @since 0.1.0
     */
    Player player();
}
