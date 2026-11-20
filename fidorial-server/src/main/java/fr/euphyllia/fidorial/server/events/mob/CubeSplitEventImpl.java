package fr.euphyllia.fidorial.server.events.mob;

import com.google.common.base.Preconditions;
import fr.fidorial.entity.mob.monster.CubeMob;
import fr.fidorial.event.mob.CubeSplitEvent;

public final class CubeSplitEventImpl implements CubeSplitEvent {

    private final CubeMob mob;
    private final int childSize;
    private int count;
    private boolean cancelled;

    public CubeSplitEventImpl(final CubeMob mob, final int count, final int childSize) {
        Preconditions.checkArgument(mob != null, "The cube must not be null");
        this.mob = mob;
        this.count = count;
        this.childSize = childSize;
    }

    @Override
    public CubeMob mob() {
        return mob;
    }

    @Override
    public int count() {
        return count;
    }

    @Override
    public void setCount(final int count) {
        Preconditions.checkArgument(count >= 0, "The count must not be negative, got %s", count);
        this.count = count;
    }

    @Override
    public int childSize() {
        return childSize;
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
