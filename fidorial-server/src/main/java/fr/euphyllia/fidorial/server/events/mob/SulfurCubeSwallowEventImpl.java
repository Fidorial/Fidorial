package fr.euphyllia.fidorial.server.events.mob;

import com.google.common.base.Preconditions;
import fr.fidorial.entity.ItemEntity;
import fr.fidorial.entity.mob.monster.SulfurCube;
import fr.fidorial.entity.player.Player;
import fr.fidorial.event.mob.SulfurCubeSwallowEvent;
import fr.fidorial.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class SulfurCubeSwallowEventImpl implements SulfurCubeSwallowEvent {

    private final SulfurCube mob;
    private final ItemStack item;
    private final @Nullable Player giver;
    private final @Nullable ItemEntity pickedUp;
    private boolean cancelled;

    public SulfurCubeSwallowEventImpl(final SulfurCube mob, final ItemStack item, final @Nullable Player giver,
                                      final @Nullable ItemEntity pickedUp) {
        Preconditions.checkArgument(mob != null, "The sulfur cube must not be null");
        Preconditions.checkArgument(item != null && !item.isEmpty(), "The swallowed item must not be empty");
        Preconditions.checkArgument((giver == null) != (pickedUp == null),
                "A swallowed block comes either from a player or from the ground");
        this.mob = mob;
        this.item = item;
        this.giver = giver;
        this.pickedUp = pickedUp;
    }

    @Override
    public SulfurCube mob() {
        return mob;
    }

    @Override
    public ItemStack item() {
        return item;
    }

    @Override
    public @Nullable Player giver() {
        return giver;
    }

    @Override
    public @Nullable ItemEntity pickedUp() {
        return pickedUp;
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
