package fr.fidorial.event.block;

import com.google.common.base.Preconditions;
import fr.fidorial.entity.Player;
import fr.fidorial.event.Cancellable;
import fr.fidorial.event.player.PlayerEvent;
import fr.fidorial.world.BlockPos;

public final class BlockBreakEvent implements PlayerEvent, Cancellable {

    private final Player player;
    private final BlockPos position;
    private boolean cancelled;

    public BlockBreakEvent(final Player player, final BlockPos position) {
        this.player = Preconditions.checkNotNull(player, "The player of a block break event must not be null");
        this.position = Preconditions.checkNotNull(position, "The position of a block break event must not be null");
    }

    @Override
    public Player player() {
        return player;
    }

    public BlockPos position() {
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
