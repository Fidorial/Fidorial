package fr.euphyllia.fidorial.server.world.generation.density;

import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;

import java.util.Arrays;

/**
 * Samples a function on a coarse grid and fills the blocks in between. Each axis has its own cell size and its
 * own way of filling: copy the nearest sample, blend the two samples around linearly or along a cosine, or follow
 * a cubic through four samples.
 *
 * <p>The grid is anchored on the multiples of the cell sizes, so that neighbouring chunks share their samples.
 * The samples are kept in the {@link DensityContext}: a chunk computes each of them once. The blend goes along x
 * first, then z, then y.</p>
 */
final class UpscaleDensity extends Density {

    /**
     * How the blocks between two grid points are filled along one axis.
     */
    enum Mode {
        NEAREST_NEIGHBOR(0, 0, true),
        INTERPOLATE_LINEAR(0, 1, true),
        INTERPOLATE_COSINE(0, 1, true),
        INTERPOLATE_CUBIC(1, 2, true),
        B_SPLINE_CUBIC(1, 2, false);

        final int before;
        final int after;
        final boolean exactOnGrid;

        Mode(final int before, final int after, final boolean exactOnGrid) {
            this.before = before;
            this.after = after;
            this.exactOnGrid = exactOnGrid;
        }

        int samples() {
            return before + 1 + after;
        }

        /**
         * {@return the weight of each sample for a block a fraction of the way through its cell}
         */
        float[] weights(final float t) {
            return switch (this) {
                case NEAREST_NEIGHBOR -> new float[]{1.0F};
                case INTERPOLATE_LINEAR -> new float[]{1.0F - t, t};
                case INTERPOLATE_COSINE -> {
                    final float eased = (float) (-(Math.cos(Math.PI * t) - 1.0) / 2.0);
                    yield new float[]{1.0F - eased, eased};
                }
                case INTERPOLATE_CUBIC -> {
                    final float t2 = t * t;
                    final float t3 = t2 * t;
                    yield new float[]{
                            -0.5F * t3 + t2 - 0.5F * t,
                            1.5F * t3 - 2.5F * t2 + 1.0F,
                            -1.5F * t3 + 2.0F * t2 + 0.5F * t,
                            0.5F * t3 - 0.5F * t2
                    };
                }
                case B_SPLINE_CUBIC -> {
                    final float t2 = t * t;
                    final float t3 = t2 * t;
                    final float u = 1.0F - t;
                    yield new float[]{
                            u * u * u / 6.0F,
                            (3.0F * t3 - 6.0F * t2 + 4.0F) / 6.0F,
                            (-3.0F * t3 + 3.0F * t2 + 3.0F * t + 1.0F) / 6.0F,
                            t3 / 6.0F
                    };
                }
            };
        }

        /**
         * {@return how much the blend can reach beyond the samples, as a share of their spread}
         */
        float overshoot() {
            return this == INTERPOLATE_CUBIC ? 0.25F : 0.0F;
        }
    }

    /**
     * One axis of the grid.
     *
     * @param mode     how the axis is filled
     * @param cellSize the distance between two samples, in blocks
     */
    record Axis(Mode mode, int cellSize) {

        float[][] table() {
            final float[][] table = new float[cellSize][];
            for (int i = 0; i < cellSize; i++) {
                table[i] = mode.weights((float) i / cellSize);
            }
            return table;
        }
    }

    /**
     * The eight grid samples around the cell an upscaled function blended last, NaN for those not read yet: the
     * blocks of a column go through a cell one after the other, and all read the same corners. The samples depend
     * on the position only, so they stay right from one chunk to the next. Belongs to a {@link DensityContext}.
     */
    static final class CellCorners {
        boolean valid;
        int cellX;
        int cellY;
        int cellZ;
        // by corner: 1 for the far side along x, 2 along y, 4 along z
        final float[] values = new float[8];
    }

    private final Density input;
    private final int slot;
    private final Axis x;
    private final Axis y;
    private final Axis z;
    private final float[][] weightsX;
    private final float[][] weightsZ;
    private final float[][] weightsY;
    private final float inverseCellY;
    private final boolean linearOrNearest;
    // the cell size minus one when it is a power of two, the position in the cell then being a mask away; -1 otherwise
    private final int maskX;
    private final int maskY;
    private final int maskZ;

