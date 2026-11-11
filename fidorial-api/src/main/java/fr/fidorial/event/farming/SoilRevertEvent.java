package fr.fidorial.event.farming;

import fr.fidorial.event.Cancellable;
import fr.fidorial.world.block.BlockData;

/**
 * Fired when tilled soil is about to turn back into plain soil: it dried out with nothing planted
 * on it, a solid block covers it, or something {@linkplain SoilTrampleEvent trampled} it.
 *
 * <p>Changing {@linkplain #setReverted(BlockData) the reverted state} changes what the soil turns
 * into. Cancelling the event keeps the soil tilled.</p>
 *
 * @since 0.1.0
 */
public interface SoilRevertEvent extends FarmingEvent, Cancellable {

    /**
     * {@return the state the soil is about to turn into}
     *
     * @since 0.1.0
     */
    BlockData reverted();

    /**
     * Sets the state the soil turns into once the event is over.
     *
     * @param reverted the state to write
     * @since 0.1.0
     */
    void setReverted(BlockData reverted);
}
