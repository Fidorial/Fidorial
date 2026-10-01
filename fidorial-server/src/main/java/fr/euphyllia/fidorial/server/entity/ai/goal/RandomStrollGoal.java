package fr.euphyllia.fidorial.server.entity.ai.goal;

import fr.euphyllia.fidorial.server.entity.mob.AbstractPathfinderMob;
import fr.fidorial.entity.ai.Goal;
import fr.fidorial.math.Location;
import fr.fidorial.world.BlockPos;

import java.util.concurrent.ThreadLocalRandom;

public final class RandomStrollGoal implements Goal {

    private static final int RANGE = 8;
    private static final int START_CHANCE = 120;
    private static final int MAX_DURATION_TICKS = 200;

    private final AbstractPathfinderMob mob;
    private final int priority;
    private final double speed;
    private int ticksRunning;

    public RandomStrollGoal(AbstractPathfinderMob mob, int priority, double speed) {
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
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Location from = mob.location();
        int x = (int) Math.floor(from.x()) + random.nextInt(-RANGE, RANGE + 1);
        int z = (int) Math.floor(from.z()) + random.nextInt(-RANGE, RANGE + 1);
        int y = (int) Math.floor(from.y());
        mob.navigation().moveTo(from, new BlockPos(x, y, z));
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
