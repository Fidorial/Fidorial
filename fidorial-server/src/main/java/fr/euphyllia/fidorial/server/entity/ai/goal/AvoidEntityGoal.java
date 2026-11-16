package fr.euphyllia.fidorial.server.entity.ai.goal;

import com.google.common.util.concurrent.AtomicDouble;
import fr.euphyllia.fidorial.server.entity.AbstractLivingEntity;
import fr.euphyllia.fidorial.server.entity.ai.RandomPositions;
import fr.euphyllia.fidorial.server.entity.ai.Targeting;
import fr.euphyllia.fidorial.server.entity.mob.AbstractPathfinderMob;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.fidorial.entity.GameMode;
import fr.fidorial.entity.ai.Goal;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.math.Location;
import org.jspecify.annotations.Nullable;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Makes a mob keep away from a kind of entity: when one it sees comes within a distance, the mob
 * walks to a spot up to 16 blocks further from it, and runs while the entity is within 7 blocks.
 *
 * @param <T> the kind of entity the mob keeps away from
 */
public final class AvoidEntityGoal<T extends AbstractLivingEntity> implements Goal {

    private static final Set<Flag> FLAGS = Set.of(Flag.MOVE);
    private static final int FLEE_HORIZONTAL = 16;
    private static final int FLEE_VERTICAL = 7;
    private static final double SEARCH_HEIGHT = 3.0;
    private static final double RUN_DISTANCE_SQ = 49.0;

    private final AbstractPathfinderMob mob;
    private final int priority;
    private final Class<T> avoided;
    private final double maxDistance;
    private final double walkSpeedModifier;
    private final double runSpeedModifier;

    private @Nullable AbstractLivingEntity toAvoid;
    private @Nullable BlockPosition refuge;

    /**
     * @param maxDistance       how close the entity has to come for the mob to move away, in blocks
     * @param walkSpeedModifier what the movement speed of the mob is multiplied by on its way
     * @param runSpeedModifier  the same, while the entity is within 7 blocks
     */
    public AvoidEntityGoal(final AbstractPathfinderMob mob, final int priority, final Class<T> avoided,
                           final double maxDistance, final double walkSpeedModifier, final double runSpeedModifier) {
        this.mob = mob;
        this.priority = priority;
        this.avoided = avoided;
        this.maxDistance = maxDistance;
        this.walkSpeedModifier = walkSpeedModifier;
        this.runSpeedModifier = runSpeedModifier;
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
        final AbstractLivingEntity nearest = findNearest();
        if (nearest == null) {
            return false;
        }
        final Location from = nearest.location();
        final BlockPosition spot = RandomPositions.landAwayFrom(mob, FLEE_HORIZONTAL, FLEE_VERTICAL, from.x(), from.z());
        // a spot closer to the entity than the mob is would not help
        if (spot == null || distanceSq(from, spot.blockX() + 0.5, spot.blockY(), spot.blockZ() + 0.5)
                < distanceSq(from, mob.location().x(), mob.location().y(), mob.location().z())) {
            return false;
        }
        toAvoid = nearest;
        refuge = spot;
        return true;
    }

    @Override
    public boolean shouldContinue() {
        return mob.navigation().isNavigating();
    }

    @Override
    public void start() {
        final BlockPosition spot = refuge;
        if (spot != null) {
            // as a mob that panics: what it was doing does not hold it back
            mob.navigation().stop();
            mob.navigation().moveTo(mob.location(), spot, mob.movementSpeed() * walkSpeedModifier);
        }
    }

    @Override
    public void stop() {
        toAvoid = null;
        refuge = null;
    }

    @Override
    public void tick() {
        final AbstractLivingEntity entity = toAvoid;
        if (entity == null) {
            return;
        }
        final double modifier = mob.distanceSqTo(entity) < RUN_DISTANCE_SQ ? runSpeedModifier : walkSpeedModifier;
        mob.navigation().setSpeed(mob.movementSpeed() * modifier);
    }

    /**
     * The nearest entity of the kind within the distance, in sight, and neither a creative nor a
     * spectator player.
     */
    private @Nullable AbstractLivingEntity findNearest() {
        final Location self = mob.location();
        final double side = mob.width() / 2.0 + maxDistance;
        final AtomicReference<@Nullable AbstractLivingEntity> nearest = new AtomicReference<>(null);
        final AtomicDouble best = new AtomicDouble(Double.MAX_VALUE);
        mob.serverWorld().entityManager().forEachNear(self.chunk(), maxDistance + 1.0, entity -> {
            if (!avoided.isInstance(entity) || !(entity instanceof final AbstractLivingEntity living)
                    || (entity instanceof final ServerPlayer player
                    && (player.gameMode() == GameMode.CREATIVE || player.gameMode() == GameMode.SPECTATOR))) {
                return;
            }
            final Location at = living.location();
            if (Math.abs(at.x() - self.x()) > side || Math.abs(at.z() - self.z()) > side
                    || at.y() < self.y() - SEARCH_HEIGHT || at.y() > self.y() + mob.height() + SEARCH_HEIGHT
                    || !Targeting.test(mob, living, maxDistance, true, true)) {
                return;
            }
            final double distanceSq = mob.distanceSqTo(living);
            if (distanceSq < best.get()) {
                best.set(distanceSq);
                nearest.set(living);
            }
        });
        return nearest.get();
    }

    private static double distanceSq(final Location from, final double x, final double y, final double z) {
        final double dx = x - from.x();
        final double dy = y - from.y();
        final double dz = z - from.z();
        return dx * dx + dy * dy + dz * dz;
    }
}
