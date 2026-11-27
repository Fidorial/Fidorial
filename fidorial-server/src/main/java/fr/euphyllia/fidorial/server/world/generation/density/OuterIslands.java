package fr.euphyllia.fidorial.server.world.generation.density;

import fr.euphyllia.fidorial.server.world.generation.noise.SimplexNoise;
import fr.euphyllia.fidorial.server.world.generation.random.LegacyRandom;

/**
 * The scattered islands of the End, out past the void around the main one: the {@code end_outer_islands}
 * function.
 *
 * <p>The world is cut into squares of 16 blocks, and a noise marks some of them, far enough from the origin, as
 * the heart of an island. A column takes its value from the marked squares in reach, each with a slope of its own
 * derived from where it lies, and the highest one wins. The value does not depend on the height. Immutable, safe
 * to share between threads.</p>
 */
final class OuterIslands extends Density {

    // the noise is drawn after this many integers of the stream of the world seed
    private static final int SKIPPED_DRAWS = 17292;

    private static final float PEAK = 100.0F;
    private static final float LOWEST = -100.0F;
    private static final float HIGHEST = 80.0F;
    private static final float BASELINE = 8.0F;
    private static final float RANGE = 128.0F;

    // a column looks up to this many squares away for an island
    private static final int SEARCH_RADIUS = 12;
    // no island within this many squares of the origin, squared
    private static final long EMPTY_SQUARES_SQ = 4096L;
    private static final float MARKED_BELOW = -0.9F;
    private static final int HALF_SQUARE = 8;

    private final SimplexNoise marks;

    /**
     * @param seed the seed of the world
     */
    OuterIslands(final long seed) {
        super((LOWEST - BASELINE) / RANGE, (HIGHEST - BASELINE) / RANGE);
        final LegacyRandom random = new LegacyRandom(seed);
        random.skip(SKIPPED_DRAWS);
        this.marks = new SimplexNoise(random, 0.0);
    }

    @Override
    public float compute(final DensityContext context, final int x, final int y, final int z) {
        return (height(x / HALF_SQUARE, z / HALF_SQUARE) - BASELINE) / RANGE;
    }

    @Override
    public int axes() {
        return X | Z;
    }

    /**
     * {@return the height of the land at a column, {@value #LOWEST} when no island reaches it}
     *
     * @param halfX the column, in half squares
     * @param halfZ the same along z
     */
    private float height(final int halfX, final int halfZ) {
        final int squareX = halfX / 2;
        final int squareZ = halfZ / 2;
        final int insideX = halfX % 2;
        final int insideZ = halfZ % 2;
        float height = LOWEST;
        for (int offsetX = -SEARCH_RADIUS; offsetX <= SEARCH_RADIUS; offsetX++) {
            for (int offsetZ = -SEARCH_RADIUS; offsetZ <= SEARCH_RADIUS; offsetZ++) {
                final long islandX = squareX + offsetX;
                final long islandZ = squareZ + offsetZ;
                if (islandX * islandX + islandZ * islandZ <= EMPTY_SQUARES_SQ
                        || marks.sample((double) islandX, (double) islandZ) >= MARKED_BELOW) {
                    continue;
                }
                final float steepness = (Math.abs((float) islandX) * 3439.0F + Math.abs((float) islandZ) * 147.0F) % 13.0F + 9.0F;
                final float awayX = (float) (insideX - offsetX * 2);
                final float awayZ = (float) (insideZ - offsetZ * 2);
                final float distance = (float) Math.sqrt(awayX * awayX + awayZ * awayZ);
                height = Math.max(height, Math.clamp(PEAK - distance * steepness, LOWEST, HIGHEST));
            }
        }
        return height;
    }
}
