package fr.fidorial.event.block;

import com.google.common.base.Preconditions;
import fr.fidorial.entity.Player;
import fr.fidorial.event.Cancellable;
import fr.fidorial.event.player.PlayerEvent;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.world.World;
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
public final class BlockPlaceEvent implements PlayerEvent, Cancellable {

    private final Player player;
    private final BlockPosition position;
    private final int stateId;
    private boolean cancelled;

    /**
     * Creates an event.
     *
     * @param player   the player placing the block
     * @param position the position of the block, in the player's world
     * @param stateId  the network identifier of the block state about to be placed
     * @since 0.1.0
     */
    public BlockPlaceEvent(final Player player, final BlockPosition position, final int stateId) {
        Preconditions.checkArgument(player != null, "The player of a block place event must not be null");
        Preconditions.checkArgument(position != null, "The position of a block place event must not be null");
        this.player = player;
        this.position = position;
        this.stateId = stateId;
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

    /**
     * {@return the network identifier of the block state about to be placed}
     *
     * @see #blockData()
     * @since 0.1.0
     */
    public int stateId() {
        return stateId;
    }

    /**
     * Resolves the block state about to be placed.
     *
     * @return the block state, or {@code null} if {@link #stateId()} is unknown to the
     * {@linkplain Blocks#registry() block registry}
     * @since 0.1.0
     */
    public @Nullable BlockData blockData() {
        return Blocks.registry().fromNetworkId(stateId);
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
