package fr.fidorial.event.player;

import fr.fidorial.entity.ItemEntity;
import fr.fidorial.event.Cancellable;
import fr.fidorial.item.ItemStack;

/**
 * Fired when a player walks over an item on the ground and is about to pick it up.
 *
 * <p>Cancelling it leaves the item on the ground; the event fires again on the next attempt.</p>
 *
 * @since 0.1.0
 */
public interface PlayerPickupItemEvent extends PlayerEvent, Cancellable {

    /**
     * {@return the item entity on the ground}
     *
     * @since 0.1.0
     */
    ItemEntity item();

    /**
     * {@return what goes into the inventory: the whole stack on the ground, or the part of it that
     * fits; the rest stays on the ground}
     *
     * @since 0.1.0
     */
    ItemStack taken();
}
