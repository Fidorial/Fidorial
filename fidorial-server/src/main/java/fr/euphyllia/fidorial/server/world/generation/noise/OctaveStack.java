package fr.euphyllia.fidorial.server.world.generation.noise;

import java.util.ArrayList;
import java.util.List;

/**
 * A sum of noise layers, each read at its own frequency and weighted by its own amplitude. The layers are added
 * in the order they were given, in single precision.
 */
public final class OctaveStack implements NoiseField {

    private final NoiseField[] layers;
    private final double[] frequencies;
    private final float[] amplitudes;
    private final float bound;

    private OctaveStack(final List<NoiseField> layers, final List<Double> frequencies, final List<Float> amplitudes,
                        final float declaredBound) {
        final int count = layers.size();
        this.layers = layers.toArray(NoiseField[]::new);
        this.frequencies = new double[count];
        this.amplitudes = new float[count];
        float total = 0.0F;
        for (int i = 0; i < count; i++) {
            this.frequencies[i] = frequencies.get(i);
            this.amplitudes[i] = amplitudes.get(i);
            total += Math.abs(this.amplitudes[i]) * this.layers[i].bound();
        }
        this.bound = Float.isNaN(declaredBound) ? total : declaredBound;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public float sample(final double x, final double y, final double z) {
        float sum = 0.0F;
        for (int i = 0; i < layers.length; i++) {
            final double frequency = frequencies[i];
            sum += amplitudes[i] * layers[i].sample(x * frequency, y * frequency, z * frequency);
        }
        return sum;
    }

    @Override
    public float bound() {
        return bound;
    }

    /**
     * Collects the layers of a stack.
     */
    public static final class Builder {

        private final List<NoiseField> layers = new ArrayList<>();
        private final List<Double> frequencies = new ArrayList<>();
        private final List<Float> amplitudes = new ArrayList<>();
        private float bound = Float.NaN;

        private Builder() {
        }

        public Builder add(final NoiseField layer, final double frequency, final float amplitude) {
            layers.add(layer);
            frequencies.add(frequency);
            amplitudes.add(amplitude);
            return this;
        }

        /**
         * Replaces the bound worked out from the layers, which assumes they all peak together, by an expected one.
         */
        public Builder bound(final float expected) {
            this.bound = expected;
            return this;
        }

        public OctaveStack build() {
            return new OctaveStack(layers, frequencies, amplitudes, bound);
        }
    }
}
