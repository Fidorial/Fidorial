package fr.euphyllia.fidorial.server.world.generation.end;

import fr.euphyllia.fidorial.server.world.generation.random.LegacyRandom;

/**
 * The shape of the End for a seed: which blocks are land and which are void.
 *
 * <p>A block is land where a density is positive. The density is the land value of the column
 * plus a noise, pulled down near the bottom and the top of the terrain so that the islands float and end in a
 * rounded cap. It is only computed on a coarse grid, one point every 8 blocks sideways and every 4 blocks
 * upwards, and blended in between, which is what makes the slopes smooth.</p>
 *
 * <p>Everything is single precision and done in a fixed order, so a seed always gives the same blocks, and two
 * neighbouring chunks agree on the grid points they share. Immutable, safe to use from several threads.</p>
 */
public final class EndTerrain {

    /**
     * The terrain lies between y = 0 and this height, excluded.
     */
    public static final int HEIGHT = 128;

    private static final int CHUNK_WIDTH = 16;
    private static final int CELL_WIDTH = 8;
    private static final int CELL_HEIGHT = 4;
    private static final int POINTS_ACROSS = CHUNK_WIDTH / CELL_WIDTH + 1;
    private static final int LEVELS = HEIGHT / CELL_HEIGHT + 1;

    // under the islands the density slides towards a slightly negative value, which rounds their underside
    private static final int BOTTOM_FADE_START = 4;
    private static final int BOTTOM_FADE_END = 32;
    private static final float BOTTOM_DENSITY = -0.234375F;
    // above, it slides towards a strongly negative one; the terrain stops well before the slide is complete
    private static final int TOP_FADE_START = 56;
    private static final int TOP_FADE_END = 312;
    private static final float TOP_DENSITY = -23.4375F;
    private static final float DENSITY_SCALE = 0.64F;

    private final EndIslands islands;
    private final EndTerrainNoise noise;
    private final float[] bottomFade = new float[LEVELS];
    private final float[] topFade = new float[LEVELS];

    /**
     * @param seed the seed of the world
     */
    public EndTerrain(final long seed) {
        // two independent streams of the same seed: the place of the islands does not depend on the terrain noise
        this.islands = new EndIslands(new LegacyRandom(seed));
        this.noise = new EndTerrainNoise(new LegacyRandom(seed));
        for (int level = 0; level < LEVELS; level++) {
            final int y = level * CELL_HEIGHT;
            bottomFade[level] = ramp(y, BOTTOM_FADE_START, BOTTOM_FADE_END, 0.0F, 1.0F);
            topFade[level] = ramp(y, TOP_FADE_START, TOP_FADE_END, 1.0F, 0.0F);
        }
    }

    private static float ramp(final int y, final int from, final int to, final float fromValue, final float toValue) {
        final int clamped = Math.clamp(y, from, to);
        return fromValue + (float) (clamped - from) * ((toValue - fromValue) / (float) (to - from));
    }

    /**
     * {@return {@code first} for an amount of zero, {@code second} for one, a blend in between}
     */
    private static float slide(final float amount, final float first, final float second) {
        if (amount <= 0.0F) {
            return first;
        }
        if (amount >= 1.0F) {
            return second;
        }
        return first + amount * (second - first);
    }

    private static boolean isLand(final float density) {
        // squash the density into a soft curve; the curve keeps the sign, land is where it stays positive
        final float clamped = Math.clamp(density, -1.0F, 1.0F);
        return clamped / 2.0F - clamped * clamped * clamped / 24.0F > 0.0F;
    }

    private static int pointIndex(final int pointX, final int level, final int pointZ) {
        return (pointZ * POINTS_ACROSS + pointX) * LEVELS + level;
    }

    /**
     * {@return the land value of a column: high at the heart of an island, lowest over the void, the same at
     * every height}
     */
    public float landValue(final int blockX, final int blockZ) {
        return islands.valueAt(blockX, blockZ);
    }

    /**
     * Calls {@code sink} for every land block of a chunk.
     *
     * @param chunkX the chunk, in chunk coordinates
     * @param chunkZ the same along z
     * @param sink   receives each land block: x and z from 0 to 15 inside the chunk, y from 0 to {@value #HEIGHT}
     *               excluded
     */
    public void forEachLandBlock(final int chunkX, final int chunkZ, final LandSink sink) {
        final float[] points = gridPoints(chunkX, chunkZ);
        final float[] column = new float[LEVELS];
        for (int z = 0; z < CHUNK_WIDTH; z++) {
            final int pointZ = z / CELL_WIDTH;
            final float towardsSouth = (float) (z % CELL_WIDTH) / (float) CELL_WIDTH;
            final float towardsNorth = 1.0F - towardsSouth;
            for (int x = 0; x < CHUNK_WIDTH; x++) {
                final int pointX = x / CELL_WIDTH;
                final float towardsEast = (float) (x % CELL_WIDTH) / (float) CELL_WIDTH;
                final float towardsWest = 1.0F - towardsEast;

                // blend the four grid columns around the block: along z first, then along x
                for (int level = 0; level < LEVELS; level++) {
                    final float west = towardsNorth * points[pointIndex(pointX, level, pointZ)]
                            + towardsSouth * points[pointIndex(pointX, level, pointZ + 1)];
                    final float east = towardsNorth * points[pointIndex(pointX + 1, level, pointZ)]
                            + towardsSouth * points[pointIndex(pointX + 1, level, pointZ + 1)];
                    column[level] = towardsWest * west + towardsEast * east;
                }

                // then climb the column, from each grid level to the next
                for (int level = 0; level < LEVELS - 1; level++) {
                    final float base = column[level];
                    final float slope = (column[level + 1] - base) * (1.0F / CELL_HEIGHT);
                    for (int step = 0; step < CELL_HEIGHT; step++) {
                        if (isLand(base + slope * (float) step)) {
                            sink.land(x, level * CELL_HEIGHT + step, z);
                        }
                    }
                }
            }
        }
    }

    /**
     * {@return the density at every grid point the chunk touches, its far edges included}
     */
    private float[] gridPoints(final int chunkX, final int chunkZ) {
        final float[] points = new float[POINTS_ACROSS * POINTS_ACROSS * LEVELS];
        for (int pointZ = 0; pointZ < POINTS_ACROSS; pointZ++) {
            final int blockZ = (chunkZ << 4) + pointZ * CELL_WIDTH;
            for (int pointX = 0; pointX < POINTS_ACROSS; pointX++) {
                final int blockX = (chunkX << 4) + pointX * CELL_WIDTH;
                final float land = islands.valueAt(blockX, blockZ);
                for (int level = 0; level < LEVELS; level++) {
                    final float raw = land + noise.sample(blockX, level * CELL_HEIGHT, blockZ);
                    final float capped = slide(topFade[level], TOP_DENSITY, raw);
                    final float floating = slide(bottomFade[level], BOTTOM_DENSITY, capped);
                    points[pointIndex(pointX, level, pointZ)] = floating * DENSITY_SCALE;
                }
            }
        }
        return points;
    }

    /**
     * Receives the land blocks of a chunk.
     */
    @FunctionalInterface
    public interface LandSink {

        /**
         * @param x the block inside the chunk, from 0 to 15
         * @param y the height of the block
         * @param z the block inside the chunk, from 0 to 15
         */
        void land(int x, int y, int z);
    }
}
