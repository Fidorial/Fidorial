package fr.euphyllia.fidorial.server.events.block;

import com.google.common.base.Preconditions;
import fr.fidorial.entity.Player;
import fr.fidorial.event.block.BlockPlaceEvent;
import fr.fidorial.world.BlockPos;
import fr.fidorial.world.World;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.Blocks;
import org.jspecify.annotations.Nullable;

public final class BlockPlaceEventImpl implements BlockPlaceEvent {

    private final Player player;
    private final BlockPos position;
    private final int stateId;
    private boolean cancelled;

    public BlockPlaceEventImpl(final Player player, final BlockPos position, final int stateId) {
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

    @Override
    public World world() {
        return player.world();
    }

    @Override
    public BlockPos position() {
        return position;
    }

    @Override
    public int stateId() {
        return stateId;
    }

    @Override
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
