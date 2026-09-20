package fr.euphyllia.fidorial.server.entity.ai.goal;

import fr.euphyllia.fidorial.server.entity.mob.AbstractPathfinderMob;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.fidorial.entity.ai.Goal;
import fr.fidorial.world.BlockPos;

public class MeleeAttackGoal implements Goal {

    private static final int ATTACK_COOLDOWN_TICKS = 20;

    private static final int PATH_INTERVAL_TICKS = 10;

    private final AbstractPathfinderMob mob;
    private final int priority;
    private final double speed;
    private final Attacker attacker;
    private final double mobWidth;

    private int attackCooldown;
    private int pathDelay;

    public MeleeAttackGoal(final AbstractPathfinderMob mob, final int priority, final double speed,
                           final double mobWidth, final Attacker attacker) {
        this.mob = mob;
        this.priority = priority;
        this.speed = speed;
        this.mobWidth = mobWidth;
        this.attacker = attacker;
    }

    @Override
    public int priority() {
        return priority;
    }

    @Override
    public boolean canStart() {
        final ServerPlayer target = mob.target();
        return target != null && !target.isDead();
    }

    @Override
    public boolean shouldContinue() {
        final ServerPlayer target = mob.target();
        return target != null && !target.isDead() && !target.isRemoved();
    }

    @Override
    public void start() {
        pathDelay = 0;
        attackCooldown = 0;
    }

    @Override
    public void stop() {
        mob.navigation().stop();
        attackCooldown = 0;
    }

    @Override
    public void tick() {
        final ServerPlayer target = mob.target();
        if (target == null) {
            return;
        }

        mob.lookAt(target);
        mob.setMoveSpeed(speed);

        if (--pathDelay <= 0) {
            pathDelay = PATH_INTERVAL_TICKS;
            final Location goal = target.location();
            mob.navigation().moveTo(mob.location(), new BlockPos(
                    (int) Math.floor(goal.x()),
                    (int) Math.floor(goal.y()),
                    (int) Math.floor(goal.z())));
        }

        if (attackCooldown > 0) {
            attackCooldown--;
        }

        if (attackCooldown == 0 && isWithinReach(target)) {
            attackCooldown = ATTACK_COOLDOWN_TICKS;
            attacker.performAttack(target);
        }
    }

    private boolean isWithinReach(final ServerPlayer target) {
        final double targetWidth = 0.6;
        final double reachSq = mobWidth * 2.0 * mobWidth * 2.0 + targetWidth;

        final Location self = mob.location();
        final Location other = target.location();
        final double dx = self.x() - other.x();
        final double dz = self.z() - other.z();
        final double dy = self.y() - other.y();

        return dx * dx + dz * dz <= reachSq && Math.abs(dy) <= 2.0;
    }

    @FunctionalInterface
    public interface Attacker {
        void performAttack(ServerPlayer target);
    }
}
