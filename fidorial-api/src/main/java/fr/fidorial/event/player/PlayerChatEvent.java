package fr.fidorial.event.player;

import fr.fidorial.entity.Player;
import fr.fidorial.event.Cancellable;
import net.kyori.adventure.text.Component;

/**
 * Fired when a player sends a chat message, before it is broadcast.
 *
 * <p>Cancelling the event drops the message.</p>
 *
 * @see PlayerSignedChatEvent
 * @since 0.1.0
 */
public class PlayerChatEvent implements PlayerEvent, Cancellable {

    private final Player player;
    private Component message;
    private boolean cancelled;

    /**
     * Creates an event.
     *
     * @param player  the player who sent the message
     * @param message the message about to be broadcast
     * @since 0.1.0
     */
    public PlayerChatEvent(final Player player, final Component message) {
        this.player = player;
        this.message = message;
    }

    @Override
    public Player player() {
        return player;
    }

    /**
     * {@return the message about to be broadcast, possibly replaced by a previous listener}
     *
     * @since 0.1.0
     */
    public Component message() {
        return message;
    }

    /**
     * Replaces the message about to be broadcast.
     *
     * @param message the message to broadcast instead
     * @since 0.1.0
     */
    public void setMessage(final Component message) {
        this.message = message;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }
}