    UpscaleDensity(final Density input, final int slot, final Axis x, final Axis y, final Axis z) {
        super(widen(input, x, y, z)[0], widen(input, x, y, z)[1]);
        this.input = input;
        this.slot = slot;
        this.x = x;
        this.y = y;
        this.z = z;
        this.weightsX = x.table();
        this.weightsY = y.table();
        this.weightsZ = z.table();
        this.inverseCellY = 1.0F / y.cellSize();
        this.linearOrNearest = simple(x.mode()) && simple(y.mode()) && simple(z.mode());
        this.maskX = maskOf(x.cellSize());
        this.maskY = maskOf(y.cellSize());
        this.maskZ = maskOf(z.cellSize());
    }

    private static int maskOf(final int size) {
        return Integer.bitCount(size) == 1 ? size - 1 : -1;
    }

    /**
     * {@return {@code Math.floorMod(block, size)}, without a division when the size is a power of two}
     */
    private static int inCell(final int block, final int size, final int mask) {
        return mask >= 0 ? block & mask : Math.floorMod(block, size);
    }

    private static final float[] ONE = {1.0F};

    private static boolean simple(final Mode mode) {
        return mode == Mode.NEAREST_NEIGHBOR || mode == Mode.INTERPOLATE_LINEAR || mode == Mode.INTERPOLATE_COSINE;
    }

    private static float[] widen(final Density input, final Axis x, final Axis y, final Axis z) {
        final float spread = input.max() - input.min();
        final float overshoot = x.mode().overshoot() + y.mode().overshoot() + z.mode().overshoot();
        return new float[]{input.min() - spread * overshoot, input.max() + spread * overshoot};
    }

    @Override
    public int axes() {
        return input.axes();
    }

    private float sampleAt(final DensityContext context, final Long2FloatOpenHashMap grid, final int bx, final int by, final int bz) {
        final long key = DensityContext.blockKey(bx, by, bz);
        final float known = grid.get(key);
        if (!Float.isNaN(known)) {
            return known;
        }
        final float value = input.compute(context, bx, by, bz);
        if (!Float.isNaN(value)) {
            grid.put(key, value);
        }
        return value;
    }

    @Override
    public float compute(final DensityContext context, final int bx, final int by, final int bz) {
        final int sizeX = x.cellSize();
        final int sizeY = y.cellSize();
        final int sizeZ = z.cellSize();
        final int inX = inCell(bx, sizeX, maskX);
        final int inY = inCell(by, sizeY, maskY);
        final int inZ = inCell(bz, sizeZ, maskZ);
        // on a grid line an exact mode needs the sample of that line only
        final boolean onX = inX == 0 && x.mode().exactOnGrid;
        final boolean onY = inY == 0 && y.mode().exactOnGrid;
        final boolean onZ = inZ == 0 && z.mode().exactOnGrid;
        if (onX && onY && onZ) {
            return sampleAt(context, context.grid(slot), bx, by, bz);
        }
        if (linearOrNearest) {
            return trilinear(context, bx - inX, by - inY, bz - inZ, inX, inY, inZ, onX, onY, onZ);
        }
        return general(context, bx, by, bz, inX, inY, inZ, onX, onY, onZ);
    }

    /**
     * The common case: every axis copies or blends two samples, eight samples at most.
     */
    private float trilinear(final DensityContext context, final int cellX, final int cellY, final int cellZ,
                            final int inX, final int inY, final int inZ,
                            final boolean onX, final boolean onY, final boolean onZ) {
        final Long2FloatOpenHashMap grid = context.grid(slot);
        final CellCorners corners = context.corners(slot);
        if (!corners.valid || corners.cellX != cellX || corners.cellY != cellY || corners.cellZ != cellZ) {
            corners.valid = true;
            corners.cellX = cellX;
            corners.cellY = cellY;
            corners.cellZ = cellZ;
            Arrays.fill(corners.values, Float.NaN);
        }
        final boolean singleX = onX || x.mode() == Mode.NEAREST_NEIGHBOR;
        final boolean singleY = onY || y.mode() == Mode.NEAREST_NEIGHBOR;
        final boolean singleZ = onZ || z.mode() == Mode.NEAREST_NEIGHBOR;
        final int nextX = cellX + x.cellSize();
        final int nextY = cellY + y.cellSize();
        final int nextZ = cellZ + z.cellSize();
        final float[] wx = weightsX[inX];
        final float[] wz = weightsZ[inZ];

        final float low = plane(context, grid, corners, 0, cellX, nextX, cellY, cellZ, nextZ, singleX, singleZ, wx, wz);
        if (singleY) {
            return low;
        }
        final float high = plane(context, grid, corners, 2, cellX, nextX, nextY, cellZ, nextZ, singleX, singleZ, wx, wz);
        if (y.mode() == Mode.INTERPOLATE_LINEAR) {
            return low + (high - low) * inverseCellY * inY;
        }
        final float[] wy = weightsY[inY];
        return wy[0] * low + wy[1] * high;
    }

