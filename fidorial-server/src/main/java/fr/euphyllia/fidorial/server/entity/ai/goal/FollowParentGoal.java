package fr.euphyllia.fidorial.server.entity.ai.goal;

import fr.euphyllia.fidorial.server.entity.AbstractEntity;
import fr.euphyllia.fidorial.server.entity.mob.AbstractAgeableMob;
import fr.fidorial.entity.ai.Goal;
import fr.fidorial.math.Location;
import org.jspecify.annotations.Nullable;

import java.util.Set;

/**
 * Makes a baby follow the closest adult of its kind, as vanilla's {@code FollowParentGoal}: an adult
 * within 8 blocks around and 4 above or below, followed while it is between 3 and 16 blocks away.
 *
 * <p>As in Vanilla it needs no control, so it runs along the other goals of the baby.</p>
 */
public final class FollowParentGoal implements Goal {

    private static final double HORIZONTAL_SCAN_RANGE = 8.0;
    private static final double VERTICAL_SCAN_RANGE = 4.0;
    private static final double MIN_DISTANCE_SQ = 3.0 * 3.0;
    private static final double MAX_DISTANCE_SQ = 16.0 * 16.0;
    private static final int REPATH_INTERVAL_TICKS = 10;

    private final AbstractAgeableMob mob;
    private final int priority;
    private final double speed;

    private @Nullable AbstractAgeableMob parent;
    private int timeToRecalcPath;

    /**
     * @param speed the movement speed of the mob times vanilla's speed modifier
     */
    public FollowParentGoal(final AbstractAgeableMob mob, final int priority, final double speed) {
        this.mob = mob;
        this.priority = priority;
        this.speed = speed;
    }

    @Override
    public int priority() {
        return priority;
    }

    @Override
    public Set<Flag> flags() {
        return Set.of();
    }

    @Override
    public boolean canStart() {
        if (mob.age() >= 0) {
            return false;
        }

        final AbstractAgeableMob candidate = findNearestAdult();
        if (candidate == null || mob.distanceSqTo(candidate) < MIN_DISTANCE_SQ) {
            return false;
        }

        parent = candidate;
        return true;
    }

    @Override
    public boolean shouldContinue() {
        final AbstractAgeableMob current = parent;
        if (mob.age() >= 0 || current == null || current.isRemoved() || current.isDead()) {
            return false;
        }
        final double distSq = mob.distanceSqTo(current);

        return distSq >= MIN_DISTANCE_SQ && distSq <= MAX_DISTANCE_SQ;
    }

    @Override
    public void start() {
        timeToRecalcPath = 0;
    }

    @Override
    public void stop() {
        parent = null;
    }

    @Override
    public void tick() {
        final AbstractAgeableMob current = parent;
        if (current != null && --timeToRecalcPath <= 0) {
            timeToRecalcPath = REPATH_INTERVAL_TICKS;
            mob.navigation().moveTo(mob.location(), current.location(), speed);
        }
    }

    private @Nullable AbstractAgeableMob findNearestAdult() {
        final Location self = mob.location();
        final double reach = HORIZONTAL_SCAN_RANGE + mob.width() * 0.5;
        final AbstractAgeableMob[] best = new AbstractAgeableMob[1];
        final double[] bestDistSq = {Double.MAX_VALUE};

        mob.serverWorld().entityManager().forEachNear(self.chunk(), reach, (final AbstractEntity entity) -> {
            if (!(entity instanceof final AbstractAgeableMob other)
                    || other == mob
                    || other.getClass() != mob.getClass()
                    || other.age() < 0
                    || other.isRemoved()
                    || other.isDead()) {
                return;
            }
            final Location at = other.location();
            final double otherHalf = other.width() * 0.5;
            if (Math.abs(at.x() - self.x()) > reach + otherHalf
                    || Math.abs(at.z() - self.z()) > reach + otherHalf
                    || at.y() > self.y() + mob.height() + VERTICAL_SCAN_RANGE
                    || at.y() + other.height() < self.y() - VERTICAL_SCAN_RANGE) {
                return;
            }
            final double distSq = mob.distanceSqTo(other);
            if (distSq <= bestDistSq[0]) {
                bestDistSq[0] = distSq;
                best[0] = other;
            }
        });

        return best[0];
    }
}
