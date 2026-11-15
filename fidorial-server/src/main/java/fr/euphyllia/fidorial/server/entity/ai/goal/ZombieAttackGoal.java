package fr.euphyllia.fidorial.server.entity.ai.goal;

import fr.euphyllia.fidorial.server.entity.mob.AbstractPathfinderMob;

/**
 * The attack of a zombie : a melee attack during which the mob raises its arms,
 * except for the half second that follows each blow.
 */
public class ZombieAttackGoal extends MeleeAttackGoal {

    private static final int RAISE_ARMS_AFTER_TICKS = 5;

    private int raiseArmTicks;

    public ZombieAttackGoal(final AbstractPathfinderMob mob, final int priority, final double speedModifier,
                            final boolean followsUnseenTarget) {
        super(mob, priority, speedModifier, followsUnseenTarget);
    }

    @Override
    public void start() {
        super.start();
        raiseArmTicks = 0;
    }

    @Override
    public void tick() {
        super.tick();
        raiseArmTicks++;
        mob.setAggressive(raiseArmTicks >= RAISE_ARMS_AFTER_TICKS && ticksUntilNextAttack() < attackInterval() / 2);
    }
}
