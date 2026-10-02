package fr.euphyllia.fidorial.testplugin.mob;

import fr.fidorial.entity.ai.Goal;
import fr.fidorial.entity.mob.Mob;
import fr.fidorial.math.Location;
import fr.fidorial.world.BlockPos;

import java.util.concurrent.ThreadLocalRandom;

public final class WanderGoal implements Goal {

    private static final int RADIUS = 8;

    private static final int MAX_TICKS = 120;

    private static final int PAUSE_TICKS = 40;

    private final Mob mob;
    private final int priority;
    private final double speed;

    private int ticks;
    private int pause;

    public WanderGoal(final Mob mob, final int priority, final double speed) {
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
        if (pause > 0) {
            pause--;
            return false;
        }
        return !mob.isDead();
    }

    @Override
    public boolean shouldContinue() {
        return ticks < MAX_TICKS && mob.navigation().isNavigating() && !mob.isDead();
    }

    @Override
    public void start() {
        ticks = 0;

        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final Location from = mob.location();
        final BlockPos destination = new BlockPos(
                (int) Math.floor(from.x()) + random.nextInt(-RADIUS, RADIUS + 1),
                (int) Math.floor(from.y()),
                (int) Math.floor(from.z()) + random.nextInt(-RADIUS, RADIUS + 1));

        mob.navigation().moveTo(from, destination);
    }

    @Override
    public void tick() {
        ticks++;
        mob.setMoveSpeed(speed);
    }

    @Override
    public void stop() {
        mob.navigation().stop();
        mob.setMoveSpeed(0.0);
        pause = PAUSE_TICKS;
    }
}
