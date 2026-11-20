package fr.euphyllia.fidorial.server.events.mob;

import com.google.common.base.Preconditions;
import fr.fidorial.entity.LivingEntity;
import fr.fidorial.entity.mob.Mob;
import fr.fidorial.event.mob.MobTargetEvent;
import org.jspecify.annotations.Nullable;

public final class MobTargetEventImpl implements MobTargetEvent {

    private final Mob mob;
    private final @Nullable LivingEntity previousTarget;
    private @Nullable LivingEntity target;
    private boolean cancelled;

    public MobTargetEventImpl(final Mob mob, final @Nullable LivingEntity previousTarget, final LivingEntity target) {
        Preconditions.checkArgument(mob != null, "The mob must not be null");
        Preconditions.checkArgument(target != null, "The new target must not be null");
        this.mob = mob;
        this.previousTarget = previousTarget;
        this.target = target;
    }

    @Override
    public Mob mob() {
        return mob;
    }

    @Override
    public @Nullable LivingEntity previousTarget() {
        return previousTarget;
    }

    @Override
    public @Nullable LivingEntity target() {
        return target;
    }

    @Override
    public void setTarget(final @Nullable LivingEntity target) {
        this.target = target;
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
