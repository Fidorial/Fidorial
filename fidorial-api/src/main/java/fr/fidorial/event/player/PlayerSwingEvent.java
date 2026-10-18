package fr.fidorial.event.player;

import fr.fidorial.entity.player.Player;
import fr.fidorial.event.Cancellable;

/**
 * Fired when a player swings their arm with the attack key (left click by default), whether or not
 * something is hit.
 *
 * <p>Cancelling the event hides the swing from the other players. Listeners run on the network thread
 * of the player, keep them short.</p>
 *
 * @since 0.1.0
 */
public final class PlayerSwingEvent implements PlayerEvent, Cancellable {

    private final Player player;
    private boolean cancelled;

    /**
     * Creates an event.
     *
     * @param player the player swinging
     * @since 0.1.0
     */
    public PlayerSwingEvent(final Player player) {
        this.player = player;
    }

    @Override
    public Player player() {
        return player;
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