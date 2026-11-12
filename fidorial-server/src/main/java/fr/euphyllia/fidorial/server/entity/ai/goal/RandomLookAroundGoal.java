package fr.euphyllia.fidorial.server.entity.ai.goal;

import fr.euphyllia.fidorial.server.entity.mob.AbstractPathfinderMob;
import fr.fidorial.entity.ai.Goal;
import fr.fidorial.math.Location;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Makes an idle mob look in a random direction for a second or two, as vanilla's
 * {@code RandomLookAroundGoal}.
 */
public final class RandomLookAroundGoal implements Goal {

    /**
     * Vanilla rolls 2% every other tick.
     */
    private static final float START_CHANCE = 0.01f;
    private static final int MIN_LOOK_TICKS = 20;
    private static final int EXTRA_LOOK_TICKS = 20;
    private static final Set<Flag> FLAGS = Set.of(Flag.MOVE, Flag.LOOK);

    private final AbstractPathfinderMob mob;
    private final int priority;
    private double relativeX;
    private double relativeZ;
    private int lookTicks;

    public RandomLookAroundGoal(final AbstractPathfinderMob mob, final int priority) {
        this.mob = mob;
        this.priority = priority;
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
        return ThreadLocalRandom.current().nextFloat() < START_CHANCE;
    }

    @Override
    public boolean shouldContinue() {
        return lookTicks >= 0;
    }

    @Override
    public void start() {
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final double angle = Math.PI * 2.0 * random.nextDouble();
        relativeX = Math.cos(angle);
        relativeZ = Math.sin(angle);
        lookTicks = MIN_LOOK_TICKS + random.nextInt(EXTRA_LOOK_TICKS);
    }

    @Override
    public void tick() {
        lookTicks--;
        final Location at = mob.location();
        mob.setLookTarget(at.x() + relativeX, at.y() + mob.eyeHeight(), at.z() + relativeZ);
    }
}
