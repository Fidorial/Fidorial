package fr.fidorial.event.block;

import fr.fidorial.event.Event;
import fr.fidorial.inventory.HopperInventory;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.world.World;

/**
 * Something a hopper does with items: moving them between containers, or picking them up.
 *
 * <p>Fired on the thread of the region owning the hopper.</p>
 *
 * @since 0.1.0
 */
public interface HopperEvent extends Event {

    /**
     * {@return the world of the hopper}
     *
     * @since 0.1.0
     */
    World world();

    /**
     * {@return where the hopper is}
     *
     * @since 0.1.0
     */
    BlockPosition position();

    /**
     * {@return the slots of the hopper}
     *
     * @since 0.1.0
     */
    HopperInventory hopper();
}
