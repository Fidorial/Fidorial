package fr.euphyllia.fidorial.server.entity.ai.goal;

import fr.euphyllia.fidorial.server.entity.mob.AbstractPathfinderMob;
import fr.fidorial.entity.ai.Goal;
import fr.fidorial.math.Location;

import java.util.concurrent.ThreadLocalRandom;

public final class RandomStrollGoal implements Goal {

    private static final int RANGE = 8;
    private static final int START_CHANCE = 120;
    private static final int MAX_DURATION_TICKS = 200;

    private final AbstractPathfinderMob mob;
    private final int priority;
    private final double speed;
    private int ticksRunning;

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
    public boolean canStart() {
        return mob.target() == null && ThreadLocalRandom.current().nextInt(START_CHANCE) == 0;
    }

    @Override
    public boolean shouldContinue() {
        return mob.target() == null
                && ticksRunning < MAX_DURATION_TICKS
                && mob.navigation().isNavigating();
    }

    @Override
    public void start() {
        ticksRunning = 0;
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final Location from = mob.location();
        final Location to = from.offset(
                random.nextInt(-RANGE, RANGE + 1), 0,
                random.nextInt(-RANGE, RANGE + 1)
        );
        mob.navigation().moveTo(from, to);
    }

    @Override
    public void stop() {
        mob.navigation().stop();
    }

    @Override
    public void tick() {
        ticksRunning++;
        mob.setMoveSpeed(speed);
    }
}
