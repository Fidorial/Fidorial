package fr.euphyllia.fidorial.server.entity.ai.goal;

import fr.euphyllia.fidorial.server.entity.mob.AbstractPathfinderMob;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.fidorial.entity.ai.Goal;
import fr.fidorial.world.BlockPos;

public final class ChaseTargetGoal implements Goal {

    private final AbstractPathfinderMob mob;
    private final int priority;
    private final double speed;

    public ChaseTargetGoal(final AbstractPathfinderMob mob, final int priority, final double speed) {
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
        return mob.target() != null;
    }

    @Override
    public boolean shouldContinue() {
        return mob.target() != null;
    }

    @Override
    public void stop() {
        mob.navigation().stop();
    }

    @Override
    public void tick() {
        final ServerPlayer target = mob.target();
        if (target == null) {
            return;
        }
        final Location goal = target.location();
        mob.navigation()
                .moveTo(mob.location(), new BlockPos((int) Math.floor(goal.x()), (int) Math.floor(goal.y()), (int)
                        Math.floor(goal.z())));
        mob.setMoveSpeed(speed);

        if (mob.distanceSqTo(target) < 36.0) {
            mob.lookAt(target);
        }
    }
}
