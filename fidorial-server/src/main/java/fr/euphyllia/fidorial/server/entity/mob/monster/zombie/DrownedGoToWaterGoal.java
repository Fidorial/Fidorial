package fr.euphyllia.fidorial.server.entity.mob.monster.zombie;

import fr.euphyllia.fidorial.server.entity.ai.BlockView;
import fr.euphyllia.fidorial.server.entity.ai.Brightness;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.entity.ai.Goal;
import fr.fidorial.math.Location;
import fr.fidorial.math.Position;
import org.jspecify.annotations.Nullable;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Sends a drowned back to the water by day: out of it, it looks around for a block of water, ten
 * tries up to 10 blocks away and from 5 below to 2 above, and walks there.
 */
final class DrownedGoToWaterGoal implements Goal {

    private static final Set<Flag> FLAGS = Set.of(Flag.MOVE);
    private static final int ATTEMPTS = 10;
    private static final int HORIZONTAL_SPREAD = 20;
    private static final int HORIZONTAL_OFFSET = 10;
    private static final int VERTICAL_SPREAD = 8;
    private static final int VERTICAL_OFFSET = 2;

    private final DrownedEntity drowned;
    private final int priority;
    private final double speedModifier;
    private @Nullable Position water;

    DrownedGoToWaterGoal(final DrownedEntity drowned, final int priority, final double speedModifier) {
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
        if (!Brightness.isDay(drowned.serverWorld()) || drowned.isInWater()) {
            return false;
        }
        water = findWater();
        return water != null;
    }

    @Override
    public boolean shouldContinue() {
        return drowned.navigation().isNavigating();
    }

    @Override
    public void start() {
        final Position to = water;
        if (to != null) {
            drowned.navigation().moveTo(drowned.location(), to, drowned.movementSpeed() * speedModifier);
        }
    }

    @Override
    public void stop() {
        water = null;
    }

    private @Nullable Position findWater() {
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final ServerWorld world = drowned.serverWorld();
        final Location at = drowned.location();
        final int originX = (int) Math.floor(at.x());
        final int originY = (int) Math.floor(at.y());
        final int originZ = (int) Math.floor(at.z());
        for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
            final int x = originX + random.nextInt(HORIZONTAL_SPREAD) - HORIZONTAL_OFFSET;
            final int y = originY + VERTICAL_OFFSET - random.nextInt(VERTICAL_SPREAD);
            final int z = originZ + random.nextInt(HORIZONTAL_SPREAD) - HORIZONTAL_OFFSET;
            final BlockState state = BlockView.blockAt(world, x, y, z);
            if (state != null && state.isWater()) {
                return Position.block(x, y, z);
            }
        }
        return null;
    }
}
