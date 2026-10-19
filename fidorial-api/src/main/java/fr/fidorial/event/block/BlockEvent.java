package fr.fidorial.event.block;

import fr.fidorial.event.Event;
import fr.fidorial.world.BlockPos;
import fr.fidorial.world.World;

/**
 * An {@link Event} about a single block; subscribing to it observes every block event.
 *
 * @since 0.1.0
 */
public interface BlockEvent extends Event {

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
    BlockPos position();
}
