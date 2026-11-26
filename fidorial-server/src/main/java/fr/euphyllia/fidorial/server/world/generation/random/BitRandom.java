package fr.euphyllia.fidorial.server.world.generation.random;

/**
 * A random stream that derives every value from {@link #next(int)}, with the same derivations as the classic
 * 48-bit linear congruential generator. Two streams fed with the same bits produce the same values, so world
 * generation code can work with any of them.
 */
public abstract class BitRandom {

    private static final float FLOAT_UNIT = 0x1.0p-24F;
    private static final double DOUBLE_UNIT = 0x1.0p-53;

    // the polar method draws two normal values at a time; the second one waits here
    private double spareGaussian;
    private boolean hasSpareGaussian;

    /**
     * {@return the next {@code bits} random bits, right-aligned}
     *
     * @param bits the number of bits, from 1 to 32
     */
    protected abstract int next(int bits);

    /**
     * Re-seeds the stream.
     *
     * @param seed the new seed
     */
    public abstract void setSeed(long seed);

    /**
     * {@return a factory of streams tied to positions or names, seeded from the next outputs of this stream}
     */
    public abstract PositionalRandom splitPositional();

    /**
     * Forgets the normal value kept by {@link #nextGaussian()}; implementations call it when they are re-seeded.
     */
    protected final void dropSpareGaussian() {
        this.hasSpareGaussian = false;
    }

    /**
     * {@return a value of the standard normal distribution}: pairs of uniform points are drawn in the square
     * around the origin until one falls inside the unit circle, which gives two values; the second is kept for
     * the next call.
     */
    public double nextGaussian() {
        if (hasSpareGaussian) {
            hasSpareGaussian = false;
            return spareGaussian;
        }
        while (true) {
            final double u = 2.0 * nextDouble() - 1.0;
            final double v = 2.0 * nextDouble() - 1.0;
            final double radius = u * u + v * v;
            if (radius < 1.0 && radius != 0.0) {
                final double scale = Math.sqrt(-2.0 * Math.log(radius) / radius);
                spareGaussian = v * scale;
                hasSpareGaussian = true;
                return u * scale;
            }
        }
    }

    public int nextInt() {
        return next(32);
    }

    public int nextInt(final int bound) {
        if (bound <= 0) {
            throw new IllegalArgumentException("Bound must be positive");
        }
        if ((bound & (bound - 1)) == 0) {
            return (int) (((long) bound * next(31)) >> 31);
        }
        int sample;
        int modulo;
        do {
            sample = next(31);
            modulo = sample % bound;
        } while (sample - modulo + (bound - 1) < 0);
        return modulo;
    }

    public int nextInt(final int origin, final int bound) {
        if (origin >= bound) {
            throw new IllegalArgumentException("bound - origin is non positive");
        }
        return origin + nextInt(bound - origin);
    }

    public int nextIntBetweenInclusive(final int min, final int maxInclusive) {
        return nextInt(maxInclusive - min + 1) + min;
    }

    /**
     * Like {@link #nextIntBetweenInclusive(int, int)}, except that a range of one value or less gives {@code min}
     * without drawing anything.
     */
    public int nextIntInRange(final int min, final int maxInclusive) {
        return min >= maxInclusive ? min : nextInt(maxInclusive - min + 1) + min;
    }

    public long nextLong() {
        final int upper = next(32);
        final int lower = next(32);
        return ((long) upper << 32) + lower;
    }

    public boolean nextBoolean() {
        return next(1) != 0;
    }

    public float nextFloat() {
        return next(24) * FLOAT_UNIT;
    }

    public double nextDouble() {
        final int upper = next(26);
        final int lower = next(27);
        return (((long) upper << 27) + lower) * DOUBLE_UNIT;
    }

    /**
     * Discards {@code count} integers.
     *
     * @param count how many integers to draw and drop
     */
    public void skip(final int count) {
        for (int i = 0; i < count; i++) {
            nextInt();
        }
    }
}
