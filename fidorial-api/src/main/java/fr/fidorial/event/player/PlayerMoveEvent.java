package fr.fidorial.event.player;

import fr.fidorial.entity.Player;
import fr.fidorial.event.Cancellable;
import fr.fidorial.math.Location;

public final class PlayerMoveEvent implements PlayerEvent, Cancellable {

    private final Player player;
    private final Location from;
    private final Location to;
    private boolean cancelled;

    public PlayerMoveEvent(final Player player, final Location from, final Location to) {
        this.player = player;
        this.from = from;
        this.to = to;
    }

    @Override
    public Player player() {
        return player;
    }

    public Location from() {
        return from;
    }

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
