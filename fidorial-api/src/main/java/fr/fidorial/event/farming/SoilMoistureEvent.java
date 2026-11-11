package fr.fidorial.event.farming;

import fr.fidorial.event.Cancellable;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.data.type.Farmland;

/**
 * Fired when tilled soil is about to get wetter or drier: water or rain soaking it, or the lack of
 * both drying it out one step. {@link #block()} is the soil as it is, {@link #moistened()} what it
 * is about to become; both usually are {@link Farmland}.
 *
 * <p>Cancelling the event keeps the moisture the soil has.</p>
 *
 * @since 0.1.0
 */
public interface SoilMoistureEvent extends FarmingEvent, Cancellable {

    /**
     * {@return the state the soil is about to take, with its new moisture}
     *
     * @since 0.1.0
     */
    BlockData moistened();
}
