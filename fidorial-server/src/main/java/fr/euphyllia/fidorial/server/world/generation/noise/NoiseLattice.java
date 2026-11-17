package fr.euphyllia.fidorial.server.world.generation.noise;

import fr.euphyllia.fidorial.server.world.generation.random.BitRandom;

/**
 * A shuffled table of 256 hashes with a random origin, the part every gradient noise of this package shares.
 *
 * <p>The origin is drawn first, one axis after the other, then the table is shuffled. Two lattices built from
 * generators in the same state are therefore identical, which is what makes a noise reproducible from a seed.</p>
 *
 * <p>Immutable once built, so it can be sampled from several threads.</p>
 */
public final class NoiseLattice {

    private static final int SIZE = 256;
    private static final int MASK = SIZE - 1;
    private static final int GRADIENT_MASK = 15;

    // The twelve edge directions of a cube, the last four repeating some of them to fill sixteen slots.
    private static final float[] GRADIENT_X = {1, -1, 1, -1, 1, -1, 1, -1, 0, 0, 0, 0, 1, 0, -1, 0};
    private static final float[] GRADIENT_Y = {1, 1, -1, -1, 0, 0, 0, 0, 1, -1, 1, -1, 1, -1, 1, -1};
    private static final float[] GRADIENT_Z = {0, 0, 0, 0, 1, 1, -1, -1, 1, 1, -1, -1, 0, 1, 0, -1};

    private final byte[] hashes = new byte[SIZE];
    private final double originX;
    private final double originY;
    private final double originZ;

    /**
     * @param random      the stream to draw the origin and the shuffle from
     * @param originRange the origin of each axis is drawn in {@code [0, originRange)}; with zero the lattice
     *                    stays on the coordinate origin, the three numbers being drawn all the same
     */
    public NoiseLattice(final BitRandom random, final double originRange) {
        this.originX = random.nextDouble() * originRange;
        this.originY = random.nextDouble() * originRange;
        this.originZ = random.nextDouble() * originRange;
        for (int i = 0; i < SIZE; i++) {
            hashes[i] = (byte) i;
        }
        for (int i = 0; i < SIZE; i++) {
            final int other = i + random.nextInt(SIZE - i);
            final byte swapped = hashes[i];
            hashes[i] = hashes[other];
            hashes[other] = swapped;
        }
    }

    public static float gradientX(final int gradient) {
        return GRADIENT_X[gradient];
    }

    public static float gradientY(final int gradient) {
        return GRADIENT_Y[gradient];
    }

    public static float gradientZ(final int gradient) {
        return GRADIENT_Z[gradient];
    }

    public double originX() {
        return originX;
    }

    public double originY() {
        return originY;
    }

    public double originZ() {
        return originZ;
    }

    /**
     * {@return the hash of a lattice index, from 0 to 255; the index wraps around the table}
     */
    public int hash(final int index) {
        return hashes[index & MASK] & MASK;
    }

    /**
     * {@return the slot of the gradient the lattice gives to an index}
     */
    public int gradient(final int index) {
        return hash(index) & GRADIENT_MASK;
    }
}
