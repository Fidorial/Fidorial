package fr.fidorial.event.player;

import fr.fidorial.entity.Player;
import fr.fidorial.event.Cancellable;
import fr.fidorial.world.Location;

/**
 * Fired when a player moves or turns, before the new position is applied.
 *
 * <p>Cancelling the event teleports the player back to {@link #from()}.</p>
 *
 * @since 0.1.0
 */
public final class PlayerMoveEvent implements PlayerEvent, Cancellable {

    private final Player player;
    private final Location from;
    private final Location to;
    private boolean cancelled;

    /**
     * Creates an event.
     *
     * @param player the player moving
     * @param from   the location before the move
     * @param to     the location after the move
     * @since 0.1.0
     */
    public PlayerMoveEvent(final Player player, final Location from, final Location to) {
        this.player = player;
        this.from = from;
        this.to = to;
    }

    @Override
    public Player player() {
        return player;
    }

    /**
     * {@return the location before the move}
     *
     * @since 0.1.0
     */
    public Location from() {
        return from;
    }

    /**
     * {@return the location the player is moving to}
     *
     * @since 0.1.0
     */
    public Location to() {
        return to;
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
