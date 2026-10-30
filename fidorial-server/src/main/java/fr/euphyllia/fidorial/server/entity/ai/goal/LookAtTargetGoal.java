package fr.euphyllia.fidorial.server.entity.ai.goal;

import fr.euphyllia.fidorial.server.entity.mob.AbstractPathfinderMob;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.fidorial.entity.ai.Goal;

import java.util.concurrent.ThreadLocalRandom;

public final class LookAtTargetGoal implements Goal {

    private final AbstractPathfinderMob mob;
    private final int priority;
    private final double rangeSq;
    private int remainingTicks;

    public LookAtTargetGoal(final AbstractPathfinderMob mob, final int priority, final double range) {
        this.mob = mob;
        this.priority = priority;
        this.rangeSq = range * range;
    }

    @Override
    public int priority() {
        return priority;
    }

    @Override
    public boolean canStart() {
        final ServerPlayer target = mob.target();
        return target != null
                && mob.distanceSqTo(target) <= rangeSq
                && ThreadLocalRandom.current().nextInt(40) == 0;
    }

    @Override
    public boolean shouldContinue() {
        final ServerPlayer target = mob.target();
        return remainingTicks > 0 && target != null && mob.distanceSqTo(target) <= rangeSq;
    }

    @Override
    public void start() {
        remainingTicks = 40 + ThreadLocalRandom.current().nextInt(40);
    }

    @Override
    public void tick() {
        remainingTicks--;
        final ServerPlayer target = mob.target();
        if (target != null) {
            mob.lookAt(target);
        }
    }
}
