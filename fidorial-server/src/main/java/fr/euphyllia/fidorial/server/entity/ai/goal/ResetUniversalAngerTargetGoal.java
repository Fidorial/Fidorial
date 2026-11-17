package fr.euphyllia.fidorial.server.entity.ai.goal;

import fr.euphyllia.fidorial.server.entity.mob.AbstractPathfinderMob;
import fr.euphyllia.fidorial.server.entity.mob.AngerableMob;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.fidorial.entity.ai.Goal;
import fr.fidorial.math.Location;
import fr.fidorial.registry.keys.GameRuleKeys;

import java.util.Set;

/**
 * With the {@code universal_anger} game rule, makes a mob a player hurt angry at every player
 * rather than at that one: it forgets its target and starts its anger over. With
 * {@code alertOthers}, the mobs of its kind around it do the same.
 *
 * @param <T> the kind of mob
 */
public final class ResetUniversalAngerTargetGoal<T extends AbstractPathfinderMob & AngerableMob> implements Goal {

    private static final double ALERT_RANGE_Y = 10.0;

    private final T mob;
    private final int priority;
    private final boolean alertOthers;
    private int answeredStamp;

    /**
     * @param alertOthers {@code true} if the mobs of the same kind around the mob forget their
     *                    target too
     */
    public ResetUniversalAngerTargetGoal(final T mob, final int priority, final boolean alertOthers) {
        this.mob = mob;
        this.priority = priority;
        this.alertOthers = alertOthers;
        this.answeredStamp = mob.lastHurtByMobStamp();
    }

    @Override
    public int priority() {
        return priority;
    }

    @Override
    public Set<Flag> flags() {
        return Set.of();
    }

    @Override
    public boolean canStart() {
        return mob.serverWorld().gameRuleValues().getBoolean(GameRuleKeys.UNIVERSAL_ANGER)
                && mob.lastHurtByMob() instanceof ServerPlayer
                && mob.lastHurtByMobStamp() != answeredStamp;
    }

    @Override
    public boolean shouldContinue() {
        return false;
    }

    @Override
    public void start() {
        answeredStamp = mob.lastHurtByMobStamp();
        mob.anger().forgetCurrentTargetAndRefreshUniversalAnger();
        if (!alertOthers) {
            return;
        }
        final double range = mob.followRange();
        final Location at = mob.location();
        mob.serverWorld().entityManager().forEachNear(at.chunk(), range + 1.0, entity -> {
            if (entity != mob && mob.getClass().isInstance(entity) && entity instanceof final AngerableMob other
                    && !entity.isRemoved()) {
                final Location there = entity.location();
                // the box of the block the mob stands in, grown by its follow range and 10 blocks up and down
                final double fromX = Math.floor(at.x());
                final double fromY = Math.floor(at.y());
                final double fromZ = Math.floor(at.z());
                if (there.x() >= fromX - range && there.x() <= fromX + 1.0 + range
                        && there.y() >= fromY - ALERT_RANGE_Y && there.y() <= fromY + 1.0 + ALERT_RANGE_Y
                        && there.z() >= fromZ - range && there.z() <= fromZ + 1.0 + range) {
                    other.anger().forgetCurrentTargetAndRefreshUniversalAnger();
                }
            }
        });
    }
}
