package fr.euphyllia.fidorial.server.world.structure.math;

import java.util.List;

public final class LegacyRandom {

    private static final long MULTIPLIER = 0x5DEECE66DL;
    private static final long ADDEND = 0xBL;
    private static final long MASK = (1L << 48) - 1;

    private long seed;

    public LegacyRandom(final long seed) {
        setSeed(seed);
    }

    public static long positionSeed(final int x, final int y, final int z) {
        long l = (long) (x * 3129871) ^ (long) z * 116129781L ^ (long) y;
        l = l * l * 42317861L + l * 11L;
        return l >> 16;
    }

    public static LegacyRandom atPosition(final int x, final int y, final int z) {
        return new LegacyRandom(positionSeed(x, y, z));
    }

    public void setSeed(final long newSeed) {
        this.seed = (newSeed ^ MULTIPLIER) & MASK;
    }

    public void setLargeFeatureSeed(final long worldSeed, final int chunkX, final int chunkZ) {
        setSeed(worldSeed);
        final long a = nextLong();
        final long b = nextLong();
        setSeed((long) chunkX * a ^ (long) chunkZ * b ^ worldSeed);
    }

    public void setLargeFeatureWithSalt(final long worldSeed, final int x, final int z, final int salt) {
        setSeed((long) x * 341873128712L + (long) z * 132897987541L + worldSeed + (long) salt);
    }

    private int next(final int bits) {
        seed = (seed * MULTIPLIER + ADDEND) & MASK;
        return (int) (seed >>> (48 - bits));
    }

    public int nextInt() {
        return next(32);
    }

    public int nextInt(final int bound) {
        if (bound <= 0) {
            throw new IllegalArgumentException("bound must be positive: " + bound);
        }
        int r = next(31);
        final int m = bound - 1;
        if ((bound & m) == 0) {
            return (int) ((bound * (long) r) >> 31);
        }
        for (int u = r; u - (r = u % bound) + m < 0; u = next(31)) {
            // rejection sampling
        }
        return r;
    }

    public int nextIntBetweenInclusive(final int min, final int max) {
        return nextInt(max - min + 1) + min;
    }

    public long nextLong() {
        return ((long) next(32) << 32) + next(32);
    }

    public boolean nextBoolean() {
        return next(1) != 0;
    }

    public float nextFloat() {
        return next(24) / (float) (1 << 24);
    }

    public double nextDouble() {
        return (((long) next(26) << 27) + next(27)) * 0x1.0p-53;
    }

    public <T> void shuffle(final List<T> list) {
        for (int i = list.size(); i > 1; i--) {
            final int j = nextInt(i);
            list.set(i - 1, list.set(j, list.get(i - 1)));
        }
    }
}
