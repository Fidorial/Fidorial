package fr.euphyllia.fidorial.server.entity.ai.goal;

import fr.euphyllia.fidorial.server.entity.AbstractLivingEntity;
import fr.euphyllia.fidorial.server.entity.mob.AbstractPathfinderMob;
import fr.fidorial.entity.ai.Goal;
import org.jspecify.annotations.Nullable;

import java.util.Set;
import java.util.function.IntSupplier;

/**
 * Makes a mob shoot at its target: it walks towards it, and shoots each time its wait is over and
 * it sees the target. The further the target, the longer the wait, from the shortest interval at
 * point-blank range to the longest at the edge of the attack radius, and the stronger the shot.
 *
 * <p>Once the target is within the attack radius and has been in sight for a quarter of a second,
 * the mob stops walking, unless {@link #stopWhenInRange()} says otherwise.</p>
 */
public class RangedAttackGoal implements Goal {

    private static final Set<Flag> FLAGS = Set.of(Flag.MOVE, Flag.LOOK);
    private static final int SEEN_TICKS_BEFORE_STOPPING = 5;
    private static final float LOOK_SPEED = 30.0f;
    private static final float MIN_POWER = 0.1f;

    protected final AbstractPathfinderMob mob;
    private final int priority;
    private final double speedModifier;
    private final IntSupplier attackIntervalMin;
    private final IntSupplier attackIntervalMax;
    private final double attackRadius;
    private final Shooter shooter;

    private @Nullable AbstractLivingEntity target;
    private int attackTime = -1;
    private int seeTime;

    /**
     * @param speedModifier     what the movement speed of the mob is multiplied by on its way to the
     *                          target
     * @param attackIntervalMin how many ticks the mob waits before it shoots at a target next to it
     * @param attackIntervalMax how many ticks the mob waits before it shoots at a target at the edge
     *                          of the attack radius
     * @param attackRadius      how close the mob gets to shoot, in blocks
     * @param shooter           what shoots
     */
    public RangedAttackGoal(final AbstractPathfinderMob mob, final int priority, final double speedModifier,
                            final IntSupplier attackIntervalMin, final IntSupplier attackIntervalMax,
                            final double attackRadius, final Shooter shooter) {
        this.mob = mob;
        this.priority = priority;
        this.speedModifier = speedModifier;
        this.attackIntervalMin = attackIntervalMin;
        this.attackIntervalMax = attackIntervalMax;
        this.attackRadius = attackRadius;
        this.shooter = shooter;
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
    public boolean canStart() {
        final AbstractLivingEntity current = mob.target();
        if (current == null || current.isDead() || current.isRemoved()) {
            return false;
        }
        target = current;
        return true;
    }

    @Override
    public boolean shouldContinue() {
        if (canStart()) {
            return true;
        }
        final AbstractLivingEntity last = target;
        return last != null && !last.isDead() && !last.isRemoved() && mob.navigation().isNavigating();
    }

    @Override
    public void stop() {
        target = null;
        seeTime = 0;
        attackTime = -1;
    }

    @Override
    public void tick() {
        final AbstractLivingEntity aimed = target;
        if (aimed == null) {
            return;
        }
        final double distanceSq = mob.distanceSqTo(aimed);
        final boolean seen = mob.hasLineOfSightTo(aimed);
        seeTime = seen ? seeTime + 1 : 0;

        if (distanceSq <= attackRadius * attackRadius && seeTime >= SEEN_TICKS_BEFORE_STOPPING && stopWhenInRange()) {
            mob.navigation().stop();
        } else {
            mob.navigation().moveTo(mob.location(), aimed.location(), mob.movementSpeed() * speedModifier);
        }
        mob.setLookTarget(aimed, LOOK_SPEED, LOOK_SPEED);

        final double reach = Math.sqrt(distanceSq) / attackRadius;
        final int shortest = attackIntervalMin.getAsInt();
        final int longest = attackIntervalMax.getAsInt();
        if (--attackTime == 0) {
            if (!seen) {
                return;
            }
            shooter.shoot(aimed, (float) Math.clamp(reach, MIN_POWER, 1.0));
            attackTime = (int) Math.floor((float) reach * (longest - shortest) + shortest);
        } else if (attackTime < 0) {
            attackTime = (int) Math.floor(shortest + reach * (longest - shortest));
            return;
        }
    }

    /**
     * {@return {@code true} if the mob stops walking once its target is in range and in sight}
     */
    protected boolean stopWhenInRange() {
        return true;
    }

    @FunctionalInterface
    public interface Shooter {
        /**
         * Shoots at a target.
         *
         * @param power how far the target is, from {@code 0.1} next to the mob to {@code 1} at the
         *              edge of the attack radius or beyond
         */
        void shoot(AbstractLivingEntity target, float power);
    }
}
