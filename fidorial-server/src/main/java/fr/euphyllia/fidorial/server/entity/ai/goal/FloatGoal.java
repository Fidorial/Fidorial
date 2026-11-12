package fr.euphyllia.fidorial.server.entity.ai.goal;

import fr.euphyllia.fidorial.server.entity.mob.AbstractPathfinderMob;
import fr.fidorial.entity.ai.Goal;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Keeps a mob afloat, as vanilla's {@code FloatGoal}: while it is deep enough in water to swim, it
 * swims up four ticks out of five. Only jumping is needed, so the mob keeps walking meanwhile.
 */
public final class FloatGoal implements Goal {

    private static final float SWIM_UP_CHANCE = 0.8f;
    private static final Set<Flag> FLAGS = Set.of(Flag.JUMP);

    private final AbstractPathfinderMob mob;
    private final int priority;

    public FloatGoal(final AbstractPathfinderMob mob, final int priority) {
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
        return mob.isDeepEnoughToSwim();
    }

    @Override
    public boolean shouldContinue() {
        return canStart();
    }

    @Override
    public void tick() {
        if (ThreadLocalRandom.current().nextFloat() < SWIM_UP_CHANCE) {
            mob.requestJump();
        }
    }
}
