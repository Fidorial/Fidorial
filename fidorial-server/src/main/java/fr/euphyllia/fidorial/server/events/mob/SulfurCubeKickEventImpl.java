package fr.euphyllia.fidorial.server.events.mob;

import com.google.common.base.Preconditions;
import fr.fidorial.entity.Entity;
import fr.fidorial.entity.mob.monster.SulfurCube;
import fr.fidorial.event.mob.SulfurCubeKickEvent;
import fr.fidorial.math.FinePosition;

public final class SulfurCubeKickEventImpl implements SulfurCubeKickEvent {

    private final SulfurCube mob;
    private final Entity kicker;
    private final Cause cause;
    private FinePosition push;
    private boolean cancelled;

    public SulfurCubeKickEventImpl(final SulfurCube mob, final Entity kicker, final Cause cause, final FinePosition push) {
        Preconditions.checkArgument(mob != null, "The sulfur cube must not be null");
        Preconditions.checkArgument(kicker != null, "The kicker must not be null");
        Preconditions.checkArgument(cause != null, "The cause must not be null");
        Preconditions.checkArgument(push != null, "The push must not be null");
        this.mob = mob;
        this.kicker = kicker;
        this.cause = cause;
        this.push = push;
    }

    @Override
    public SulfurCube mob() {
        return mob;
    }

    @Override
    public Entity kicker() {
        return kicker;
    }

    @Override
    public Cause cause() {
        return cause;
    }

    @Override
    public FinePosition push() {
        return push;
    }

    @Override
    public void setPush(final FinePosition push) {
        Preconditions.checkArgument(push != null, "The push must not be null");
        this.push = push;
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
