package fr.euphyllia.fidorial.server.entity.ai.goal;

import fr.euphyllia.fidorial.server.entity.AbstractLivingEntity;
import fr.euphyllia.fidorial.server.entity.mob.AbstractPathfinderMob;
import fr.fidorial.entity.ai.Goal;
import fr.fidorial.math.Location;
import org.jspecify.annotations.Nullable;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Makes a mob on the ground leap at its target when it is between 2 and 4 blocks away: it jumps
 * towards the target at 0.4 blocks a tick across, keeping a fifth of the speed it had, and the goal
 * lasts until it lands.
 */
public final class LeapAtTargetGoal implements Goal {

    private static final Set<Flag> FLAGS = Set.of(Flag.MOVE, Flag.JUMP);
    private static final double MIN_DISTANCE_SQ = 4.0;
    private static final double MAX_DISTANCE_SQ = 16.0;
    private static final double LEAP_SPEED = 0.4;
    private static final double KEPT_SPEED = 0.2;
    /**
     * The mob leaps one tick in six at most.
     */
    private static final int LEAP_CHANCE = 6;

    private final AbstractPathfinderMob mob;
    private final int priority;
    private final double leapHeight;
    private @Nullable AbstractLivingEntity target;

    /**
     * @param leapHeight the vertical speed the mob leaps with
     */
    public LeapAtTargetGoal(final AbstractPathfinderMob mob, final int priority, final double leapHeight) {
        this.mob = mob;
        this.priority = priority;
        this.leapHeight = leapHeight;
    }

    @Override
    public int priority() {
        return priority;
    }

    @Override
    public Set<Flag> flags() {
        return FLAGS;
    }

    @Override
    public boolean canStart() {
        final AbstractLivingEntity current = mob.target();
        if (current == null) {
            return false;
        }
        final double distanceSq = mob.distanceSqTo(current);
        if (distanceSq < MIN_DISTANCE_SQ || distanceSq > MAX_DISTANCE_SQ || !mob.onGround()) {
            return false;
        }
        if (ThreadLocalRandom.current().nextInt(LEAP_CHANCE) != 0) {
            return false;
        }
        target = current;
        return true;
    }

    @Override
    public boolean shouldContinue() {
        return !mob.onGround();
    }

    @Override
    public void start() {
        final AbstractLivingEntity aimed = target;
        if (aimed == null) {
            return;
        }
        final Location self = mob.location();
        final Location other = aimed.location();
        double velocityX = other.x() - self.x();
        double velocityZ = other.z() - self.z();
        final double length = Math.sqrt(velocityX * velocityX + velocityZ * velocityZ);
        if (length * length > 1.0E-7) {
            velocityX = velocityX / length * LEAP_SPEED + mob.velocityX() * KEPT_SPEED;
            velocityZ = velocityZ / length * LEAP_SPEED + mob.velocityZ() * KEPT_SPEED;
        }
        mob.setVelocity(velocityX, leapHeight, velocityZ);
    }

    @Override
    public void stop() {
        target = null;
    }
}