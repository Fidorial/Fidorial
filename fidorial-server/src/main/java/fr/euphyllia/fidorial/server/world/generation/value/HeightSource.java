package fr.euphyllia.fidorial.server.world.generation.value;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.euphyllia.fidorial.server.world.generation.data.HeightContext;
import fr.euphyllia.fidorial.server.world.generation.random.BitRandom;

/**
 * A height drawn from a stream, between two heights resolved against the dimension: uniform, a trapezoid, biased
 * towards the bottom, or a weighted pick among other sources.
 */
@FunctionalInterface
public interface HeightSource {

    int sample(BitRandom random, HeightContext heights);

    /**
     * Reads a height source: a bare height, or an object with a {@code type}.
     */
    static HeightSource parse(final JsonElement element) {
        if (element.isJsonPrimitive()) {
            final int value = element.getAsInt();
            return (random, heights) -> value;
        }
        final JsonObject object = element.getAsJsonObject();
        if (!object.has("type")) {
            return (random, heights) -> heights.resolve(object);
        }
        final String type = Values.type(object);
        return switch (type) {
            case "constant" -> {
                final JsonElement value = object.get("value");
                yield (random, heights) -> heights.resolve(value);
            }
            case "uniform" -> {
                final JsonElement low = object.get("min_inclusive");
                final JsonElement high = object.get("max_inclusive");
                yield (random, heights) -> {
                    final int min = heights.resolve(low);
                    final int max = heights.resolve(high);
                    return min > max ? min : random.nextInt(max - min + 1) + min;
                };
            }
            case "trapezoid" -> {
                final JsonElement low = object.get("min_inclusive");
                final JsonElement high = object.get("max_inclusive");
                final int plateau = object.has("plateau") ? object.get("plateau").getAsInt() : 0;
                yield (random, heights) -> {
                    final int min = heights.resolve(low);
                    final int max = heights.resolve(high);
                    if (min > max) {
                        return min;
                    }
                    final int range = max - min;
                    if (plateau >= range) {
                        return random.nextInt(max - min + 1) + min;
                    }
                    final int plateauStart = (range - plateau) / 2;
                    final int plateauEnd = range - plateauStart;
                    return min + random.nextInt(plateauEnd + 1) + random.nextInt(plateauStart + 1);
                };
            }
            case "biased_to_bottom" -> {
                final JsonElement low = object.get("min_inclusive");
                final JsonElement high = object.get("max_inclusive");
                final int inner = object.has("inner") ? object.get("inner").getAsInt() : 1;
                yield (random, heights) -> {
                    final int min = heights.resolve(low);
                    final int max = heights.resolve(high);
                    if (max - min - inner + 1 <= 0) {
                        return min;
                    }
                    final int limit = random.nextInt(max - min - inner + 1);
                    return random.nextInt(limit + inner) + min;
                };
            }
            case "very_biased_to_bottom" -> {
                final JsonElement low = object.get("min_inclusive");
                final JsonElement high = object.get("max_inclusive");
                final int inner = object.has("inner") ? object.get("inner").getAsInt() : 1;
                yield (random, heights) -> {
                    final int min = heights.resolve(low);
                    final int max = heights.resolve(high);
                    if (max - min - inner + 1 <= 0) {
                        return min;
                    }
                    final int upper = between(random, min + inner, max);
                    final int biased = between(random, min, upper - 1);
                    return between(random, min, biased - 1 + inner);
                };
            }
            case "weighted_list" -> {
                final JsonArray distribution = object.getAsJsonArray("distribution");
                final HeightSource[] sources = new HeightSource[distribution.size()];
                final int[] weights = new int[distribution.size()];
                int total = 0;
                for (int i = 0; i < sources.length; i++) {
                    final JsonObject entry = distribution.get(i).getAsJsonObject();
                    sources[i] = parse(entry.get("data"));
                    weights[i] = entry.get("weight").getAsInt();
                    total += weights[i];
                }
                final int totalWeight = total;
                yield (random, heights) -> sources[Values.pickWeighted(random, weights, totalWeight)].sample(random, heights);
            }
            default -> throw new IllegalArgumentException("Unknown height provider type " + type);
        };
    }

    /**
     * {@return a draw between two bounds, both included; the lower one without drawing when the range is empty}
     */
    private static int between(final BitRandom random, final int min, final int max) {
        return min >= max ? min : random.nextInt(max - min + 1) + min;
    }
}
