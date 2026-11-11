package fr.fidorial.event.farming;

import fr.fidorial.entity.player.Player;
import fr.fidorial.event.Cancellable;
import fr.fidorial.item.ItemStack;
import fr.fidorial.world.farming.FarmingBehaviour;
import fr.fidorial.world.farming.FarmingWorld;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Fired when a block taking part in farming is about to drop what it gives: a player who is not in
 * creative mode breaks it or picks its fruits, or it breaks on its own because it can no longer
 * stand. {@link #block()} is the state giving the harvest.
 *
 * <p>{@link #drops()} can be modified: what is left in it once the event is over is dropped.
 * Cancelling the event drops nothing; the block is broken or picked all the same.</p>
 *
 * @see FarmingBehaviour#drops
 * @see FarmingWorld#harvest
 * @since 0.1.0
 */
public interface HarvestEvent extends FarmingEvent, Cancellable {

    /**
     * {@return the player harvesting the block, {@code null} when it breaks on its own}
     *
     * @since 0.1.0
     */
    @Nullable Player player();

    /**
     * {@return what the block is harvested with, {@link ItemStack#EMPTY} for a bare hand or when
     * the block breaks on its own}
     *
     * @since 0.1.0
     */
    ItemStack tool();

    /**
     * {@return the stacks about to be dropped, which listeners may add to, remove from or replace}
     *
     * @since 0.1.0
     */
    List<ItemStack> drops();
}
