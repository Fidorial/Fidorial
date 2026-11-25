package fr.euphyllia.fidorial.server.world.generation.noise;

import fr.euphyllia.fidorial.server.world.generation.random.BitRandom;

/**
 * One octave of three-dimensional gradient noise over a randomly shifted and shuffled lattice. The lattice is drawn
 * from the stream given to the constructor: three doubles for its origin, then the shuffle.
 *
 * <p>Coordinates far from the origin are folded back before use, so that the fractional part keeps its
 * precision. Everything past the folding is single precision.</p>
 */
public class PerlinOctave implements NoiseField {

    private static final double ORIGIN_RANGE = 256.0;
    private static final double FOLD_PERIOD = 3.3554432E7;
    private static final double FOLD_LIMIT = Math.nextDown(1.6777216E7);
    private static final float BOUND = 2.0F;

    protected final NoiseLattice lattice;

    public PerlinOctave(final BitRandom random) {
        this.lattice = new NoiseLattice(random, ORIGIN_RANGE);
    }

    /**
     * {@return a coordinate folded into the range where a double still resolves small fractions}
     */
    protected static double fold(final double value) {
        if (value >= -FOLD_LIMIT && value < FOLD_LIMIT) {
            return value;
        }
        return value - Math.floor(value / FOLD_PERIOD + 0.5) * FOLD_PERIOD;
    }

    protected static float ease(final float t) {
        return t * t * t * (t * (t * 6.0F - 15.0F) + 10.0F);
    }

    protected static float mix(final float amount, final float from, final float to) {
        return from + amount * (to - from);
    }

    private static float dot(final int gradient, final float x, final float y, final float z) {
        return NoiseLattice.gradientX(gradient) * x + NoiseLattice.gradientY(gradient) * y + NoiseLattice.gradientZ(gradient) * z;
    }

    @Override
    public float bound() {
        return BOUND;
    }

    @Override
    public float sample(final double x, final double y, final double z) {
        final double px = fold(x) + lattice.originX();
        final double py = fold(y) + lattice.originY();
        final double pz = fold(z) + lattice.originZ();
        final int cellX = (int) Math.floor(px);
        final int cellY = (int) Math.floor(py);
        final int cellZ = (int) Math.floor(pz);
        final float fy = (float) (py - cellY);
        return blend(cellX, cellY, cellZ, (float) (px - cellX), fy, (float) (pz - cellZ), fy);
    }

    /**
     * {@return the noise of a two-dimensional plane, the second coordinate being the third axis}
     */
    public float sample2d(final double x, final double z) {
        return sample(fold(x), 0.0, fold(z));
    }

    /**
     * Interpolates the eight lattice corners around a point.
     *
     * @param inY      where the point is inside its cell vertically, as seen by the gradients
     * @param easeFrom where it is for the vertical blend; differs from {@code inY} for smeared noises only
     */
    protected final float blend(final int cellX, final int cellY, final int cellZ,
                                final float inX, final float inY, final float inZ, final float easeFrom) {
        final int west = lattice.hash(cellX);
        final int east = lattice.hash(cellX + 1);
        final int westLow = lattice.hash(west + cellY);
        final int westHigh = lattice.hash(west + cellY + 1);
        final int eastLow = lattice.hash(east + cellY);
        final int eastHigh = lattice.hash(east + cellY + 1);

        final float outX = inX - 1.0F;
        final float outY = inY - 1.0F;
        final float outZ = inZ - 1.0F;
        final float c000 = dot(lattice.gradient(westLow + cellZ), inX, inY, inZ);
        final float c100 = dot(lattice.gradient(eastLow + cellZ), outX, inY, inZ);
        final float c010 = dot(lattice.gradient(westHigh + cellZ), inX, outY, inZ);
        final float c110 = dot(lattice.gradient(eastHigh + cellZ), outX, outY, inZ);
        final float c001 = dot(lattice.gradient(westLow + cellZ + 1), inX, inY, outZ);
        final float c101 = dot(lattice.gradient(eastLow + cellZ + 1), outX, inY, outZ);
        final float c011 = dot(lattice.gradient(westHigh + cellZ + 1), inX, outY, outZ);
        final float c111 = dot(lattice.gradient(eastHigh + cellZ + 1), outX, outY, outZ);

        final float tx = ease(inX);
        final float ty = ease(easeFrom);
        final float tz = ease(inZ);
        final float near = mix(ty, mix(tx, c000, c100), mix(tx, c010, c110));
        final float far = mix(ty, mix(tx, c001, c101), mix(tx, c011, c111));
        return mix(tz, near, far);
    }
}