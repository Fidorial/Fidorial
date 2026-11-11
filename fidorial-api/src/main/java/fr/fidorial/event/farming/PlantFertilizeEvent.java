package fr.fidorial.event.farming;

import fr.fidorial.entity.player.Player;
import fr.fidorial.event.Cancellable;
import fr.fidorial.item.ItemStack;
import fr.fidorial.world.farming.FarmingBehaviour;
import org.jspecify.annotations.Nullable;

/**
 * Fired when a fertilizer is about to be used on a block that
 * {@linkplain FarmingBehaviour#canFertilize takes it}, before the item is used up and before the
 * block is {@linkplain FarmingBehaviour#fertilize fed}. The growth that follows fires its own
 * {@link PlantGrowEvent}.
 *
 * <p>Cancelling the event leaves both the block and the item as they are.</p>
 *
 * @since 0.1.0
 */
public interface PlantFertilizeEvent extends FarmingEvent, Cancellable {

    /**
     * {@return the player using the fertilizer, {@code null} when nobody does}
     *
     * @since 0.1.0
     */
    @Nullable Player player();

    /**
     * {@return the stack the fertilizer is taken from}
     *
     * @since 0.1.0
     */
    ItemStack fertilizer();
}
