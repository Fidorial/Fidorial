package fr.euphyllia.fidorial.server.events.player;

import com.google.common.base.Preconditions;
import fr.fidorial.entity.Player;
import fr.fidorial.event.player.PlayerOpenEnderChestEvent;
import fr.fidorial.inventory.EnderChestInventory;
import fr.fidorial.world.BlockPos;
import fr.fidorial.world.World;

public final class PlayerOpenEnderChestEventImpl implements PlayerOpenEnderChestEvent {

    private final Player player;
    private final BlockPos position;
    private final EnderChestInventory enderChest;
    private boolean cancelled;

    public PlayerOpenEnderChestEventImpl(final Player player, final BlockPos position, final EnderChestInventory enderChest) {
        Preconditions.checkArgument(player != null, "The player opening an ender chest must not be null");
        Preconditions.checkArgument(position != null, "The position of the ender chest must not be null");
        Preconditions.checkArgument(enderChest != null, "The ender chest inventory must not be null");
        this.player = player;
        this.position = position;
        this.enderChest = enderChest;
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
    public EnderChestInventory enderChest() {
        return enderChest;
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
