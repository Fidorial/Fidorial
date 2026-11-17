package fr.euphyllia.fidorial.server.world.generation.end;

import fr.euphyllia.fidorial.server.world.generation.noise.NoiseOctaves;
import fr.euphyllia.fidorial.server.world.generation.random.BitRandom;

/**
 * The noise that roughens the islands: two wide-ranging noises, and a third, smoother one that picks between them
 * from place to place. Where the selector sits at one end the terrain follows a single noise, in between it
 * slides from one to the other, which avoids both a uniform texture and hard seams.
 */
final class EndTerrainNoise {

    private static final double BASE_SCALE = 684.412;
    private static final double HORIZONTAL_SCALE = BASE_SCALE * 0.25;
    private static final double VERTICAL_SCALE = BASE_SCALE * 0.25;
    private static final double SMEAR_STEP = VERTICAL_SCALE * 4.0;
    private static final double SELECTOR_HORIZONTAL_STRETCH = 80.0;
    private static final double SELECTOR_VERTICAL_STRETCH = 160.0;

    private static final int BOUND_LAYERS = 16;
    private static final int SELECTOR_LAYERS = 8;
    // a hair under one: the widest layer weighs exactly a half
    private static final double BOUND_WEIGHT = 65535.0 / 65536.0;
    private static final double SELECTOR_WEIGHT = 12.75;
    private static final float SELECTOR_OFFSET = 0.5F;

    private final NoiseOctaves lower;
    private final NoiseOctaves upper;
    private final NoiseOctaves selector;

    EndTerrainNoise(final BitRandom random) {
        this.lower = new NoiseOctaves(random, BOUND_LAYERS, SMEAR_STEP, BOUND_WEIGHT);
        this.upper = new NoiseOctaves(random, BOUND_LAYERS, SMEAR_STEP, BOUND_WEIGHT);
        this.selector = new NoiseOctaves(random, SELECTOR_LAYERS, SMEAR_STEP / SELECTOR_VERTICAL_STRETCH, SELECTOR_WEIGHT);
    }

    float sample(final int blockX, final int blockY, final int blockZ) {
        final float first = lower.sample(blockX, blockY, blockZ, HORIZONTAL_SCALE, VERTICAL_SCALE);
        final float second = upper.sample(blockX, blockY, blockZ, HORIZONTAL_SCALE, VERTICAL_SCALE);
        final float picked = selector.sample(blockX, blockY, blockZ,
                HORIZONTAL_SCALE / SELECTOR_HORIZONTAL_STRETCH, VERTICAL_SCALE / SELECTOR_VERTICAL_STRETCH);
        final float amount = Math.clamp(picked + SELECTOR_OFFSET, 0.0F, 1.0F);
        if (amount <= 0.0F) {
            return first;
        }
        if (amount >= 1.0F) {
            return second;
        }
        return first + amount * (second - first);
    }
}
