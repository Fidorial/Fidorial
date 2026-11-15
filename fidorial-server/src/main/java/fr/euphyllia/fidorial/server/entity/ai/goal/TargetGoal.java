package fr.euphyllia.fidorial.server.entity.ai.goal;

import fr.euphyllia.fidorial.server.entity.AbstractLivingEntity;
import fr.euphyllia.fidorial.server.entity.ai.Targeting;
import fr.euphyllia.fidorial.server.entity.mob.AbstractPathfinderMob;
import fr.fidorial.entity.ai.Goal;
import org.jspecify.annotations.Nullable;

import java.util.Set;

/**
 * The base of the goals that pick whom a mob goes after. Such a goal only holds the {@link Flag#TARGET target}
 * of the mob, so it runs beside the goals that move it; it gives the target up once it may no longer be
 * attacked, went out of the {@linkplain AbstractPathfinderMob#followRange() follow range}, or stayed
 * out of sight for too long.
 */
public abstract class TargetGoal implements Goal {

    private static final Set<Flag> FLAGS = Set.of(Flag.TARGET);
    private static final int DEFAULT_UNSEEN_MEMORY_TICKS = 60;

    protected final AbstractPathfinderMob mob;
    private final int priority;
    private final boolean mustSee;

    /**
     * The entity this goal made the mob go after.
     */
    protected @Nullable AbstractLivingEntity targetMob;

    /**
     * For how many ticks the mob keeps after a target it does not see any more.
     */
    protected int unseenMemoryTicks = DEFAULT_UNSEEN_MEMORY_TICKS;
    private int unseenTicks;

    /**
     * @param mustSee {@code true} if the mob forgets a target it has not seen for a while
     */
    protected TargetGoal(final AbstractPathfinderMob mob, final int priority, final boolean mustSee) {
        this.mob = mob;
        this.priority = priority;
        this.mustSee = mustSee;
    }

    @Override
    public final int priority() {
        return priority;
    }

    @Override
    public final Set<Flag> flags() {
        return FLAGS;
    }

    @Override
    public boolean shouldContinue() {
        AbstractLivingEntity target = mob.target();
        if (target == null) {
            target = targetMob;
        }
        if (target == null || !Targeting.canAttack(mob, target)) {
            return false;
        }
        final double range = mob.followRange();
        if (mob.distanceSqTo(target) > range * range) {
            return false;
        }
        if (mustSee) {
            if (mob.hasLineOfSightTo(target)) {
                unseenTicks = 0;
            } else if (++unseenTicks > unseenMemoryTicks) {
                return false;
            }
        }
        mob.setTarget(target);
        return true;
    }

    @Override
    public void start() {
        unseenTicks = 0;
    }

    @Override
    public void stop() {
        mob.setTarget(null);
        targetMob = null;
    }
}
