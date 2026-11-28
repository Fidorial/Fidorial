package fr.euphyllia.fidorial.server.world.generation.biome;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Turns biomes chosen on a coarse grid, one point every 4 blocks on each axis, into a biome for every block,
 * without the blocky look of the grid.
 *
 * <p>Every grid point is moved by a small offset drawn from the seed and from its position. A block takes the
 * biome of the nearest moved point among the eight corners of the grid cell around it: the borders between
 * biomes come out irregular, and always the same for a seed.</p>
 */
public final class JitteredBiomeGrid {

    /**
     * Set in a corner returned by {@link #nearestCorner(int, int, int)} when it is on the far side along x.
     */
    public static final int FAR_X = 4;
    /**
     * Set in a corner when it is on the far side along y.
     */
    public static final int FAR_Y = 2;
    /**
     * Set in a corner when it is on the far side along z.
     */
    public static final int FAR_Z = 1;

    private static final int CORNERS = 8;
    private static final int CELL_SHIFT = 2;
    private static final int CELL_MASK = 3;
    private static final double CELL_SIZE = 4.0;
    // the cells are shifted by half their size, so that a block sits between the points it chooses from
    private static final int HALF_CELL = 2;

    private static final long MULTIPLIER = 6364136223846793005L;
    private static final long INCREMENT = 1442695040888963407L;
    private static final int JITTER_STEPS = 1024;
    private static final double JITTER_RANGE = 0.9;

    private static final int STEP_BITS = 10;
    private static final int STEP_MASK = (1 << STEP_BITS) - 1;
    // the offsets a point can be moved by, one per jitter step: what offset(state) computes, looked up
    private static final double[] OFFSETS = new double[JITTER_STEPS];

    static {
        for (int step = 0; step < JITTER_STEPS; step++) {
            OFFSETS[step] = (step / (double) JITTER_STEPS - 0.5) * JITTER_RANGE;
        }
    }

    private final long seed;

    /**
     * @param worldSeed the seed of the world; it is hashed first, so that close seeds give unrelated borders
     */
    public JitteredBiomeGrid(final long worldSeed) {
        this.seed = hashSeed(worldSeed);
    }

    private static long hashSeed(final long worldSeed) {
        final byte[] bytes = ByteBuffer.allocate(Long.BYTES).order(ByteOrder.LITTLE_ENDIAN).putLong(worldSeed).array();
        try {
            final byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
            return ByteBuffer.wrap(digest, 0, Long.BYTES).order(ByteOrder.LITTLE_ENDIAN).getLong();
        } catch (final NoSuchAlgorithmException missing) {
            throw new IllegalStateException("SHA-256 is not available in this Java runtime", missing);
        }
    }

    private static long step(final long state, final long input) {
        return state * (state * MULTIPLIER + INCREMENT) + input;
    }

    private static double offset(final long state) {
        final double fraction = Math.floorMod(state >> 24, JITTER_STEPS) / (double) JITTER_STEPS;
        return (fraction - 0.5) * JITTER_RANGE;
    }

    private static double square(final double value) {
        return value * value;
    }

    /**
     * {@return the grid point of the cell holding a block coordinate, on one axis; the cell spans this point and
     * the next one}
     */
    public static int cellOf(final int block) {
        return (block - HALF_CELL) >> CELL_SHIFT;
    }

