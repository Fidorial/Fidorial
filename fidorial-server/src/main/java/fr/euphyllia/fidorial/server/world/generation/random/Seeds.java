package fr.euphyllia.fidorial.server.world.generation.random;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * The arithmetic shared by the seeded streams of world generation.
 */
public final class Seeds {

    /**
     * 2<sup>64</sup> divided by the golden ratio, an odd constant with well spread bits.
     */
    public static final long GOLDEN_RATIO_64 = -7046029254386353131L;
    /**
     * 2<sup>64</sup> divided by the silver ratio.
     */
    public static final long SILVER_RATIO_64 = 7640891576956012809L;

    private Seeds() {
    }

    /**
     * {@return the value with its bits avalanched}: every input bit flips about half of the output bits.
     */
    public static long mix(final long value) {
        long mixed = (value ^ value >>> 30) * -4658895280553007687L;
        mixed = (mixed ^ mixed >>> 27) * -7723592293110705685L;
        return mixed ^ mixed >>> 31;
    }

    /**
     * {@return the seed of a block position}: close positions give unrelated seeds.
     */
    public static long ofPosition(final int x, final int y, final int z) {
        // the x factor is applied in 32 bits on purpose: the overflow is part of the result
        long seed = (long) (x * 3129871) ^ (long) z * 116129781L ^ (long) y;
        seed = seed * seed * 42317861L + seed * 11L;
        return seed >> 16;
    }

    /**
     * {@return the 128 bits of the MD5 digest of a name, as two longs read big-endian: low half first}
     */
    public static long[] ofName(final String name) {
        final byte[] digest;
        try {
            digest = MessageDigest.getInstance("MD5").digest(name.getBytes(StandardCharsets.UTF_8));
        } catch (final NoSuchAlgorithmException missing) {
            throw new IllegalStateException("MD5 is not available in this Java runtime", missing);
        }
        return new long[]{readLong(digest, 0), readLong(digest, 8)};
    }

    private static long readLong(final byte[] bytes, final int offset) {
        long value = 0L;
        for (int i = 0; i < Long.BYTES; i++) {
            value = value << 8 | (bytes[offset + i] & 0xFFL);
        }
        return value;
    }
}
