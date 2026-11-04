package fr.euphyllia.fidorial.server.world.structure.math;

import fr.euphyllia.fidorial.server.world.generation.random.BitRandom;

import java.util.List;

/**
 * The 48-bit linear congruential generator behind structures and the legacy world generation streams.
 * Not thread-safe.
 */
public final class LegacyRandom extends BitRandom {

    private static final long MULTIPLIER = 0x5DEECE66DL;
    private static final long ADDEND = 0xBL;
    private static final long MASK = (1L << 48) - 1;

    private long seed;

    public LegacyRandom(final long seed) {
        setSeed(seed);
    }

    public static long positionSeed(final int x, final int y, final int z) {
        long l = (x * 3129871L) ^ (long) z * 116129781L ^ (long) y;
        l = l * l * 42317861L + l * 11L;
        return l >> 16;
    }

    public static LegacyRandom atPosition(final int x, final int y, final int z) {
        return new LegacyRandom(positionSeed(x, y, z));
    }

    @Override
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

    @Override
    protected int next(final int bits) {
        seed = (seed * MULTIPLIER + ADDEND) & MASK;
        return (int) (seed >>> (48 - bits));
    }

    public <T> void shuffle(final List<T> list) {
        for (int i = list.size(); i > 1; i--) {
            final int j = nextInt(i);
            list.set(i - 1, list.set(j, list.get(i - 1)));
        }
    }
}
