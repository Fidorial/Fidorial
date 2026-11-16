package fr.euphyllia.fidorial.server.entity.ai.goal;

import fr.euphyllia.fidorial.server.entity.AbstractLivingEntity;
import fr.euphyllia.fidorial.server.entity.ai.Targeting;
import fr.euphyllia.fidorial.server.entity.mob.AbstractPathfinderMob;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.fidorial.math.Location;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

/**
 * Makes a mob go after the nearest living entity of a kind within its
 * {@linkplain AbstractPathfinderMob#followRange() follow range}. The mob
 * looks around once in a while rather than every tick, and notices from
 * closer an entity that {@linkplain Targeting#visibility hides}.
 *
 * @param <T> the kind of entity the mob goes after
 */
public final class NearestAttackableTargetGoal<T extends AbstractLivingEntity> extends TargetGoal {

    /**
     * Vanilla looks for a target one time out of five, every other tick.
     */
    private static final int DEFAULT_INTERVAL = 10;

    private final Class<T> targetType;
    private final int interval;
    private final boolean mustSee;
    private final @Nullable Predicate<T> selector;
    private BooleanSupplier condition = () -> true;
    private @Nullable AbstractLivingEntity found;

    /**
     * @param mustSee {@code true} if the mob only goes after what it sees
     */
    public NearestAttackableTargetGoal(final AbstractPathfinderMob mob, final int priority,
                                       final Class<T> targetType, final boolean mustSee) {
        this(mob, priority, targetType, DEFAULT_INTERVAL, mustSee, null);
    }

    /**
     * @param interval the mob looks for a target one tick out of this many, on average
     * @param mustSee  {@code true} if the mob only goes after what it sees
     * @param selector what else an entity has to be for the mob to go after it, or {@code null}
     */
    public NearestAttackableTargetGoal(final AbstractPathfinderMob mob, final int priority,
                                       final Class<T> targetType, final int interval, final boolean mustSee,
                                       final @Nullable Predicate<T> selector) {
        super(mob, priority, mustSee);
        this.targetType = targetType;
        this.interval = interval;
        this.mustSee = mustSee;
        this.selector = selector;
    }

    /**
     * Makes the mob look for a target only while a condition holds, such as a spider in the dark;
     * a target it already has is kept whatever the condition.
     *
     * @return this goal
     */
    public NearestAttackableTargetGoal<T> onlyWhen(final BooleanSupplier condition) {
        this.condition = condition;
        return this;
    }

    @Override
    public boolean canStart() {
        if (!condition.getAsBoolean()) {
            return false;
        }
        if (interval > 0 && ThreadLocalRandom.current().nextInt(interval) != 0) {
            return false;
        }
        found = findTarget();
        return found != null;
    }

    @Override
    public void start() {
        mob.setTarget(found);
        targetMob = mob.target();
        super.start();
    }

    @Override
    public void stop() {
        found = null;
        super.stop();
    }

    private @Nullable AbstractLivingEntity findTarget() {
        final double range = mob.followRange();
        final Nearest nearest = new Nearest();
        if (targetType == ServerPlayer.class) {
            for (final ServerPlayer player : mob.server().players().online()) {
                consider(player, range, nearest);
            }
        } else {
            mob.serverWorld().entityManager().forEachNear(mob.location().chunk(), range + mob.width(), entity -> {
                if (entity instanceof final AbstractLivingEntity living) {
                    consider(living, range, nearest);
                }
            });
        }
        return nearest.entity;
    }

    private void consider(final AbstractLivingEntity entity, final double range, final Nearest nearest) {
        if (!targetType.isInstance(entity)) {
            return;
        }
        final T candidate = targetType.cast(entity);
        if (selector != null && !selector.test(candidate)) {
            return;
        }
        if (!withinSearchBox(entity, range) || !Targeting.test(mob, entity, range, mustSee, true)) {
            return;
        }
        final double distanceSq = distanceFromEyesSq(entity);
        if (distanceSq < nearest.distanceSq) {
            nearest.distanceSq = distanceSq;
            nearest.entity = entity;
        }
    }

    /**
     * Vanilla only looks at the entities in the box of the mob grown by its follow range; players
     * are the exception, any of them in the world may be picked.
     */
    private boolean withinSearchBox(final AbstractLivingEntity entity, final double range) {
        if (entity instanceof ServerPlayer) {
            return true;
        }
        final Location self = mob.location();
        final Location other = entity.location();
        final double side = mob.width() / 2.0 + range;
        return Math.abs(other.x() - self.x()) <= side
                && Math.abs(other.z() - self.z()) <= side
                && other.y() >= self.y() - range
                && other.y() <= self.y() + mob.height() + range;
    }

    private double distanceFromEyesSq(final AbstractLivingEntity entity) {
        final Location self = mob.location();
        final Location other = entity.location();
        final double dx = other.x() - self.x();
        final double dy = other.y() - (self.y() + mob.eyeHeight());
        final double dz = other.z() - self.z();
        return dx * dx + dy * dy + dz * dz;
    }

    private static final class Nearest {
        private @Nullable AbstractLivingEntity entity;
        private double distanceSq = Double.MAX_VALUE;
    }
}
