package fr.fidorial.event.block;

import fr.fidorial.event.Cancellable;
import fr.fidorial.event.farming.HarvestEvent;
import fr.fidorial.event.player.PlayerEvent;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.world.World;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.farming.FarmingWorld;

import java.util.List;

/**
 * Fired when a player breaks a block, before the block is removed from the world.
 *
 * <p>{@link #drops()} holds what the block is about to drop and can be modified: what is left in
 * it once the event is over is dropped where the block stood. Cancelling the event keeps the block
 * in place, drops nothing and resyncs the position to the client.</p>
 *
 * <p>Fired on the thread of the region owning the block.</p>
 *
 * @since 0.1.0
 */
public interface BlockBreakEvent extends PlayerEvent, Cancellable {

    /**
     * {@return the world the block belongs to, that is the world of the player}
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
     * {@return the state of the block being broken}
     *
     * @since 0.1.0
     */
    BlockData block();

    /**
     * {@return what the player breaks the block with, {@link ItemStack#EMPTY} for a bare hand}
     *
     * @since 0.1.0
     */
    ItemStack tool();

    /**
     * {@return the stacks about to be dropped, which listeners may add to, remove from or replace}
     *
     * <p>They are rolled from the loot table of the block with {@link #tool()}, so the list starts
     * empty in creative mode, with the {@code block_drops} game rule off, or when the block needs a
     * tool the player does not hold. What listeners leave in it is dropped
     * whatever the game mode and the game rule.</p>
     *
     * <p>A block taking part in farming starts with an empty list: its harvest is worked out once
     * the event is over by {@link FarmingWorld#harvest}, which fires a {@link HarvestEvent}. What
     * listeners add here drops along with it.</p>
     *
     * @since 0.1.0
     */
    List<ItemStack> drops();
}
