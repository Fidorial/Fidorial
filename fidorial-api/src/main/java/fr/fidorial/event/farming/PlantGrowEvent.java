package fr.fidorial.event.farming;

import fr.fidorial.event.Cancellable;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.farming.FarmingWorld;

/**
 * Fired when a plant is about to grow: a crop reaching its next stage, a cane growing one block
 * taller, a stem bearing a fruit. {@link #position()} is the plant growing, and {@link #block()}
 * its state before it grows.
 *
 * <p>Changing {@linkplain #setGrown(BlockData) the grown state} changes what the plant turns into.
 * Cancelling the event leaves the plant as it is; it tries again on a later random tick, or the
 * next time it is fertilized.</p>
 *
 * @see FarmingWorld#grow(BlockPosition, BlockPosition, BlockData, boolean)
 * @since 0.1.0
 */
public interface PlantGrowEvent extends FarmingEvent, Cancellable {

    /**
     * {@return where the grown state is about to be written}: {@link #position()} for a plant
     * growing in place, another block for a plant growing taller or bearing a fruit
     *
     * @since 0.1.0
     */
    BlockPosition target();

    /**
     * {@return the state about to be written at {@link #target()}}
     *
     * @since 0.1.0
     */
    BlockData grown();

    /**
     * Sets the state written at {@link #target()} once the event is over, in place of the one the
     * plant chose.
     *
     * @param grown the state to write
     * @since 0.1.0
     */
    void setGrown(BlockData grown);

    /**
     * {@return {@code true} if a fertilizer causes the growth, {@code false} if the plant grows on
     * its own}
     *
     * @since 0.1.0
     */
    boolean fertilized();
}
