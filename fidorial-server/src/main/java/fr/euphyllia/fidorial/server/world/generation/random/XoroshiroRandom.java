package fr.euphyllia.fidorial.server.world.generation.random;

public final class XoroshiroRandom extends BitRandom {

    private long low;
    private long high;

    public XoroshiroRandom(final long seed) {
        setSeed(seed);
    }

    @Override
    public void setSeed(final long seed) {
        reseed(seed);
        dropSpareGaussian();
    }

    /**
     * Restarts the stream from a seed but keeps the normal value left by {@link #nextGaussian()}: the decorations of
     * a chunk share it from one feature to the next.
     */
    private void reseed(final long seed) {
        final long unmixedLow = seed ^ Seeds.SILVER_RATIO_64;
        final long unmixedHigh = unmixedLow + Seeds.GOLDEN_RATIO_64;
        this.low = Seeds.mix(unmixedLow);
        this.high = Seeds.mix(unmixedHigh);
        if ((low | high) == 0L) {
            this.low = Seeds.GOLDEN_RATIO_64;
            this.high = Seeds.SILVER_RATIO_64;
        }
    }

    /**
     * {@return a factory of positional streams seeded from the next two raw outputs}
     */
    @Override
    public PositionalRandom splitPositional() {
        return new PositionalRandom.Xoroshiro(nextRaw(), nextRaw());
    }

    /**
     * {@return an independent stream seeded from the next two raw outputs}
     */
    public XoroshiroSource split() {
        return new XoroshiroSource(nextRaw(), nextRaw());
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
        reseed(seed);
        final long xScale = nextLong() | 1L;
        final long zScale = nextLong() | 1L;
        final long result = blockX * xScale + blockZ * zScale ^ seed;
        reseed(result);
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
        reseed(decorationSeed + index + 10000L * step);
    }
}
