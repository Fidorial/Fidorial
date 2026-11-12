package fr.euphyllia.fidorial.server.entity.ai.goal;

import fr.euphyllia.fidorial.server.entity.ai.RandomPositions;
import fr.euphyllia.fidorial.server.entity.mob.AbstractPathfinderMob;
import fr.fidorial.entity.ai.Goal;
import fr.fidorial.math.Position;
import org.jspecify.annotations.Nullable;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Makes a mob wander, as vanilla's {@code WaterAvoidingRandomStrollGoal}: once every
 * {@value #INTERVAL} ticks on average it walks to a random spot on land, up to 10 blocks away, or
 * 15 to get out of the water it is in. A mob no player has been near for a while stays put.
 */
public final class RandomStrollGoal implements Goal {

    private static final int INTERVAL = 120;
    private static final int IDLE_LIMIT_TICKS = 100;
    private static final int HORIZONTAL = 10;
    private static final int HORIZONTAL_FROM_WATER = 15;
    private static final int VERTICAL = 7;
    private static final Set<Flag> FLAGS = Set.of(Flag.MOVE);

    private final AbstractPathfinderMob mob;
    private final int priority;
    private final double speed;
    private @Nullable Position destination;

    /**
     * @param speed the movement speed of the mob times vanilla's speed modifier
     */
    public RandomStrollGoal(final AbstractPathfinderMob mob, final int priority, final double speed) {
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
        return FLAGS;
    }

    @Override
    public boolean canStart() {
        if (mob.noActionTime() >= IDLE_LIMIT_TICKS) {
            return false;
        }
        if (ThreadLocalRandom.current().nextInt(INTERVAL) != 0) {
            return false;
        }
        destination = mob.isInWater()
                ? RandomPositions.land(mob, HORIZONTAL_FROM_WATER, VERTICAL)
                : RandomPositions.land(mob, HORIZONTAL, VERTICAL);
        return destination != null;
    }

    @Override
    public boolean shouldContinue() {
        return mob.navigation().isNavigating();
    }

    @Override
    public void start() {
        final Position target = destination;
        if (target != null) {
            mob.navigation().moveTo(mob.location(), target, speed);
        }
    }

    @Override
    public void stop() {
        destination = null;
        mob.navigation().stop();
    }
}
