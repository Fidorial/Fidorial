package fr.euphyllia.fidorial.server.world.generation.noise;

import fr.euphyllia.fidorial.server.world.generation.random.BitRandom;

/**
 * Three-dimensional gradient noise sampled at block positions, whose vertical gradients move in steps instead of
 * continuously. The stepping stretches the features along the vertical axis, which gives terraced, layered shapes
 * rather than round blobs.
 */
public final class SmearedPerlinNoise {

    private static final double ORIGIN_RANGE = 256.0;
    // far from the origin the coordinates are folded back, so the fractional part keeps its precision
    private static final double WRAP_PERIOD = 3.3554432E7;
    private static final double WRAP_LIMIT = Math.nextDown(WRAP_PERIOD / 2.0);
    // keeps a height that sits exactly on a step from falling into the step below
    private static final double STEP_EPSILON = 1.0E-7F;

    private final NoiseLattice lattice;
    private final double smearStep;

    /**
     * @param random    the stream the lattice is drawn from
     * @param smearStep the height of a vertical step, in noise units
     */
    public SmearedPerlinNoise(final BitRandom random, final double smearStep) {
        this.lattice = new NoiseLattice(random, ORIGIN_RANGE);
        this.smearStep = smearStep;
    }

    private static double wrap(final double value) {
        if (value >= -WRAP_LIMIT && value < WRAP_LIMIT) {
            return value;
        }
        return value - Math.floor(value / WRAP_PERIOD + 0.5) * WRAP_PERIOD;
    }

    private static float fade(final float t) {
        return t * t * t * (t * (t * 6.0F - 15.0F) + 10.0F);
    }

    private static float lerp(final float amount, final float from, final float to) {
        return from + amount * (to - from);
    }

    private static float horizontal(final int gradient, final float x, final float z) {
        return NoiseLattice.gradientX(gradient) * x + NoiseLattice.gradientZ(gradient) * z;
    }

    /**
     * {@return the noise at a block}
     *
     * @param horizontalScale what one block is worth in noise units, along x and z
     * @param verticalScale   the same along y
     */
    public float sample(final int blockX, final int blockY, final int blockZ, final double horizontalScale, final double verticalScale) {
        final double z = wrap(blockZ * horizontalScale) + lattice.originZ();
        final int cellZ = (int) Math.floor(z);
        final float inZ = (float) (z - cellZ);

        final double x = wrap(blockX * horizontalScale) + lattice.originX();
        final int cellX = (int) Math.floor(x);
        final float inX = (float) (x - cellX);

        final double scaledY = blockY * verticalScale;
        final double y = wrap(scaledY) + lattice.originY();
        final int cellY = (int) Math.floor(y);
        final double inYExact = y - cellY;

        final int west = lattice.hash(cellX);
        final int east = lattice.hash(cellX + 1);
        final int westLow = lattice.hash(west + cellY);
        final int westHigh = lattice.hash(west + cellY + 1);
        final int eastLow = lattice.hash(east + cellY);
        final int eastHigh = lattice.hash(east + cellY + 1);

        final int g000 = lattice.gradient(westLow + cellZ);
        final int g100 = lattice.gradient(eastLow + cellZ);
        final int g010 = lattice.gradient(westHigh + cellZ);
        final int g110 = lattice.gradient(eastHigh + cellZ);
        final int g001 = lattice.gradient(westLow + cellZ + 1);
        final int g101 = lattice.gradient(eastLow + cellZ + 1);
        final int g011 = lattice.gradient(westHigh + cellZ + 1);
        final int g111 = lattice.gradient(eastHigh + cellZ + 1);

        // the gradients see a height that only moves by whole steps, the blend between corners follows the real one
        final float inY = (float) (inYExact - steppedHeight(scaledY, inYExact));
        final float aboveX = inX - 1.0F;
        final float aboveY = inY - 1.0F;
        final float aboveZ = inZ - 1.0F;

        final float v000 = horizontal(g000, inX, inZ) + NoiseLattice.gradientY(g000) * inY;
        final float v100 = horizontal(g100, aboveX, inZ) + NoiseLattice.gradientY(g100) * inY;
        final float v010 = horizontal(g010, inX, inZ) + NoiseLattice.gradientY(g010) * aboveY;
        final float v110 = horizontal(g110, aboveX, inZ) + NoiseLattice.gradientY(g110) * aboveY;
        final float v001 = horizontal(g001, inX, aboveZ) + NoiseLattice.gradientY(g001) * inY;
        final float v101 = horizontal(g101, aboveX, aboveZ) + NoiseLattice.gradientY(g101) * inY;
        final float v011 = horizontal(g011, inX, aboveZ) + NoiseLattice.gradientY(g011) * aboveY;
        final float v111 = horizontal(g111, aboveX, aboveZ) + NoiseLattice.gradientY(g111) * aboveY;

        final float fadeX = fade(inX);
        final float fadeY = fade((float) inYExact);
        final float fadeZ = fade(inZ);
        final float near = lerp(fadeY, lerp(fadeX, v000, v100), lerp(fadeX, v010, v110));
        final float far = lerp(fadeY, lerp(fadeX, v001, v101), lerp(fadeX, v011, v111));
        return lerp(fadeZ, near, far);
    }

    private double steppedHeight(final double scaledY, final double inCell) {
        final double reference = (scaledY >= 0.0 && scaledY < inCell) ? scaledY : inCell;
        return (int) Math.floor(reference / smearStep + STEP_EPSILON) * smearStep;
    }
}
