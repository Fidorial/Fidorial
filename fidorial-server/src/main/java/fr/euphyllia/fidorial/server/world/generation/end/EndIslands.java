package fr.euphyllia.fidorial.server.world.generation.end;

import fr.euphyllia.fidorial.server.world.generation.noise.SimplexNoise;
import fr.euphyllia.fidorial.server.world.generation.random.BitRandom;

/**
 * Where the land of the End is, seen from above: one large island around the origin, an empty ring, then
 * scattered islands out to the edge of the world.
 *
 * <p>The value of a column is high in the middle of an island and falls with the distance to it; it does not
 * depend on the height. The terrain adds it to its noise, and the biomes read it to tell the heart of an island
 * from its shore.</p>
 */
final class EndIslands {

    // the noise is drawn after this many integers of the stream
    private static final int SKIPPED_DRAWS = 17292;

    private static final float PEAK = 100.0F;
    private static final float FLOOR = -100.0F;
    private static final float CEILING = 80.0F;
    private static final float BASELINE = 8.0F;
    private static final float RANGE = 128.0F;

    // islands are laid on a grid of 16 blocks, and looked for up to 12 grid squares away
    private static final int SEARCH_RADIUS = 12;
    // no scattered island within this many grid squares of the origin, squared
    private static final long EMPTY_RING_SQ = 4096L;
    private static final float ISLAND_THRESHOLD = -0.9F;
    private static final int HALF_SQUARE = 8;

    private final SimplexNoise scatter;

    EndIslands(final BitRandom random) {
        random.skip(SKIPPED_DRAWS);
        this.scatter = new SimplexNoise(random, 0.0);
    }

    private static float mainIsland(final int blockX, final int blockZ) {
        final float x = (float) blockX;
        final float z = (float) blockZ;
        final float distance = (float) Math.sqrt(x * x + z * z);
        return (Math.clamp(PEAK - distance, FLOOR, CEILING) - BASELINE) / RANGE;
    }

    /**
     * {@return the land value of a column, from {@code -0.84375} over the void to {@code 0.5625} at the heart of
     * an island}
     */
    float valueAt(final int blockX, final int blockZ) {
        return Math.max(mainIsland(blockX, blockZ), scatteredIslands(blockX / HALF_SQUARE, blockZ / HALF_SQUARE));
    }

    /**
     * Each grid square the noise marks holds an island, whose slope depends on where the square is: the highest
     * of the islands in reach wins.
     *
     * @param halfX the column, in half grid squares
     * @param halfZ the same along z
     */
    private float scatteredIslands(final int halfX, final int halfZ) {
        final int squareX = halfX / 2;
        final int squareZ = halfZ / 2;
        final int insideX = halfX % 2;
        final int insideZ = halfZ % 2;
        float height = FLOOR;
        for (int offsetX = -SEARCH_RADIUS; offsetX <= SEARCH_RADIUS; offsetX++) {
            for (int offsetZ = -SEARCH_RADIUS; offsetZ <= SEARCH_RADIUS; offsetZ++) {
                final long islandX = squareX + offsetX;
                final long islandZ = squareZ + offsetZ;
                if (islandX * islandX + islandZ * islandZ <= EMPTY_RING_SQ) {
                    continue;
                }
                if (scatter.sample((double) islandX, (double) islandZ) >= ISLAND_THRESHOLD) {
                    continue;
                }
                final float steepness = (Math.abs((float) islandX) * 3439.0F + Math.abs((float) islandZ) * 147.0F) % 13.0F + 9.0F;
                final float awayX = (float) (insideX - offsetX * 2);
                final float awayZ = (float) (insideZ - offsetZ * 2);
                final float distance = (float) Math.sqrt(awayX * awayX + awayZ * awayZ);
                height = Math.max(height, Math.clamp(PEAK - distance * steepness, FLOOR, CEILING));
            }
        }
        return (height - BASELINE) / RANGE;
    }
}
