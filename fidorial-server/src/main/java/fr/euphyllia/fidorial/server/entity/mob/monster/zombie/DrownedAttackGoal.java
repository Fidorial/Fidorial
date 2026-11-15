package fr.euphyllia.fidorial.server.entity.mob.monster.zombie;

import fr.euphyllia.fidorial.server.entity.ai.goal.ZombieAttackGoal;

/**
 * The attack of a drowned: that of a zombie, but only on a target the drowned
 * {@linkplain Drowned#okTarget cares about}, one in the water by day.
 */
final class DrownedAttackGoal extends ZombieAttackGoal {

    private final Drowned drowned;

    DrownedAttackGoal(final Drowned drowned, final int priority, final double speedModifier,
                      final boolean followsUnseenTarget) {
        super(drowned, priority, speedModifier, followsUnseenTarget);
        this.drowned = drowned;
    }

    @Override
    public boolean canStart() {
        return super.canStart() && drowned.okTarget(drowned.target());
    }

    @Override
    public boolean shouldContinue() {
        return super.shouldContinue() && drowned.okTarget(drowned.target());
    }
}
