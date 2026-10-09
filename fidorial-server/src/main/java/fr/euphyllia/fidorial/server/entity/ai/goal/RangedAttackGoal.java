package fr.euphyllia.fidorial.server.entity.ai.goal;

import fr.euphyllia.fidorial.server.entity.mob.AbstractPathfinderMob;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.fidorial.entity.ai.Goal;

import java.util.function.BooleanSupplier;

/**
 * Stands still and shoots at the target while it is far enough, leaving closer targets to a melee goal.
 *
 * <p>Give it a higher priority (a lower number) than the melee goal: it then takes over as soon as the
 * target is within range and hands back when the target comes closer or the mob can no longer shoot.</p>
 */
public final class RangedAttackGoal implements Goal {

    /** A target has to come this much closer than the minimum range before the mob stops shooting. */
    private static final double MIN_RANGE_HYSTERESIS = 0.75;

    private final AbstractPathfinderMob mob;
    private final int priority;
    private final double minRangeSq;
    private final double maxRangeSq;
    private final int cooldownTicks;
    private final BooleanSupplier canShoot;
    private final Shooter shooter;

    private int cooldown;

    public RangedAttackGoal(final AbstractPathfinderMob mob, final int priority,
                            final double minRange, final double maxRange, final int cooldownTicks,
                            final BooleanSupplier canShoot, final Shooter shooter) {
        this.mob = mob;
        this.priority = priority;
        this.minRangeSq = minRange * minRange;
        this.maxRangeSq = maxRange * maxRange;
        this.cooldownTicks = cooldownTicks;
        this.canShoot = canShoot;
        this.shooter = shooter;
    }

    @Override
    public int priority() {
        return priority;
    }

    @Override
    public boolean canStart() {
        final ServerPlayer target = mob.target();
        if (target == null || target.isDead() || !canShoot.getAsBoolean()) {
            return false;
        }
        final double distanceSq = mob.distanceSqTo(target);
        return distanceSq >= minRangeSq && distanceSq <= maxRangeSq && mob.hasLineOfSightTo(target);
    }

    @Override
    public boolean shouldContinue() {
        final ServerPlayer target = mob.target();
        if (target == null || target.isDead() || target.isRemoved() || !canShoot.getAsBoolean()) {
            return false;
        }
        final double distanceSq = mob.distanceSqTo(target);
        return distanceSq >= minRangeSq * MIN_RANGE_HYSTERESIS * MIN_RANGE_HYSTERESIS
                && distanceSq <= maxRangeSq
                && mob.hasLineOfSightTo(target);
    }

    @Override
    public void start() {
        mob.navigation().stop();
        cooldown = cooldownTicks / 2;
    }

    @Override
    public void tick() {
        final ServerPlayer target = mob.target();
        if (target == null) {
            return;
        }
        mob.lookAt(target);
        if (cooldown > 0) {
            cooldown--;
            return;
        }
        cooldown = cooldownTicks;
        shooter.shoot(target);
    }

    @FunctionalInterface
    public interface Shooter {
        void shoot(ServerPlayer target);
    }
}
