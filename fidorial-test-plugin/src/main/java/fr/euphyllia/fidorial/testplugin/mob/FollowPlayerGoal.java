package fr.euphyllia.fidorial.testplugin.mob;

import fr.fidorial.entity.player.Player;
import fr.fidorial.entity.ai.Goal;
import fr.fidorial.entity.mob.Mob;
import org.jspecify.annotations.Nullable;

public final class FollowPlayerGoal implements Goal {

    private static final double START_DISTANCE = 3.5;

    private static final double STOP_DISTANCE = 2.0;

    private static final double GIVE_UP_DISTANCE = 20.0;

    private static final int REPATH_INTERVAL = 8;

    private final Mob mob;
    private final int priority;
    private final double speed;

    private int ticks;
    private @Nullable Player followed;

    public FollowPlayerGoal(final Mob mob, final int priority, final double speed) {
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
        final Player nearest = mob.nearestPlayer(GIVE_UP_DISTANCE);
        if (nearest == null || !nearest.isAlive()) {
            return false;
        }
        if (mob.distanceSqTo(nearest) < START_DISTANCE * START_DISTANCE) {
            return false;
        }
        followed = nearest;
        return true;
    }

    @Override
    public boolean shouldContinue() {
        final Player target = followed;
        if (target == null || !target.isAlive() || target.isRemoved() || mob.isDead()) {
            return false;
        }
        final double distanceSq = mob.distanceSqTo(target);
        return distanceSq > STOP_DISTANCE * STOP_DISTANCE
                && distanceSq < GIVE_UP_DISTANCE * GIVE_UP_DISTANCE;
    }

    @Override
    public void start() {
        ticks = 0;
    }

    @Override
    public void tick() {
        final Player target = followed;
        if (target == null) {
            return;
        }

        ticks++;
        mob.lookAt(target);

        if (ticks % REPATH_INTERVAL == 1) {
            mob.navigation().moveTo(mob.location(), target.location());
        }
        mob.setMoveSpeed(speed);
    }

    @Override
    public void stop() {
        followed = null;
        ticks = 0;
        mob.navigation().stop();
        mob.setMoveSpeed(0.0);
    }
}
