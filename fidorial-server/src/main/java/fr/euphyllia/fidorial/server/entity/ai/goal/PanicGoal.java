package fr.euphyllia.fidorial.server.entity.ai.goal;

import fr.euphyllia.fidorial.server.entity.mob.AbstractPathfinderMob;
import fr.fidorial.entity.ai.Goal;
import fr.fidorial.world.BlockPos;

import java.util.concurrent.ThreadLocalRandom;


public final class PanicGoal implements Goal {

    private static final int DURATION_TICKS = 100;
    private static final int FLEE_DISTANCE = 12;
    private static final int RETRY_ATTEMPTS = 8;

    private final AbstractPathfinderMob mob;
    private final int priority;
    private final double speed;

    private int remainingTicks;

    public PanicGoal(final AbstractPathfinderMob mob, final int priority, final double speed) {
        this.mob = mob;
        this.priority = priority;
        this.speed = speed;
    }

    public void panic() {
        remainingTicks = DURATION_TICKS;
    }

    public boolean isPanicking() {
        return remainingTicks > 0;
    }

    @Override
    public int priority() {
        return priority;
    }

    @Override
    public boolean canStart() {
        return remainingTicks > 0;
    }

    @Override
    public boolean shouldContinue() {
        return remainingTicks > 0 && mob.navigation().isNavigating();
    }

    @Override
    public void start() {
        pickEscapePoint();
    }

    @Override
    public void stop() {
        mob.navigation().stop();
    }

    @Override
    public void tick() {
        remainingTicks--;
        mob.setMoveSpeed(speed);
        if (!mob.navigation().isNavigating()) {
            pickEscapePoint();
        }
    }


    private void pickEscapePoint() {
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final Location from = mob.location();
        final int originX = (int) Math.floor(from.x());
        final int originY = (int) Math.floor(from.y());
        final int originZ = (int) Math.floor(from.z());

        double awayX = random.nextDouble() - 0.5;
        double awayZ = random.nextDouble() - 0.5;
        final var threat = mob.target();
        if (threat != null) {
            final Location threatPos = threat.location();
            awayX = from.x() - threatPos.x();
            awayZ = from.z() - threatPos.z();
        }
        final double length = Math.sqrt(awayX * awayX + awayZ * awayZ);
        if (length < 1.0E-4) {
            awayX = 1.0;
            awayZ = 0.0;
        } else {
            awayX /= length;
            awayZ /= length;
        }

        for (int attempt = 0; attempt < RETRY_ATTEMPTS; attempt++) {
            final double spread = (random.nextDouble() - 0.5) * 1.5;
            final double dirX = awayX + spread;
            final double dirZ = awayZ - spread;
            final int targetX = originX + (int) Math.round(dirX * FLEE_DISTANCE);
            final int targetZ = originZ + (int) Math.round(dirZ * FLEE_DISTANCE);
            if (targetX == originX && targetZ == originZ) {
                continue;
            }
            mob.navigation().moveTo(from, new BlockPos(targetX, originY, targetZ));
            return;
        }
    }

}
