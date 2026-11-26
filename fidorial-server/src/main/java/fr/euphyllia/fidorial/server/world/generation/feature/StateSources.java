package fr.euphyllia.fidorial.server.world.generation.feature;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.euphyllia.fidorial.server.world.generation.noise.NoiseDefinition;
import fr.euphyllia.fidorial.server.world.generation.noise.OctaveStack;
import fr.euphyllia.fidorial.server.world.generation.value.IntSource;
import fr.euphyllia.fidorial.server.world.generation.value.Values;
import fr.euphyllia.fidorial.server.world.generation.random.LegacyRandom;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Reads the state sources written in the data.
 */
final class StateSources {

    private static final String[] DIRECTIONS = {"down", "up", "north", "south", "west", "east"};

    private StateSources() {
    }

    static StateSource parse(final FeatureLibrary library, final JsonElement element) {
        if (element.isJsonPrimitive()) {
            return StateSource.of(library.state(element));
        }
        final JsonObject object = element.getAsJsonObject();
        if (!object.has("type")) {
            return StateSource.of(library.state(object));
        }
        final StateFlags flags = library.context().states();
        return switch (Values.type(object)) {
            case "simple_state_provider" -> StateSource.of(library.state(object.get("state")));
            case "weighted" -> {
                final JsonArray entries = object.getAsJsonArray("entries");
                final char[] states = new char[entries.size()];
                final int[] weights = new int[entries.size()];
                int total = 0;
                for (int i = 0; i < states.length; i++) {
                    final JsonObject entry = entries.get(i).getAsJsonObject();
                    states[i] = library.state(entry.get("data"));
                    weights[i] = entry.get("weight").getAsInt();
                    total += weights[i];
                }
                final int totalWeight = total;
                yield (region, random, x, y, z) -> states[Values.pickWeighted(random, weights, totalWeight)];
            }
            case "randomized_int" -> {
                final StateSource source = library.source(object.get("source"));
                final String property = object.get("property").getAsString();
                final IntSource values = IntSource.parse(object.get("values"));
                yield (region, random, x, y, z) -> {
                    final char base = source.pickOrKeep(region, random, x, y, z);
                    if (flags.property(base, property) == null) {
                        return base;
                    }
                    return flags.with(base, property, Integer.toString(values.sample(random)));
                };
            }
            case "rotated" -> {
                final StateSource source = library.source(object.get("state"));
                final int fixed = object.has("direction")
                        ? List.of(DIRECTIONS).indexOf(object.get("direction").getAsString().toLowerCase(Locale.ROOT)) : -1;
                yield (region, random, x, y, z) -> {
                    final int direction = fixed >= 0 ? fixed : random.nextInt(DIRECTIONS.length);
                    char state = source.pickOrKeep(region, random, x, y, z);
                    final String axis = direction < 2 ? "y" : direction < 4 ? "z" : "x";
                    state = flags.withIfAllowed(state, "axis", axis);
                    return flags.withIfAllowed(state, "facing", DIRECTIONS[direction]);
                };
            }
            case "random_block" -> {
                final char[] states = library.blockList(object.get("blocks"));
                yield (region, random, x, y, z) -> states.length == 0 ? StateSource.NONE : states[random.nextInt(states.length)];
            }
            case "noise" -> {
                final Noise noise = noise(object);
                final char[] states = states(library, object.getAsJsonArray("states"));
                yield (region, random, x, y, z) -> pickByNoise(states, (float) noise.at(x, y, z, noise.scale));
            }
            case "dual_noise" -> {
                final Noise noise = noise(object);
                final OctaveStack slow = NoiseDefinition.parse(object.get("slow_noise")).create(new LegacyRandom(noise.seed));
                final float slowScale = object.get("slow_scale").getAsFloat();
                final JsonArray variety = object.getAsJsonArray("variety");
                final int minVariety = variety.get(0).getAsInt();
                final int maxVariety = variety.get(1).getAsInt();
                final char[] states = states(library, object.getAsJsonArray("states"));
                yield (region, random, x, y, z) -> {
                    final double varietyNoise = slow.sample(x * (double) slowScale, y * (double) slowScale, z * (double) slowScale);
                    final int count = (int) clampedMap(varietyNoise, -1.0, 1.0, minVariety, maxVariety + 1);
                    final char[] candidates = new char[count];
                    for (int i = 0; i < count; i++) {
                        final float value = slow.sample((x + i * 54545) * (double) slowScale, y * (double) slowScale,
                                (z + i * 34234) * (double) slowScale);
                        candidates[i] = pickByNoise(states, value);
                    }
                    return pickByNoise(candidates, (float) noise.at(x, y, z, noise.scale));
                };
            }
            case "noise_threshold" -> {
                final Noise noise = noise(object);
                final float threshold = object.get("threshold").getAsFloat();
                final float highChance = object.get("high_chance").getAsFloat();
                final char fallback = library.state(object.get("default_state"));
                final char[] low = states(library, object.getAsJsonArray("low_states"));
                final char[] high = states(library, object.getAsJsonArray("high_states"));
                yield (region, random, x, y, z) -> {
                    final double value = noise.at(x, y, z, noise.scale);
                    if (value < threshold) {
                        return low[random.nextInt(low.length)];
                    }
                    return random.nextFloat() < highChance ? high[random.nextInt(high.length)] : fallback;
                };
            }
            case "rule_based" -> {
                final List<BlockTest> tests = new ArrayList<>();
                final List<StateSource> results = new ArrayList<>();
                for (final JsonElement rule : object.getAsJsonArray("rules")) {
                    tests.add(library.test(rule.getAsJsonObject().get("if_true")));
                    results.add(library.source(rule.getAsJsonObject().get("then")));
                }
                final StateSource fallback = object.has("fallback") ? library.source(object.get("fallback")) : null;
                yield (region, random, x, y, z) -> {
                    for (int i = 0; i < tests.size(); i++) {
                        if (tests.get(i).test(region, x, y, z)) {
                            final char state = results.get(i).pick(region, random, x, y, z);
                            if (state != StateSource.NONE) {
                                return state;
                            }
                        }
                    }
                    return fallback == null ? StateSource.NONE : fallback.pick(region, random, x, y, z);
                };
            }
            default -> {
                library.unsupported("block state provider", Values.type(object));
                yield (region, random, x, y, z) -> StateSource.NONE;
            }
        };
    }

