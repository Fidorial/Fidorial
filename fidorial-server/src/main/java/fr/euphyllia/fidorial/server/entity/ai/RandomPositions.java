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

    /**
     * {@return a spot for a mob that swims as well as it walks, {@code null} if none of the tries
     * fits}: on the ground or anywhere in the water
     *
     * @param horizontal how far the spot may be along each horizontal axis
     * @param vertical   how far the spot may be above or below
     */
    public static @Nullable BlockPosition amphibious(final AbstractPathfinderMob mob, final int horizontal, final int vertical) {
        return pick(mob, horizontal, vertical, false, true);
    }

    /**
     * {@return a spot for a mob that swims as well as it walks, in the half of its surroundings that
     * faces a point, {@code null} if none of the tries fits}: on the ground or anywhere in the water
     *
     * @param horizontal how far the spot may be along each horizontal axis
     * @param vertical   how far the spot may be above or below
     * @param towardsX   where the point is along the x axis
     * @param towardsZ   where the point is along the z axis
     */
    public static @Nullable BlockPosition amphibiousTowards(final AbstractPathfinderMob mob, final int horizontal,
                                                            final int vertical, final double towardsX, final double towardsZ) {
        return towards(mob, horizontal, vertical, towardsX, towardsZ, true);
    }

    /**
     * {@return a spot on solid ground in the half of the surroundings of a mob that faces away from a
     * point, {@code null} if none of the tries fits}: where a mob flees to
     *
     * @param horizontal how far the spot may be along each horizontal axis
     * @param vertical   how far the spot may be above or below
     * @param fromX      where the point to flee is along the x axis
     * @param fromZ      where the point to flee is along the z axis
     */
    public static @Nullable BlockPosition landAwayFrom(final AbstractPathfinderMob mob, final int horizontal,
                                                       final int vertical, final double fromX, final double fromZ) {
        final Location at = mob.location();
        return towards(mob, horizontal, vertical, 2.0 * at.x() - fromX, 2.0 * at.z() - fromZ, false);
    }

    private static @Nullable BlockPosition towards(final AbstractPathfinderMob mob, final int horizontal,
                                                   final int vertical, final double towardsX, final double towardsZ,
                                                   final boolean overWater) {
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final ServerWorld world = mob.serverWorld();
        final Location from = mob.location();
        final int originX = (int) Math.floor(from.x());
        final int originY = (int) Math.floor(from.y());
        final int originZ = (int) Math.floor(from.z());
        final int maxY = world.minY() + world.height() - 1;
        final double facing = Math.atan2(towardsZ - from.z(), towardsX - from.x()) - Math.PI / 2.0;
        BlockPosition best = null;
        float bestValue = Float.NEGATIVE_INFINITY;
        for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
            final double angle = facing + (2.0 * random.nextDouble() - 1.0) * Math.PI / 2.0;
            final double distance = Math.sqrt(random.nextDouble()) * Math.sqrt(2.0) * horizontal;
            final double offsetX = -distance * Math.sin(angle);
            final double offsetZ = distance * Math.cos(angle);
            if (Math.abs(offsetX) > horizontal || Math.abs(offsetZ) > horizontal) {
                continue;
            }
            final int x = originX + (int) Math.floor(offsetX);
            final int y = originY + random.nextInt(2 * vertical + 1) - vertical;
            final int z = originZ + (int) Math.floor(offsetZ);
            if (y <= world.minY() || y > maxY || !BlockView.isPassable(world, x, y, z)
                    || !(isSolid(world, x, y - 1, z) || (overWater && isWater(world, x, y - 1, z)))) {
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

    private static @Nullable BlockPosition pick(final AbstractPathfinderMob mob, final int horizontal, final int vertical,
                                                final boolean landOnly) {
        return pick(mob, horizontal, vertical, landOnly, false);
    }

    private static @Nullable BlockPosition pick(final AbstractPathfinderMob mob, final int horizontal, final int vertical,
                                                final boolean landOnly, final boolean overWater) {
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
            if (y <= world.minY() || y > maxY
                    || !(isSolid(world, x, y - 1, z) || (overWater && isWater(world, x, y - 1, z)))) {
                continue;
            }
            if (overWater && !BlockView.isPassable(world, x, y, z)) {
                continue;
            }
            if (landOnly) {
                while (y <= maxY && isSolid(world, x, y, z)) {
                    y++;
                }
                final BlockState state = BlockView.blockAt(world, x, y, z);
                if (state == null || state.isWater()) {
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

    private static boolean isWater(final ServerWorld world, final int x, final int y, final int z) {
        final BlockState state = BlockView.blockAt(world, x, y, z);
        return state != null && state.isWater();
    }

    private static boolean isSolid(final ServerWorld world, final int x, final int y, final int z) {
        final BlockState state = BlockView.blockAt(world, x, y, z);
        return state != null && !state.isAir() && BlockCollision.shape(state).isFullBlock();
    }
}
