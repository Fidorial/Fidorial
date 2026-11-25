package fr.euphyllia.fidorial.server.world.generation.noise;

import fr.euphyllia.fidorial.server.world.generation.random.BitRandom;

/**
 * The base terrain of earlier world generators, still used as a source of detail: two wide noises, and a third,
 * smoother one that picks between them from place to place.
 *
 * <p>All three are stacks of {@linkplain SmearedOctave smeared octaves}. The two bounds have sixteen octaves, the
 * selector eight; the selector is offset by a half and clamped to {@code [0, 1]} before it blends the bounds.</p>
 */
public final class BlendedTerrainNoise {

    private static final double BASE_SCALE = 684.412;
    // the weight given to the sixteen octaves of a bound, a hair under one
    private static final double BOUND_WEIGHT = 0.99998474F;
    private static final double SELECTOR_WEIGHT = 12.75;
    private static final int BOUND_FIRST_OCTAVE = -15;
    private static final int SELECTOR_FIRST_OCTAVE = -7;
    private static final float SELECTOR_OFFSET = 0.5F;

    private final OctaveStack lower;
    private final OctaveStack upper;
    private final OctaveStack selector;
    private final double xzMultiplier;
    private final double yMultiplier;
    private final double selectorXz;
    private final double selectorY;

    /**
     * @param random               the stream the three stacks are drawn from, in order: lower, upper, selector
     * @param xzScale              horizontal scale of the bounds
     * @param yScale               vertical scale of the bounds
     * @param xzFactor             how much smoother the selector is horizontally
     * @param yFactor              how much smoother the selector is vertically
     * @param smearScaleMultiplier the height of a smear step, in vertical scale units
     */
    public BlendedTerrainNoise(final BitRandom random, final double xzScale, final double yScale,
                               final double xzFactor, final double yFactor, final double smearScaleMultiplier) {
        this.xzMultiplier = BASE_SCALE * xzScale;
        this.yMultiplier = BASE_SCALE * yScale;
        final double boundStep = yMultiplier * smearScaleMultiplier;
        this.lower = stack(random, BOUND_FIRST_OCTAVE, boundStep, BOUND_WEIGHT);
        this.upper = stack(random, BOUND_FIRST_OCTAVE, boundStep, BOUND_WEIGHT);
        this.selector = stack(random, SELECTOR_FIRST_OCTAVE, boundStep / yFactor, SELECTOR_WEIGHT);
        this.selectorXz = xzMultiplier / xzFactor;
        this.selectorY = yMultiplier / yFactor;
    }

    /**
     * Builds a stack whose octaves are drawn from the finest to the widest; each one is twice as wide as the
     * previous and weighs twice as much, the weights summing to {@code totalWeight}.
     */
    private static OctaveStack stack(final BitRandom random, final int firstOctave, final double step, final double totalWeight) {
        final int count = -firstOctave + 1;
        double frequency = 1.0;
        double weight = totalWeight / (Math.pow(2.0, count) - 1.0);
        final OctaveStack.Builder builder = OctaveStack.builder();
        for (int i = count - 1; i >= 0; i--) {
            builder.add(new SmearedOctave(random, step * frequency), frequency, (float) weight);
            frequency /= 2.0;
            weight *= 2.0;
        }
        return builder.build();
    }

    /**
     * {@return the noise at a block}
     */
    public float sample(final int x, final int y, final int z) {
        final float picked = selector.sample(x * selectorXz, y * selectorY, z * selectorXz) + SELECTOR_OFFSET;
        final float amount = Math.clamp(picked, 0.0F, 1.0F);
        if (amount == 0.0F) {
            return lower.sample(x * xzMultiplier, y * yMultiplier, z * xzMultiplier);
        }
        if (amount == 1.0F) {
            return upper.sample(x * xzMultiplier, y * yMultiplier, z * xzMultiplier);
        }
        final float low = lower.sample(x * xzMultiplier, y * yMultiplier, z * xzMultiplier);
        final float high = upper.sample(x * xzMultiplier, y * yMultiplier, z * xzMultiplier);
        return low + amount * (high - low);
    }

    /**
     * {@return the largest absolute value the noise can take}
     */
    public float bound() {
        return Math.max(lower.bound(), upper.bound());
    }
}