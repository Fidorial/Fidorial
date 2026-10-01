package fr.fidorial.event.block;

import com.google.common.base.Preconditions;
import fr.fidorial.entity.Player;
import fr.fidorial.event.Cancellable;
import fr.fidorial.event.player.PlayerEvent;
import fr.fidorial.world.BlockPos;

public final class BlockPlaceEvent implements PlayerEvent, Cancellable {

    private final Player player;
    private final BlockPos position;
    private final int stateId;
    private boolean cancelled;

    public BlockPlaceEvent(final Player player, final BlockPos position, final int stateId) {
        this.player = Preconditions.checkNotNull(player, "The player of a block place event must not be null");
        this.position = Preconditions.checkNotNull(position, "The position of a block place event must not be null");
        this.stateId = stateId;
    }

    @Override
    public Player player() {
        return player;
    }

    public BlockPos position() {
        return position;
    }

    public int stateId() {
        return stateId;
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
