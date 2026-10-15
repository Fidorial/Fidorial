package fr.fidorial.event.player;

import fr.fidorial.event.Cancellable;
import fr.fidorial.event.block.BlockEvent;
import fr.fidorial.inventory.EnderChestInventory;

/**
 * Fired right before a player opens an ender chest.
 *
 * <p>Cancelling the event keeps the container closed.</p>
 *
 * @since 0.1.0
 */
public interface PlayerOpenEnderChestEvent extends PlayerEvent, BlockEvent, Cancellable {

    /**
     * {@return the container about to be displayed; changes made to it are kept}
     *
     * @since 0.1.0
     */
    EnderChestInventory enderChest();
}
