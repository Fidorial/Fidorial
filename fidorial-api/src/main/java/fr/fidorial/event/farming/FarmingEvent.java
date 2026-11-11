package fr.fidorial.event.farming;

import fr.fidorial.event.Event;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.world.World;
import fr.fidorial.world.block.BlockData;

/**
 * An event about one block taking part in farming.
 *
 * @since 0.1.0
 */
public interface FarmingEvent extends Event {

    /**
     * {@return the world of the block}
     *
     * @since 0.1.0
     */
    World world();

    /**
     * {@return the position of the block}
     *
     * @since 0.1.0
     */
    BlockPosition position();

    /**
     * {@return the state of the block when the event was posted, before the change it describes}
     *
     * @since 0.1.0
     */
    BlockData block();
}
