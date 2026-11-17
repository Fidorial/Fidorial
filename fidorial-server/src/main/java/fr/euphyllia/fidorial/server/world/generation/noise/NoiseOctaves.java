package fr.euphyllia.fidorial.server.world.generation.noise;

import fr.euphyllia.fidorial.server.world.generation.random.BitRandom;

/**
 * Several layers of {@link SmearedPerlinNoise} added together. Each layer is twice as wide as the one before and
 * weighs twice as much, so the wide layers draw the large shapes and the narrow ones only add detail.
 */
public final class NoiseOctaves {

    private final SmearedPerlinNoise[] layers;
    private final double[] frequencies;
    private final float[] weights;

    /**
     * @param random      the stream the layers are drawn from, the narrowest layer first
     * @param count       the number of layers
     * @param smearStep   the height of a vertical step of the narrowest layer, see {@link SmearedPerlinNoise}
     * @param totalWeight the sum of the weights of all the layers
     */
    public NoiseOctaves(final BitRandom random, final int count, final double smearStep, final double totalWeight) {
        this.layers = new SmearedPerlinNoise[count];
        this.frequencies = new double[count];
        this.weights = new float[count];
        double frequency = 1.0;
        double weight = totalWeight / (Math.pow(2.0, count) - 1.0);
        for (int i = 0; i < count; i++) {
            layers[i] = new SmearedPerlinNoise(random, smearStep * frequency);
            frequencies[i] = frequency;
            weights[i] = (float) weight;
            frequency /= 2.0;
            weight *= 2.0;
        }
    }

    /**
     * {@return the sum of the layers at a block}
     *
     * @param horizontalScale what one block is worth for the narrowest layer, along x and z
     * @param verticalScale   the same along y
     */
    public float sample(final int blockX, final int blockY, final int blockZ, final double horizontalScale, final double verticalScale) {
        float sum = 0.0F;
        for (int i = 0; i < layers.length; i++) {
            final double frequency = frequencies[i];
            sum += weights[i] * layers[i].sample(blockX, blockY, blockZ, horizontalScale * frequency, verticalScale * frequency);
        }
        return sum;
    }
}
