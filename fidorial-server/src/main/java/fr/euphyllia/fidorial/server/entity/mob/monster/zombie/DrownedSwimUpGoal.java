package fr.euphyllia.fidorial.server.entity.mob.monster.zombie;

import fr.euphyllia.fidorial.server.entity.ai.Brightness;
import fr.euphyllia.fidorial.server.entity.ai.RandomPositions;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.fidorial.entity.ai.Goal;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.math.Location;
import fr.fidorial.math.Position;

import java.util.Set;

/**
 * Makes a drowned come up at night: deep in the water, it swims from one random spot to the next
 * while it drifts up, until it is 2 blocks under the surface of the sea. It holds no flag, so the
 * other goals keep running beside it.
 */
final class DrownedSwimUpGoal implements Goal {

    private static final Set<Flag> FLAGS = Set.of();
    private static final int HORIZONTAL = 4;
    private static final int VERTICAL = 8;
    private static final double CLOSE_TO_DESTINATION_SQ = 4.0;

    private final DrownedEntity drowned;
    private final int priority;
    private final double speedModifier;
    private boolean stuck;

    DrownedSwimUpGoal(final DrownedEntity drowned, final int priority, final double speedModifier) {
        this.drowned = drowned;
        this.priority = priority;
        this.speedModifier = speedModifier;
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
        final ServerWorld world = drowned.serverWorld();
        return !Brightness.isDay(world) && drowned.isInWater() && drowned.location().y() < world.seaLevel() - 2;
    }

    @Override
    public boolean shouldContinue() {
        return canStart() && !stuck;
    }

    @Override
    public void start() {
        drowned.setSearchingForLand(true);
        stuck = false;
    }

    @Override
    public void stop() {
        drowned.setSearchingForLand(false);
    }

    @Override
    public void tick() {
        final Location at = drowned.location();
        if (at.y() >= drowned.serverWorld().seaLevel() - 1) {
            return;
        }
        if (drowned.navigation().isNavigating() && !isCloseToDestination(at)) {
            return;
        }
        // straight above: any direction will do
        final BlockPosition next = RandomPositions.amphibiousTowards(drowned, HORIZONTAL, VERTICAL, at.x(), at.z());
        if (next == null) {
            stuck = true;
            return;
        }
        drowned.navigation().moveTo(at, next, drowned.movementSpeed() * speedModifier);
    }

    private boolean isCloseToDestination(final Location at) {
        final Position destination = drowned.navigation().destination();
        if (destination == null) {
            return false;
        }
        final double dx = destination.x() - at.x();
        final double dy = destination.y() - at.y();
        final double dz = destination.z() - at.z();
        return dx * dx + dy * dy + dz * dz < CLOSE_TO_DESTINATION_SQ;
    }
}
