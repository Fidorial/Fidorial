package fr.fidorial.event.block;

import fr.fidorial.event.Cancellable;
import fr.fidorial.event.player.PlayerEvent;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.Blocks;
import org.jspecify.annotations.Nullable;

/**
 * Fired when a player places a block, before the block is written to the world.
 *
 * <p>Cancelling the event leaves the world untouched and resyncs the position to the client.</p>
 *
 * @since 0.1.0
 */
public interface BlockPlaceEvent extends BlockEvent, PlayerEvent, Cancellable {

    /**
     * {@return the network identifier of the block state about to be placed}
     *
     * @see #blockData()
     * @since 0.1.0
     */
    int stateId();

    /**
     * Resolves the block state about to be placed.
     *
     * @return the block state, or {@code null} if {@link #stateId()} is unknown to the
     * {@linkplain Blocks#registry() block registry}
     * @since 0.1.0
     */
    @Nullable BlockData blockData();
}
