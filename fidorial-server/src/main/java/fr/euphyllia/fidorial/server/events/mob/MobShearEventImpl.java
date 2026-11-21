package fr.euphyllia.fidorial.server.events.mob;

import com.google.common.base.Preconditions;
import fr.fidorial.entity.mob.Shearable;
import fr.fidorial.entity.player.Player;
import fr.fidorial.event.mob.MobShearEvent;
import fr.fidorial.item.ItemStack;

public final class MobShearEventImpl implements MobShearEvent {

    private final Shearable mob;
    private final Player player;
    private final ItemStack shears;
    private boolean cancelled;

    public MobShearEventImpl(final Shearable mob, final Player player, final ItemStack shears) {
        Preconditions.checkArgument(mob != null, "The mob must not be null");
        Preconditions.checkArgument(player != null, "The player must not be null");
        Preconditions.checkArgument(shears != null, "The shears must not be null");
        this.mob = mob;
        this.player = player;
        this.shears = shears;
    }

    @Override
    public Shearable mob() {
        return mob;
    }

    @Override
    public Player player() {
        return player;
    }

    @Override
    public ItemStack shears() {
        return shears;
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
