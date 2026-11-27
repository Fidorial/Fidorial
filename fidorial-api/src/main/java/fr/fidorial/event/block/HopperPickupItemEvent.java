package fr.fidorial.event.block;

import fr.fidorial.entity.ItemEntity;
import fr.fidorial.event.Cancellable;
import fr.fidorial.item.ItemStack;

/**
 * Fired when a hopper is about to pick up an item lying above it.
 *
 * <p>Cancelling it leaves the item where it is; the event fires again on the next game tick the
 * item is still there.</p>
 *
 * @since 0.1.0
 */
public interface HopperPickupItemEvent extends HopperEvent, Cancellable {

    /**
     * {@return the item entity above the hopper}
     *
     * @since 0.1.0
     */
    ItemEntity item();

    /**
     * {@return what goes into the hopper: the whole stack of the item entity, or the part of it that
     * fits; the rest stays where it lies}
     *
     * @since 0.1.0
     */
    ItemStack taken();
}
