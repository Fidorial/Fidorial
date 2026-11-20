package fr.euphyllia.fidorial.server.events.mob;

import com.google.common.base.Preconditions;
import fr.fidorial.entity.mob.Mob;
import fr.fidorial.event.mob.MobExplodeEvent;

public final class MobExplodeEventImpl implements MobExplodeEvent {

    private final Mob mob;
    private float power;
    private boolean breaksBlocks;
    private boolean cancelled;

    public MobExplodeEventImpl(final Mob mob, final float power, final boolean breaksBlocks) {
        Preconditions.checkArgument(mob != null, "The mob must not be null");
        this.mob = mob;
        this.power = power;
        this.breaksBlocks = breaksBlocks;
    }

    @Override
    public Mob mob() {
        return mob;
    }

    @Override
    public float power() {
        return power;
    }

    @Override
    public void setPower(final float power) {
        this.power = power;
    }

    @Override
    public boolean breaksBlocks() {
        return breaksBlocks;
    }

    @Override
    public void setBreaksBlocks(final boolean breaksBlocks) {
        this.breaksBlocks = breaksBlocks;
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
