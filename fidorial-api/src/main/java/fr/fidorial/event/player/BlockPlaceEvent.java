package fr.fidorial.event.player;

import fr.fidorial.annotation.ThreadContract;
import fr.fidorial.entity.Player;
import fr.fidorial.event.Cancellable;
import fr.fidorial.world.BlockPos;

public final class BlockPlaceEvent implements PlayerEvent, Cancellable {

    private final Player player;
    private final BlockPos position;
    private final int stateId;
    private boolean cancelled;

    public BlockPlaceEvent(Player player, BlockPos position, int stateId) {
        this.player = player;
        this.position = position;
        this.stateId = stateId;
    }

    @Override
    @ThreadContract(value = "get -> any; modify -> any", transitive = true)
    public Player player() {
        return player;
    }

    @ThreadContract("get -> any")
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
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
