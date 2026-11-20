package fr.euphyllia.fidorial.server.events.mob;

import com.google.common.base.Preconditions;
import fr.fidorial.entity.mob.Mob;
import fr.fidorial.event.mob.MobConvertEvent;

public final class MobConvertEventImpl implements MobConvertEvent {

    private final Mob mob;
    private final Mob converted;
    private boolean cancelled;

    public MobConvertEventImpl(final Mob mob, final Mob converted) {
        Preconditions.checkArgument(mob != null, "The mob must not be null");
        Preconditions.checkArgument(converted != null, "The converted mob must not be null");
        this.mob = mob;
        this.converted = converted;
    }

    @Override
    public Mob mob() {
        return mob;
    }

    @Override
    public Mob converted() {
        return converted;
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
