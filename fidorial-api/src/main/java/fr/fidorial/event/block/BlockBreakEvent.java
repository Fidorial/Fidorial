package fr.fidorial.event.block;

import com.google.common.base.Preconditions;
import fr.fidorial.entity.Player;
import fr.fidorial.event.Cancellable;
import fr.fidorial.event.player.PlayerEvent;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.world.World;


/**
 * Fired when a player breaks a block, before the block is removed from the world.
 *
 * <p>Cancelling the event keeps the block in place and resyncs the position to the client.</p>
 *
 * @since 0.1.0
 */
public final class BlockBreakEvent implements PlayerEvent, Cancellable {

    private final Player player;
    private final BlockPosition position;
    private boolean cancelled;

    /**
     * Creates an event.
     *
     * @param player   the player breaking the block
     * @param position the position of the block, in the player's world
     * @since 0.1.0
     */
    public BlockBreakEvent(final Player player, final BlockPosition position) {
        Preconditions.checkArgument(player != null, "The player of a block break event must not be null");
        Preconditions.checkArgument(position != null, "The position of a block break event must not be null");
        this.player = player;
        this.position = position;
    }

    @Override
    public Player player() {
        return player;
    }

    /**
     * {@return the world the block belongs to, that is the world of the player}
     *
     * @since 0.1.0
     */
    public World world() {
        return player.world();
    }

    /**
     * {@return the position of the block}
     *
     * @since 0.1.0
     */
    public BlockPosition position() {
        return position;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }
}
