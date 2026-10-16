package fr.fidorial.event.player;

import fr.fidorial.entity.Player;
import fr.fidorial.event.Cancellable;
import fr.fidorial.inventory.EnderChestInventory;
import fr.fidorial.math.BlockPosition;

/**
 * Fired right before a player opens an ender chest.
 *
 * <p>Cancelling the event keeps the container closed.</p>
 *
 * @since 0.1.0
 */
public final class PlayerOpenEnderChestEvent implements PlayerEvent, Cancellable {

    private final Player player;
    private final BlockPosition position;
    private final EnderChestInventory enderChest;
    private boolean cancelled;

    /**
     * Creates an event.
     *
     * @param player     the player opening the ender chest
     * @param position   the position of the ender chest block
     * @param enderChest the container about to be displayed
     * @since 0.1.0
     */
    public PlayerOpenEnderChestEvent(final Player player, final BlockPosition position, final EnderChestInventory enderChest) {
        this.player = player;
        this.position = position;
        this.enderChest = enderChest;
    }

    @Override
    public Player player() {
        return player;
    }

    /**
     * {@return the position of the ender chest block}
     *
     * @since 0.1.0
     */
    public BlockPosition position() {
        return position;
    }

    /**
     * {@return the container about to be displayed; changes made to it are kept}
     *
     * @since 0.1.0
     */
    public EnderChestInventory enderChest() {
        return enderChest;
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
