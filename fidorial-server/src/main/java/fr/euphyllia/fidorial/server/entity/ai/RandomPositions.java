package fr.euphyllia.fidorial.server.entity.ai;

import fr.euphyllia.fidorial.server.entity.mob.AbstractPathfinderMob;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.block.BlockCollision;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.math.Location;
import fr.fidorial.math.Position;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Picks the random spots mobs walk to, as vanilla does: ten tries around the mob, each kept only if
 * it lies on solid ground, the one the mob {@linkplain AbstractPathfinderMob#walkTargetValue likes
 * best} winning.
 */
public final class RandomPositions {

    private static final int ATTEMPTS = 10;

    private RandomPositions() {
    }

    /**
     * {@return a spot on land for a mob to stroll to, {@code null} if none of the tries fits}: never
     * in water, and moved up out of the block it may fall in
     *
     * @param horizontal how far the spot may be along each horizontal axis
     * @param vertical   how far the spot may be above or below
     */
    public static @Nullable BlockPosition land(final AbstractPathfinderMob mob, final int horizontal, final int vertical) {
        return pick(mob, horizontal, vertical, true);
    }

    /**
     * {@return a spot for a mob to run to, {@code null} if none of the tries fits}: water will do
     *
     * @param horizontal how far the spot may be along each horizontal axis
     * @param vertical   how far the spot may be above or below
     */
    public static @Nullable BlockPosition any(final AbstractPathfinderMob mob, final int horizontal, final int vertical) {
        return pick(mob, horizontal, vertical, false);
    }

    private static @Nullable BlockPosition pick(final AbstractPathfinderMob mob, final int horizontal, final int vertical,
                                                final boolean landOnly) {
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final ServerWorld world = mob.serverWorld();
        final Location from = mob.location();
        final int originX = (int) Math.floor(from.x());
        final int originY = (int) Math.floor(from.y());
        final int originZ = (int) Math.floor(from.z());
        final int maxY = world.minY() + world.height() - 1;
        final PathPenalty penalty = mob.navigation().pathPenalty();

        BlockPosition best = null;
        float bestValue = Float.NEGATIVE_INFINITY;
        for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
            final int x = originX + random.nextInt(2 * horizontal + 1) - horizontal;
            int y = originY + random.nextInt(2 * vertical + 1) - vertical;
            final int z = originZ + random.nextInt(2 * horizontal + 1) - horizontal;
            if (y <= world.minY() || y > maxY || !isSolid(world, x, y - 1, z)) {
                continue;
            }
            if (landOnly) {
                while (y <= maxY && isSolid(world, x, y, z)) {
                    y++;
                }
                final BlockState state = BlockView.blockAt(world, x, y, z);
                if (state == null || BlockView.isWater(state)) {
                    continue;
                }
            }
            if (penalty != null && penalty.cost(world, x, y, z) != 0.0) {
                continue;
            }
            final float value = mob.walkTargetValue(x, y, z);
            if (value > bestValue) {
                bestValue = value;
                best = Position.block(x, y, z);
            }
        }
        return best;
    }

    private static boolean isSolid(final ServerWorld world, final int x, final int y, final int z) {
        final BlockState state = BlockView.blockAt(world, x, y, z);
        return state != null && !state.isAir() && BlockCollision.shape(state).isFullBlock();
    }
}
