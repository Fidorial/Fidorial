package fr.fidorial.event.farming;

import fr.fidorial.entity.player.Player;
import fr.fidorial.event.Cancellable;
import fr.fidorial.item.ItemStack;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.farming.FarmingRegistry;

/**
 * Fired when a player is about to till a block with a hoe: {@link #block()} is the soil as it is,
 * {@link #tilled()} what it is about to become.
 *
 * <p>Changing {@linkplain #setTilled(BlockData) the tilled state} changes what the soil turns into.
 * Cancelling the event leaves the soil as it is, and the hoe unworn.</p>
 *
 * @see FarmingRegistry#tillable
 * @since 0.1.0
 */
public interface SoilTillEvent extends FarmingEvent, Cancellable {

    /**
     * {@return the player tilling the soil}
     *
     * @since 0.1.0
     */
    Player player();

    /**
     * {@return the hoe the soil is tilled with}
     *
     * @since 0.1.0
     */
    ItemStack tool();

    /**
     * {@return the state the soil is about to turn into}
     *
     * @since 0.1.0
     */
    BlockData tilled();

    /**
     * Sets the state the soil turns into once the event is over.
     *
     * @param tilled the state to write
     * @since 0.1.0
     */
    void setTilled(BlockData tilled);
}
