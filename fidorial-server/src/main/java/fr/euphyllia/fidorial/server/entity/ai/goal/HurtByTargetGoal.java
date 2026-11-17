package fr.euphyllia.fidorial.server.entity.ai.goal;

import fr.euphyllia.fidorial.server.entity.AbstractLivingEntity;
import fr.euphyllia.fidorial.server.entity.ai.Targeting;
import fr.euphyllia.fidorial.server.entity.mob.AbstractPathfinderMob;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.fidorial.math.Location;
import fr.fidorial.registry.keys.GameRuleKeys;

import java.util.Set;

/**
 * Makes a mob go after whoever hurt it; it then keeps after its attacker for 15 seconds without
 * seeing it. With {@link #alertOthers(Class[])} the mobs of its
 * kind around it that have no target join in.
 */
public final class HurtByTargetGoal extends TargetGoal {

    private static final double ALERT_RANGE_Y = 10.0;
    private static final int UNSEEN_MEMORY_TICKS = 300;

    private int answeredStamp;
    private boolean alertsOthers;
    private Set<Class<?>> notAlerted = Set.of();
    private Set<Class<?>> forgiven = Set.of();

    public HurtByTargetGoal(final AbstractPathfinderMob mob, final int priority) {
        super(mob, priority, true);
        this.answeredStamp = mob.lastHurtByMobStamp();
    }

    /**
     * Makes the mob call those of its kind for help: every mob of its class, or of a class that
     * extends it, within its follow range and 10 blocks above or below.
     *
     * @param except the classes of the mobs that do not answer the call
     * @return this goal
     */
    public HurtByTargetGoal alertOthers(final Class<?>... except) {
        this.alertsOthers = true;
        this.notAlerted = Set.of(except);
        return this;
    }

    /**
     * Makes the mob put up with the blows of some mobs: it does not go after an attacker of one of
     * these classes, or of a class that extends one of them.
     *
     * @param forgiven the classes of the attackers the mob leaves alone
     * @return this goal
     */
    public HurtByTargetGoal ignoringDamageFrom(final Class<?>... forgiven) {
        this.forgiven = Set.of(forgiven);
        return this;
    }

    @Override
    public boolean canStart() {
        final AbstractLivingEntity attacker = mob.lastHurtByMob();
        if (attacker == null || mob.lastHurtByMobStamp() == answeredStamp) {
            return false;
        }
        if (attacker instanceof ServerPlayer && mob.serverWorld().gameRuleValues().getBoolean(GameRuleKeys.UNIVERSAL_ANGER)) {
            // a mob angry at every player does not single out the one that hurt it
            return false;
        }
        for (final Class<?> kind : forgiven) {
            if (kind.isInstance(attacker)) {
                return false;
            }
        }
        // the one that hurt the mob is known without being seen, hidden or not
        return Targeting.test(mob, attacker, 0.0, false, false);
    }

    @Override
    public void start() {
        final AbstractLivingEntity attacker = mob.lastHurtByMob();
        mob.setTarget(attacker);
        targetMob = mob.target();
        answeredStamp = mob.lastHurtByMobStamp();
        unseenMemoryTicks = UNSEEN_MEMORY_TICKS;
        if (alertsOthers && attacker != null) {
            alert(attacker);
        }
        super.start();
    }

    private void alert(final AbstractLivingEntity attacker) {
        final double range = mob.followRange();
        final Location self = mob.location();
        final Class<?> kind = mob.getClass();
        mob.serverWorld().entityManager().forEachNear(self.chunk(), range + 1.0, entity -> {
            if (entity == mob || !kind.isInstance(entity) || notAlerted.contains(entity.getClass())
                    || !(entity instanceof final AbstractPathfinderMob other)
                    || other.isRemoved() || other.isDead() || other.target() != null) {
                return;
            }
            final Location pos = other.location();
            // vanilla's box: a block at the feet of the mob, grown by the range and by 10 blocks in height
            if (outside(pos.x(), self.x(), range) || outside(pos.z(), self.z(), range)
                    || outside(pos.y(), self.y(), ALERT_RANGE_Y)) {
                return;
            }
            other.setTarget(attacker);
        });
    }

    private static boolean outside(final double value, final double corner, final double grown) {
        return value < corner - grown || value > corner + 1.0 + grown;
    }
}
