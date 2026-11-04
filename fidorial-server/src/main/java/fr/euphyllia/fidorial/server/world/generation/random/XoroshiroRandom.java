package fr.euphyllia.fidorial.server.world.generation.random;

public final class XoroshiroRandom extends BitRandom {

    private static final long SILVER_RATIO_64 = 7640891576956012809L;
    private static final long GOLDEN_RATIO_64 = -7046029254386353131L;

    private long low;
    private long high;

    public XoroshiroRandom(final long seed) {
        setSeed(seed);
    }

    private static long mixStafford13(final long value) {
        long mixed = (value ^ value >>> 30) * -4658895280553007687L;
        mixed = (mixed ^ mixed >>> 27) * -7723592293110705685L;
        return mixed ^ mixed >>> 31;
    }

    @Override
    public void setSeed(final long seed) {
        final long unmixedLow = seed ^ SILVER_RATIO_64;
        final long unmixedHigh = unmixedLow + GOLDEN_RATIO_64;
        this.low = mixStafford13(unmixedLow);
        this.high = mixStafford13(unmixedHigh);
        if ((low | high) == 0L) {
            this.low = GOLDEN_RATIO_64;
            this.high = SILVER_RATIO_64;
        }
    }

    private long nextRaw() {
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

    /**
     * Seeds the stream for the decorations of the chunk whose minimum block is given.
     *
     * @param seed   the world seed
     * @param blockX the minimum block X of the chunk
     * @param blockZ the minimum block Z of the chunk
     * @return the decoration seed, to derive the per-feature seeds from
     */
    public long setDecorationSeed(final long seed, final int blockX, final int blockZ) {
        setSeed(seed);
        final long xScale = nextLong() | 1L;
        final long zScale = nextLong() | 1L;
        final long result = blockX * xScale + blockZ * zScale ^ seed;
        setSeed(result);
        return result;
    }

    /**
     * Seeds the stream for one decoration of a chunk.
     *
     * @param decorationSeed the seed returned by {@link #setDecorationSeed(long, int, int)}
     * @param index          the decoration index within its step
     * @param step           the generation step
     */
    public void setFeatureSeed(final long decorationSeed, final int index, final int step) {
        setSeed(decorationSeed + index + 10000L * step);
    }
}
