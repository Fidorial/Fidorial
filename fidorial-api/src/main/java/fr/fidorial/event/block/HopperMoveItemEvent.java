package fr.fidorial.event.block;

import fr.fidorial.event.Cancellable;
import fr.fidorial.inventory.Container;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.BlockPosition;

/**
 * Fired when a hopper is about to move items: pushing them into the container its spout faces, or
 * pulling them from the container above it.
 *
 * <p>Cancelling it leaves the items where they are, and the hopper waits its cooldown as if it had
 * moved them: a move refused every time does not fire on every game tick.</p>
 *
 * @since 0.1.0
 */
public interface HopperMoveItemEvent extends HopperEvent, Cancellable {

    /**
     * {@return {@code true} when the hopper pushes the items out, {@code false} when it pulls them in}
     *
     * @since 0.1.0
     */
    boolean isPush();

    /**
     * {@return where the items come from: the hopper when it pushes, the container above it when it
     * pulls}
     *
     * @since 0.1.0
     */
    BlockPosition sourcePosition();

    /**
     * {@return where the items go: the container its spout faces when it pushes, the hopper when it
     * pulls}
     *
     * @since 0.1.0
     */
    BlockPosition destinationPosition();

    /**
     * {@return the container the items come from, such as a large chest, seen whole}
     *
     * @since 0.1.0
     */
    Container source();

    /**
     * {@return the container the items go to, such as a large chest, seen whole}
     *
     * @since 0.1.0
     */
    Container destination();

    /**
     * {@return the items that move, taken from one slot: as many as the hopper moves at once, or
     * fewer when that slot or the room of the destination holds fewer}
     *
     * @since 0.1.0
     */
    ItemStack item();
}