    /**
     * {@return which corner of its cell a block takes its biome from: {@link #FAR_X}, {@link #FAR_Y} and
     * {@link #FAR_Z} tell, for each axis, whether it is the point after {@link #cellOf(int)} rather than that one}
     */
    public int nearestCorner(final int blockX, final int blockY, final int blockZ) {
        final int cellX = cellOf(blockX);
        final int cellY = cellOf(blockY);
        final int cellZ = cellOf(blockZ);
        final double inX = ((blockX - HALF_CELL) & CELL_MASK) / CELL_SIZE;
        final double inY = ((blockY - HALF_CELL) & CELL_MASK) / CELL_SIZE;
        final double inZ = ((blockZ - HALF_CELL) & CELL_MASK) / CELL_SIZE;
        int nearest = 0;
        double nearestDistance = Double.POSITIVE_INFINITY;
        for (int corner = 0; corner < CORNERS; corner++) {
            final boolean farX = (corner & FAR_X) != 0;
            final boolean farY = (corner & FAR_Y) != 0;
            final boolean farZ = (corner & FAR_Z) != 0;
            final double distance = distanceTo(
                    farX ? cellX + 1 : cellX, farY ? cellY + 1 : cellY, farZ ? cellZ + 1 : cellZ,
                    farX ? inX - 1.0 : inX, farY ? inY - 1.0 : inY, farZ ? inZ - 1.0 : inZ);
            // on a tie the first corner stays
            if (nearestDistance > distance) {
                nearest = corner;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    /**
     * {@return the jitter steps of a box of grid points, one packed int per point: the step along x, y then z, ten
     * bits each}; read by {@link #nearestCorner(int, int, int, int[], int, int, int)}
     *
     * <p>Point {@code (x, y, z)} of the box, counted from its minimum, is at index
     * {@code (x * sizeZ + z) * sizeY + y}.</p>
     */
    public int[] jitterSteps(final int minX, final int minY, final int minZ, final int sizeX, final int sizeY, final int sizeZ) {
        final int[] steps = new int[sizeX * sizeY * sizeZ];
        for (int x = 0; x < sizeX; x++) {
            for (int z = 0; z < sizeZ; z++) {
                final int row = (x * sizeZ + z) * sizeY;
                for (int y = 0; y < sizeY; y++) {
                    steps[row + y] = packedSteps(minX + x, minY + y, minZ + z);
                }
            }
        }
        return steps;
    }

    /**
     * {@return the same as {@link #nearestCorner(int, int, int)}, with the jitter of the grid points read from
     * {@link #jitterSteps} instead of drawn again}
     *
     * @param steps   the jitter steps of a box holding the eight corners of the cell of the block
     * @param base    the index in {@code steps} of the corner before the block on the three axes
     * @param strideX how far the next point along x is in {@code steps}; the next point along y is the next index
     * @param strideZ the same along z
     */
    public int nearestCorner(final int blockX, final int blockY, final int blockZ, final int[] steps,
                             final int base, final int strideX, final int strideZ) {
        final double inX = ((blockX - HALF_CELL) & CELL_MASK) / CELL_SIZE;
        final double inY = ((blockY - HALF_CELL) & CELL_MASK) / CELL_SIZE;
        final double inZ = ((blockZ - HALF_CELL) & CELL_MASK) / CELL_SIZE;
        int nearest = 0;
        double nearestDistance = Double.POSITIVE_INFINITY;
        for (int corner = 0; corner < CORNERS; corner++) {
            final boolean farX = (corner & FAR_X) != 0;
            final boolean farY = (corner & FAR_Y) != 0;
            final boolean farZ = (corner & FAR_Z) != 0;
            final int packed = steps[base + (farX ? strideX : 0) + (farY ? 1 : 0) + (farZ ? strideZ : 0)];
            final double towardsX = farX ? inX - 1.0 : inX;
            final double towardsY = farY ? inY - 1.0 : inY;
            final double towardsZ = farZ ? inZ - 1.0 : inZ;
            // the same sum, in the same order, as distanceTo: the same distances to the last bit, so the same ties
            final double distance = square(towardsZ + OFFSETS[packed & STEP_MASK])
                    + square(towardsY + OFFSETS[(packed >>> STEP_BITS) & STEP_MASK])
                    + square(towardsX + OFFSETS[packed >>> (2 * STEP_BITS)]);
            if (nearestDistance > distance) {
                nearest = corner;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    private int packedSteps(final int pointX, final int pointY, final int pointZ) {
        long state = seed;
        state = step(state, pointX);
        state = step(state, pointY);
        state = step(state, pointZ);
        state = step(state, pointX);
        state = step(state, pointY);
        state = step(state, pointZ);
        final int stepX = Math.floorMod(state >> 24, JITTER_STEPS);
        state = step(state, seed);
        final int stepY = Math.floorMod(state >> 24, JITTER_STEPS);
        state = step(state, seed);
        final int stepZ = Math.floorMod(state >> 24, JITTER_STEPS);
        return stepX << (2 * STEP_BITS) | stepY << STEP_BITS | stepZ;
    }

    /**
     * {@return the squared distance from a block to a moved grid point}
     *
     * @param towardsX where the block is from the point before it was moved, in cells
     */
    private double distanceTo(final int pointX, final int pointY, final int pointZ,
                              final double towardsX, final double towardsY, final double towardsZ) {
        long state = seed;
        state = step(state, pointX);
        state = step(state, pointY);
        state = step(state, pointZ);
        state = step(state, pointX);
        state = step(state, pointY);
        state = step(state, pointZ);
        final double offsetX = offset(state);
        state = step(state, seed);
        final double offsetY = offset(state);
        state = step(state, seed);
        final double offsetZ = offset(state);
        return square(towardsZ + offsetZ) + square(towardsY + offsetY) + square(towardsX + offsetX);
    }
}
