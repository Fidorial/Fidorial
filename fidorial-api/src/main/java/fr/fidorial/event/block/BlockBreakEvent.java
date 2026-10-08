package fr.fidorial.event.block;

import fr.fidorial.event.Cancellable;
import fr.fidorial.event.player.PlayerEvent;

/**
 * Fired when a player breaks a block, before the block is removed from the world.
 *
 * <p>Cancelling the event keeps the block in place and resyncs the position to the client.</p>
 *
 * @since 0.1.0
 */
public interface BlockBreakEvent extends BlockEvent, PlayerEvent, Cancellable {
}
