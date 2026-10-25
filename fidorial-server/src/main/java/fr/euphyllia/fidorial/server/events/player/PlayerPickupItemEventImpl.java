package fr.euphyllia.fidorial.server.events.player;

import com.google.common.base.Preconditions;
import fr.fidorial.entity.ItemEntity;
import fr.fidorial.entity.player.Player;
import fr.fidorial.event.player.PlayerPickupItemEvent;
import fr.fidorial.item.ItemStack;

public final class PlayerPickupItemEventImpl implements PlayerPickupItemEvent {

    private final Player player;
    private final ItemEntity item;
    private final ItemStack taken;
    private boolean cancelled;

    public PlayerPickupItemEventImpl(final Player player, final ItemEntity item, final ItemStack taken) {
        Preconditions.checkArgument(player != null, "The player picking up must not be null");
        Preconditions.checkArgument(item != null, "The item entity must not be null");
        Preconditions.checkArgument(taken != null && !taken.isEmpty(), "What is picked up must not be empty");
        this.player = player;
        this.item = item;
        this.taken = taken;
    }

    @Override
    public Player player() {
        return player;
    }

    @Override
    public ItemEntity item() {
        return item;
    }

    @Override
    public ItemStack taken() {
        return taken;
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
