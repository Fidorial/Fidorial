package fr.euphyllia.fidorial.server.events.mob;

import com.google.common.base.Preconditions;
import fr.fidorial.entity.mob.Mob;
import fr.fidorial.event.mob.MobTeleportEvent;
import fr.fidorial.math.Location;

public final class MobTeleportEventImpl implements MobTeleportEvent {

    private final Mob mob;
    private final Location from;
    private final Location to;
    private boolean cancelled;

    public MobTeleportEventImpl(final Mob mob, final Location from, final Location to) {
        Preconditions.checkArgument(mob != null, "The mob must not be null");
        Preconditions.checkArgument(from != null && to != null, "The locations must not be null");
        this.mob = mob;
        this.from = from;
        this.to = to;
    }

    @Override
    public Mob mob() {
        return mob;
    }

    @Override
    public Location from() {
        return from;
    }

    @Override
    public Location to() {
        return to;
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
