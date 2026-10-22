package fr.fidorial.event.player;

import fr.fidorial.entity.player.Player;
import fr.fidorial.event.Cancellable;

/**
 * Fired when a player selects another hotbar slot, with the number keys or the mouse wheel.
 *
 * @since 0.1.0
 */
public final class PlayerItemHeldEvent implements PlayerEvent, Cancellable {

    private final Player player;
    private final int previousSlot;
    private final int newSlot;
    private boolean cancelled;

    /**
     * Creates an event.
     *
     * @param player       the player
     * @param previousSlot the slot selected before, {@code 0}-{@code 8}
     * @param newSlot      the slot selected now, {@code 0}-{@code 8}
     * @since 0.1.0
     */
    public PlayerItemHeldEvent(final Player player, final int previousSlot, final int newSlot) {
        this.player = player;
        this.previousSlot = previousSlot;
        this.newSlot = newSlot;
    }

    @Override
    public Player player() {
        return player;
    }

    /**
     * {@return the hotbar slot selected before, {@code 0}-{@code 8}}
     *
     * @since 0.1.0
     */
    public int previousSlot() {
        return previousSlot;
    }

    /**
     * {@return the hotbar slot the player selects, {@code 0}-{@code 8}}
     *
     * @since 0.1.0
     */
    public int newSlot() {
        return newSlot;
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
