package fr.euphyllia.fidorial.server.world.generation.noise;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.euphyllia.fidorial.server.world.generation.random.BitRandom;
import fr.euphyllia.fidorial.server.world.generation.random.PositionalRandom;
import fr.euphyllia.fidorial.server.world.generation.random.XoroshiroSource;
import fr.euphyllia.fidorial.server.world.structure.math.LegacyRandom;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * The parameters of a named noise, as a data pack writes them: the first octave, how many octaves, their
 * relative weights, and how the result is scaled.
 *
 * <p>A noise built from these parameters is made of two copies of the same octaves, the second read at a
 * slightly higher frequency so that the lattices of the two never line up. The sum is scaled so that its spread
 * is about the same whatever the octaves: a third of the total weight, at one standard deviation.</p>
 *
 * @param baseAmplitude the weight of the first octave, before the others halve it one after the other
 * @param baseOctave    the first octave; octave {@code n} is read at frequency {@code 2^n}
 * @param octaveCount   how many octaves follow the first one, it included
 * @param scaling       how the octaves are scaled before they are summed
 * @param modifiers     an extra weight for each octave, all ones when empty; zero leaves an octave out
 */
public record NoiseDefinition(double baseAmplitude, int baseOctave, int octaveCount, Scaling scaling,
                              double[] modifiers) {

    // the second copy is read this much faster
    private static final double SECOND_COPY_FACTOR = 1.0181268882175227;
    // one octave of gradient noise spreads its values this much, at one standard deviation
    private static final double OCTAVE_DEVIATION = 0.2702247831245211;
    private static final double TARGET_DEVIATION = 1.0 / 3.0;

    /**
     * How the octave weights are worked out.
     */
    public enum Scaling {
        /**
         * The weights are left as given.
         */
        NONE,
        /**
         * The first weight is raised so that the weights, halving from one octave to the next, sum to the base
         * amplitude.
         */
        NORMALIZED,
        /**
         * Like {@link #NORMALIZED}, with the overall factor of earlier releases, for the noises that still need
         * it.
         */
        LEGACY
    }

    private record Octave(int index, double frequency, double amplitude) {
    }

    /**
     * Reads the parameters of a noise.
     */
    public static NoiseDefinition parse(final JsonElement element) {
        final JsonObject object = element.getAsJsonObject();
        final double baseAmplitude = object.has("base_amplitude") ? object.get("base_amplitude").getAsDouble() : 1.0;
        final int baseOctave = object.get("base_octave").getAsInt();
        final int octaveCount = object.has("octave_count") ? object.get("octave_count").getAsInt() : 1;
        Scaling scaling = Scaling.NORMALIZED;
        if (object.has("normalize")) {
            final JsonElement normalize = object.get("normalize");
            if (normalize.getAsJsonPrimitive().isBoolean()) {
                scaling = normalize.getAsBoolean() ? Scaling.NORMALIZED : Scaling.NONE;
            } else if ("legacy".equals(normalize.getAsString())) {
                scaling = Scaling.LEGACY;
            }
        }
        double[] modifiers = new double[0];
        if (object.has("amplitude_modifiers")) {
            final JsonArray array = object.getAsJsonArray("amplitude_modifiers");
            modifiers = new double[array.size()];
            for (int i = 0; i < modifiers.length; i++) {
                modifiers[i] = array.get(i).getAsDouble();
            }
        }
        return new NoiseDefinition(baseAmplitude, baseOctave, octaveCount, scaling, modifiers);
    }

    private double modifier(final int index) {
        return modifiers.length == 0 ? 1.0 : modifiers[index];
    }

    private List<Octave> octaves(final double amplitudeAtBase, final boolean raiseFirst) {
        double frequency = Math.pow(2.0, baseOctave);
        double amplitude = amplitudeAtBase;
        if (raiseFirst) {
            amplitude = amplitudeAtBase * (Math.pow(0.5, -(octaveCount - 1)) / (Math.pow(0.5, -octaveCount) - 1.0));
        }
        final List<Octave> octaves = new ArrayList<>(octaveCount);
        for (int i = 0; i < octaveCount; i++) {
            final double weight = modifier(i);
            if (weight != 0.0) {
                octaves.add(new Octave(baseOctave + i, frequency, amplitude * weight));
            }
            frequency *= 2.0;
            amplitude *= 0.5;
        }
        return octaves;
    }

    private static double totalWeight(final List<Octave> octaves) {
        double total = 0.0;
        for (final Octave octave : octaves) {
            total += Math.abs(octave.amplitude());
        }
        return total;
    }

    /**
     * {@return the factor that brings the spread of the sum of both copies to a third of the total weight}
     */
    private static double scaleFor(final double totalWeight, final List<Octave> octaves) {
        double variance = 0.0;
        for (final Octave octave : octaves) {
            final double deviation = OCTAVE_DEVIATION * Math.abs(octave.amplitude());
            variance += deviation * deviation;
        }
        final double deviation = Math.sqrt(variance);
        if (deviation == 0.0) {
            return 0.0;
        }
        return totalWeight * TARGET_DEVIATION / (deviation * Math.sqrt(2.0));
    }

    /**
     * {@return the overall factor of earlier releases, which only looked at how many octaves the noise spans}
     */
    private double legacyScale(final double amplitude) {
        int first = Integer.MAX_VALUE;
        int last = Integer.MIN_VALUE;
        for (int i = 0; i < octaveCount; i++) {
            if (modifier(i) != 0.0) {
                first = Math.min(first, i);
                last = Math.max(last, i);
            }
        }
        final double expectedDeviation = 0.1 * (1.0 + 1.0 / (last - first + 1));
        return amplitude * 0.5 * TARGET_DEVIATION / expectedDeviation;
    }

    /**
     * Builds the noise.
     *
     * @param random the stream of the noise, usually named after it; two positional factories are split from it,
     *               one per copy
     */
    public OctaveStack create(final XoroshiroSource random) {
        final PositionalRandom first = random.splitPositional();
        final PositionalRandom second = random.splitPositional();
        return build(first::named, second::named);
    }

    /**
     * Builds the noise from a classic stream, as the noises with a fixed seed of the decorations do: each copy
     * takes a seed from the stream, and an octave mixes it with the hash of its name.
     */
    public OctaveStack create(final LegacyRandom random) {
        final long first = random.nextLong();
        final long second = random.nextLong();
        return build(name -> new LegacyRandom(name.hashCode() ^ first), name -> new LegacyRandom(name.hashCode() ^ second));
    }

    private OctaveStack build(final Function<String, BitRandom> first,
                              final Function<String, BitRandom> second) {
        final List<Octave> octaves = octaves(baseAmplitude, scaling != Scaling.NONE);
        double totalWeight = totalWeight(octaves);
        double scale = scaleFor(totalWeight, octaves);
        if (scaling == Scaling.LEGACY && scale != 0.0) {
            final double legacy = legacyScale(baseAmplitude);
            totalWeight *= legacy / scale;
            scale = legacy;
        }

        final OctaveStack.Builder stack = OctaveStack.builder();
        for (final Octave octave : octaves) {
            final String name = "octave_" + octave.index();
            final float weight = (float) (scale * octave.amplitude());
            stack.add(new PerlinOctave(first.apply(name)), octave.frequency(), weight);
            stack.add(new PerlinOctave(second.apply(name)), octave.frequency() * SECOND_COPY_FACTOR, weight);
        }
        // six times the expected spread: in practice the noise never leaves it
        return stack.bound((float) (totalWeight * TARGET_DEVIATION * 6.0)).build();
    }
}
