package fr.euphyllia.fidorial.server.events.block;

import com.google.common.base.Preconditions;
import fr.fidorial.entity.Player;
import fr.fidorial.event.block.BlockBreakEvent;
import fr.fidorial.world.BlockPos;
import fr.fidorial.world.World;

public final class BlockBreakEventImpl implements BlockBreakEvent {

    private final Player player;
    private final BlockPos position;
    private boolean cancelled;

    public BlockBreakEventImpl(final Player player, final BlockPos position) {
        Preconditions.checkArgument(player != null, "The player of a block break event must not be null");
        Preconditions.checkArgument(position != null, "The position of a block break event must not be null");
        this.player = player;
        this.position = position;
    }

    @Override
    public Player player() {
        return player;
    }

    @Override
    public World world() {
        return player.world();
    }

    @Override
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
