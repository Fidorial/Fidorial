package fr.fidorial.event.mob;

import fr.fidorial.event.Cancellable;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.world.block.BlockData;

/**
 * Fired before a mob changes a block of its world: an enderman picking a block up or putting it
 * down, a silverfish slipping into a block or waking out of it, a zombie breaking a door, a sheep
 * eating grass.
 *
 * <p>Cancelling it leaves the block as it is, and the mob does without: an enderman keeps its hands
 * empty or full, a silverfish stays out, a door stays up. A sheep that grazed still grows its wool
 * back.</p>
 *
 * @since 0.1.0
 */
public interface MobChangeBlockEvent extends MobEvent, Cancellable {

    /**
     * {@return where the block lies, in the world of the mob}
     *
     * @since 0.1.0
     */
    BlockPosition position();

    /**
     * {@return the block there now}
     *
     * @since 0.1.0
     */
    BlockData from();

    /**
     * {@return the block the mob is about to put there}
     *
     * @since 0.1.0
     */
    BlockData to();
}