    /**
     * @param corner the corner bit of {@code sampleY}: 0 for the near side, 2 for the far side
     */
    private float plane(final DensityContext context, final Long2FloatOpenHashMap grid, final CellCorners corners,
                        final int corner, final int cellX, final int nextX, final int sampleY, final int cellZ,
                        final int nextZ, final boolean singleX, final boolean singleZ, final float[] wx, final float[] wz) {
        final float near = line(context, grid, corners, corner, cellX, nextX, sampleY, cellZ, singleX, wx);
        if (singleZ) {
            return near;
        }
        final float far = line(context, grid, corners, corner | 4, cellX, nextX, sampleY, nextZ, singleX, wx);
        return wz[0] * near + wz[1] * far;
    }

    private float line(final DensityContext context, final Long2FloatOpenHashMap grid, final CellCorners corners,
                       final int corner, final int cellX, final int nextX, final int sampleY, final int sampleZ,
                       final boolean singleX, final float[] wx) {
        final float first = corner(context, grid, corners, corner, cellX, sampleY, sampleZ);
        if (singleX) {
            return first;
        }
        return wx[0] * first + wx[1] * corner(context, grid, corners, corner | 1, nextX, sampleY, sampleZ);
    }

    /**
     * {@return a corner of the cell, read from the grid the first time only}
     */
    private float corner(final DensityContext context, final Long2FloatOpenHashMap grid, final CellCorners corners,
                         final int corner, final int bx, final int by, final int bz) {
        final float known = corners.values[corner];
        return Float.isNaN(known) ? readCorner(context, grid, corners, corner, bx, by, bz) : known;
    }

    private float readCorner(final DensityContext context, final Long2FloatOpenHashMap grid, final CellCorners corners,
                             final int corner, final int bx, final int by, final int bz) {
        final float value = sampleAt(context, grid, bx, by, bz);
        corners.values[corner] = value;
        return value;
    }

    /**
     * Any combination of modes, cubic ones included.
     */
    private float general(final DensityContext context, final int bx, final int by, final int bz,
                          final int inX, final int inY, final int inZ,
                          final boolean onX, final boolean onY, final boolean onZ) {
        final Long2FloatOpenHashMap grid = context.grid(slot);
        final int sizeX = x.cellSize();
        final int sizeY = y.cellSize();
        final int sizeZ = z.cellSize();
        final int startX = bx - inX - (onX ? 0 : x.mode().before * sizeX);
        final int startY = by - inY - (onY ? 0 : y.mode().before * sizeY);
        final int startZ = bz - inZ - (onZ ? 0 : z.mode().before * sizeZ);
        final int countX = onX ? 1 : x.mode().samples();
        final int countY = onY ? 1 : y.mode().samples();
        final int countZ = onZ ? 1 : z.mode().samples();
        final float[] wx = onX ? ONE : weightsX[inX];
        final float[] wy = onY ? ONE : weightsY[inY];
        final float[] wz = onZ ? ONE : weightsZ[inZ];

        float result = 0.0F;
        for (int sy = 0; sy < countY; sy++) {
            final int sampleY = startY + sy * sizeY;
            float acrossZ = 0.0F;
            for (int sz = 0; sz < countZ; sz++) {
                final int sampleZ = startZ + sz * sizeZ;
                float alongX = 0.0F;
                for (int sx = 0; sx < countX; sx++) {
                    final float sample = sampleAt(context, grid, startX + sx * sizeX, sampleY, sampleZ);
                    alongX = sx == 0 ? wx[0] * sample : alongX + wx[sx] * sample;
                }
                acrossZ = sz == 0 ? wz[0] * alongX : acrossZ + wz[sz] * alongX;
            }
            result = sy == 0 ? wy[0] * acrossZ : result + wy[sy] * acrossZ;
        }
        return result;
    }
}
