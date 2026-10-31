package fr.euphyllia.fidorial.testplugin.terrain;

import java.util.Random;

public final class PerlinNoise {

    private final int[] perm = new int[512];

    public PerlinNoise(final long seed) {
        final int[] p = new int[256];
        for (int i = 0; i < 256; i++) {
            p[i] = i;
        }
        final Random random = new Random(seed);
        for (int i = 255; i > 0; i--) {
            final int j = random.nextInt(i + 1);
            final int tmp = p[i];
            p[i] = p[j];
            p[j] = tmp;
        }
        for (int i = 0; i < 512; i++) {
            perm[i] = p[i & 255];
        }
    }

    private static double fade(final double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private static double lerp(final double a, final double b, final double t) {
        return a + t * (b - a);
    }

    private static double grad(final int hash, final double x, final double y) {
        return switch (hash & 3) {
            case 0 -> x + y;
            case 1 -> -x + y;
            case 2 -> x - y;
            default -> -x - y;
        };
    }

    public double noise(final double x, final double y) {
        final int xi = (int) Math.floor(x) & 255;
        final int yi = (int) Math.floor(y) & 255;
        final double xf = x - Math.floor(x);
        final double yf = y - Math.floor(y);

        final double u = fade(xf);
        final double v = fade(yf);

        final int aa = perm[perm[xi] + yi];
        final int ab = perm[perm[xi] + yi + 1];
        final int ba = perm[perm[xi + 1] + yi];
        final int bb = perm[perm[xi + 1] + yi + 1];

        final double x1 = lerp(grad(aa, xf, yf), grad(ba, xf - 1, yf), u);
        final double x2 = lerp(grad(ab, xf, yf - 1), grad(bb, xf - 1, yf - 1), u);
        return lerp(x1, x2, v);
    }

    public double fbm(final double x, final double y, final int octaves) {
        double total = 0;
        double amplitude = 1;
        double frequency = 1;
        double max = 0;
        for (int i = 0; i < octaves; i++) {
            total += noise(x * frequency, y * frequency) * amplitude;
            max += amplitude;
            amplitude *= 0.5;
            frequency *= 2;
        }
        return total / max;
    }
}
