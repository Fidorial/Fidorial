package fr.euphyllia.fidorial.server.world.generation.noise;

import fr.euphyllia.fidorial.server.world.generation.random.BitRandom;

/**
 * A gradient noise octave whose vertical gradients only see the height in whole steps. The vertical blend still
 * follows the real height, which stretches the shapes into layers. Used by the blended terrain noise.
 */
public final class SmearedOctave extends PerlinOctave {

    // keeps a height lying exactly on a step from slipping into the step below
    private static final double STEP_EPSILON = 1.0E-7F;

    private final double step;

    /**
     * @param random the stream the lattice is drawn from
     * @param step   the height of a step, in noise units
     */
    public SmearedOctave(final BitRandom random, final double step) {
        super(random);
        this.step = step;
    }

    @Override
    public float bound() {
        return (float) (Math.abs(step) + 2.0);
    }

    @Override
    public float sample(final double x, final double y, final double z) {
        final double px = fold(x) + lattice.originX();
        final double py = fold(y) + lattice.originY();
        final double pz = fold(z) + lattice.originZ();
        final int cellX = (int) Math.floor(px);
        final int cellY = (int) Math.floor(py);
        final int cellZ = (int) Math.floor(pz);
        final double inY = py - cellY;
        final float stepped = (float) (inY - steppedOffset(y, inY));
        return blend(cellX, cellY, cellZ, (float) (px - cellX), stepped, (float) (pz - cellZ), (float) inY);
    }

    private double steppedOffset(final double unfoldedY, final double inCell) {
        final double reference = unfoldedY >= 0.0 && unfoldedY < inCell ? unfoldedY : inCell;
        return Math.floor(reference / step + STEP_EPSILON) * step;
    }
}