    /**
     * A noise with a fixed seed, read at a scale.
     */
    private record Noise(long seed, OctaveStack field, float scale) {

        double at(final int x, final int y, final int z, final double factor) {
            return field.sample(x * factor, y * factor, z * factor);
        }
    }

    private static Noise noise(final JsonObject object) {
        final long seed = object.get("seed").getAsLong();
        final OctaveStack field = NoiseDefinition.parse(object.get("noise")).create(new LegacyRandom(seed));
        return new Noise(seed, field, object.get("scale").getAsFloat());
    }

    private static char[] states(final FeatureLibrary library, final JsonArray array) {
        final char[] states = new char[array.size()];
        for (int i = 0; i < states.length; i++) {
            states[i] = library.state(array.get(i));
        }
        return states;
    }

    /**
     * {@return the state at the place the noise points to along the list}
     */
    private static char pickByNoise(final char[] states, final float noise) {
        final float placement = Math.clamp((1.0F + noise) / 2.0F, 0.0F, 0.9999F);
        return states[(int) (placement * states.length)];
    }

    private static double clampedMap(final double value, final double fromLow, final double fromHigh,
                                     final double toLow, final double toHigh) {
        final double t = Math.clamp((value - fromLow) / (fromHigh - fromLow), 0.0, 1.0);
        return toLow + t * (toHigh - toLow);
    }
}
