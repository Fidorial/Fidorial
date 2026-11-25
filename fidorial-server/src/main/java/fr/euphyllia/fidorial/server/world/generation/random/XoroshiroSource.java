package fr.euphyllia.fidorial.server.world.generation.random;

/**
 * A xoroshiro128++ stream whose values come straight from its 64-bit outputs: an integer is the low half of one
 * output, a bounded integer uses the multiply-and-shift method, a double the top 53 bits of one output.
 *
 * <p>This differs from {@link XoroshiroRandom}, which feeds the same generator through the derivations of the
 * 48-bit generator: the noises, the aquifers and the material rules draw from this one, the decorations from the
 * other. Not thread-safe.</p>
 */
public final class XoroshiroSource extends BitRandom {

    private static final float FLOAT_UNIT = 0x1.0p-24F;
    private static final double DOUBLE_UNIT = 0x1.0p-53;
    private static final long UNSIGNED_INT = 0xFFFFFFFFL;

    private long low;
    private long high;

    /**
     * @param seed a 64-bit seed, spread over the 128 bits of state
     */
    public XoroshiroSource(final long seed) {
        setSeed(seed);
    }

    /**
     * @param low  the low half of the state, used as given
     * @param high the high half of the state, used as given
     */
    public XoroshiroSource(final long low, final long high) {
        setState(low, high);
    }

    private void setState(final long newLow, final long newHigh) {
        if ((newLow | newHigh) == 0L) {
            // an all-zero state would only ever produce zeroes
            this.low = Seeds.GOLDEN_RATIO_64;
            this.high = Seeds.SILVER_RATIO_64;
        } else {
            this.low = newLow;
            this.high = newHigh;
        }
    }

    @Override
    public void setSeed(final long seed) {
        final long unmixedLow = seed ^ Seeds.SILVER_RATIO_64;
        final long unmixedHigh = unmixedLow + Seeds.GOLDEN_RATIO_64;
        setState(Seeds.mix(unmixedLow), Seeds.mix(unmixedHigh));
        dropSpareGaussian();
    }

    /**
     * {@return the next 64 bits of the generator}
     */
    public long nextRaw() {
        final long s0 = low;
        long s1 = high;
        final long result = Long.rotateLeft(s0 + s1, 17) + s0;
        s1 ^= s0;
        this.low = Long.rotateLeft(s0, 49) ^ s1 ^ s1 << 21;
        this.high = Long.rotateLeft(s1, 28);
        return result;
    }

    @Override
    protected int next(final int bits) {
        return (int) (nextRaw() >>> (64 - bits));
    }

    @Override
    public int nextInt() {
        return (int) nextRaw();
    }

    @Override
    public int nextInt(final int bound) {
        if (bound <= 0) {
            throw new IllegalArgumentException("Bound must be positive");
        }
        // scale a 32-bit draw to the bound; the few draws that would favour some results are thrown away
        long scaled = (nextInt() & UNSIGNED_INT) * bound;
        long fraction = scaled & UNSIGNED_INT;
        if (fraction < bound) {
            final int threshold = Integer.remainderUnsigned(-bound, bound);
            while (fraction < threshold) {
                scaled = (nextInt() & UNSIGNED_INT) * bound;
                fraction = scaled & UNSIGNED_INT;
            }
        }
        return (int) (scaled >> 32);
    }

    @Override
    public long nextLong() {
        return nextRaw();
    }

    @Override
    public boolean nextBoolean() {
        return (nextRaw() & 1L) != 0L;
    }

    @Override
    public float nextFloat() {
        return (nextRaw() >>> 40) * FLOAT_UNIT;
    }

    @Override
    public double nextDouble() {
        return (nextRaw() >>> 11) * DOUBLE_UNIT;
    }

    @Override
    public void skip(final int count) {
        for (int i = 0; i < count; i++) {
            nextRaw();
        }
    }

    /**
     * {@return an independent stream seeded from the next two outputs of this one}
     */
    public XoroshiroSource split() {
        return new XoroshiroSource(nextRaw(), nextRaw());
    }

    /**
     * {@return a factory of streams tied to positions or names, seeded from the next two outputs of this one}
     */
    public PositionalRandom splitPositional() {
        return new PositionalRandom(nextRaw(), nextRaw());
    }
}
