package fr.euphyllia.fidorial.server.world.structure.processor;

import fr.euphyllia.fidorial.server.world.structure.math.LegacyRandom;

public interface PosRuleTest {

    PosRuleTest ALWAYS_TRUE = (lx, ly, lz, wx, wy, wz, px, py, pz, random) -> true;

    boolean test(int lx, int ly, int lz, int wx, int wy, int wz, int px, int py, int pz, LegacyRandom random);

    private static float clampedLerp(final float start, final float end, final float delta) {
        if (delta < 0.0F) {
            return start;
        }
        return delta > 1.0F ? end : start + delta * (end - start);
    }

    private static float inverseLerp(final float value, final float start, final float end) {
        return (value - start) / (end - start);
    }

    static PosRuleTest linear(final float minChance, final float maxChance, final int minDist, final int maxDist) {
        return (lx, ly, lz, wx, wy, wz, px, py, pz, random) -> {
            final int distance = Math.abs(wx - px) + Math.abs(wy - py) + Math.abs(wz - pz);
            final float roll = random.nextFloat();
            return roll <= clampedLerp(minChance, maxChance, inverseLerp(distance, minDist, maxDist));
        };
    }

    static PosRuleTest axisAligned(final char axis, final float minChance, final float maxChance,
                                   final int minDist, final int maxDist) {
        return (lx, ly, lz, wx, wy, wz, px, py, pz, random) -> {
            final int distance = switch (axis) {
                case 'x' -> Math.abs(px - wx);
                case 'z' -> Math.abs(pz - wz);
                default -> Math.abs(py - wy);
            };
            final float roll = random.nextFloat();
            return roll <= clampedLerp(minChance, maxChance, inverseLerp(distance, minDist, maxDist));
        };
    }
}
