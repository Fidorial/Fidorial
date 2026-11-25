package fr.euphyllia.fidorial.server.world.generation.value;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.euphyllia.fidorial.server.world.generation.random.BitRandom;

/**
 * An integer drawn from a stream: a constant, a uniform pick, a weighted pick among other sources, a value biased
 * towards its minimum, a trapezoid, or a clamped source.
 */
public interface IntSource {

    int sample(BitRandom random);

    int min();

    int max();

    static IntSource constant(final int value) {
        return new IntSource() {
            @Override
            public int sample(final BitRandom random) {
                return value;
            }

            @Override
            public int min() {
                return value;
            }

            @Override
            public int max() {
                return value;
            }
        };
    }

    /**
     * Reads an integer source: a bare number, or an object with a {@code type}.
     */
    static IntSource parse(final JsonElement element) {
        if (element.isJsonPrimitive()) {
            return constant(element.getAsInt());
        }
        final JsonObject object = element.getAsJsonObject();
        // a bare range is a uniform draw
        final String type = object.has("type") ? Values.type(object) : "uniform";
        return switch (type) {
            case "constant" -> constant(object.get("value").getAsInt());
            case "uniform" -> range(object, (random, min, max) -> random.nextInt(max - min + 1) + min);
            case "biased_to_bottom" -> range(object, (random, min, max) -> min + random.nextInt(random.nextInt(max - min + 1) + 1));
            case "very_biased_to_bottom" -> range(object,
                    (random, min, max) -> min + random.nextInt(random.nextInt(random.nextInt(max - min + 1) + 1) + 1));
            case "trapezoid" -> trapezoid(object);
            case "clamped" -> {
                final IntSource source = parse(object.get("source"));
                final int low = object.get("min_inclusive").getAsInt();
                final int high = object.get("max_inclusive").getAsInt();
                yield new IntSource() {
                    @Override
                    public int sample(final BitRandom random) {
                        return Math.clamp(source.sample(random), low, high);
                    }

                    @Override
                    public int min() {
                        return Math.max(low, source.min());
                    }

                    @Override
                    public int max() {
                        return Math.min(high, source.max());
                    }
                };
            }
            case "clamped_normal" -> {
                final float mean = object.get("mean").getAsFloat();
                final float deviation = object.get("deviation").getAsFloat();
                final int low = object.get("min_inclusive").getAsInt();
                final int high = object.get("max_inclusive").getAsInt();
                yield new IntSource() {
                    @Override
                    public int sample(final BitRandom random) {
                        return (int) Math.clamp(mean + (float) random.nextGaussian() * deviation, (float) low, (float) high);
                    }

                    @Override
                    public int min() {
                        return low;
                    }

                    @Override
                    public int max() {
                        return high;
                    }
                };
            }
            case "weighted_list" -> weighted(object.getAsJsonArray("distribution"));
            default -> throw new IllegalArgumentException("Unknown int provider type " + type);
        };
    }

    private static IntSource trapezoid(final JsonObject object) {
        final int low = object.has("min") ? object.get("min").getAsInt() : object.get("min_inclusive").getAsInt();
        final int high = object.has("max") ? object.get("max").getAsInt() : object.get("max_inclusive").getAsInt();
        final int plateau = object.has("plateau") ? object.get("plateau").getAsInt() : 0;
        return new IntSource() {
            @Override
            public int sample(final BitRandom random) {
                if (plateau == 0 && high == -low) {
                    final int up = random.nextInt(high + 1);
                    final int down = random.nextInt(high + 1);
                    return up - down;
                }
                final int range = high - low;
                if (plateau == range) {
                    return random.nextInt(high - low + 1) + low;
                }
                final int plateauStart = (range - plateau) / 2;
                final int plateauEnd = range - plateauStart;
                return low + random.nextInt(plateauEnd + 1) + random.nextInt(plateauStart + 1);
            }

            @Override
            public int min() {
                return low;
            }

            @Override
            public int max() {
                return high;
            }
        };
    }

    /**
     * A draw between two bounds.
     */
    @FunctionalInterface
    interface RangeDraw {
        int draw(BitRandom random, int min, int max);
    }

    private static IntSource range(final JsonObject object, final RangeDraw draw) {
        final JsonObject bounds = object.has("value") ? object.getAsJsonObject("value") : object;
        final int low = bounds.get("min_inclusive").getAsInt();
        final int high = bounds.get("max_inclusive").getAsInt();
        return new IntSource() {
            @Override
            public int sample(final BitRandom random) {
                return draw.draw(random, low, high);
            }

            @Override
            public int min() {
                return low;
            }

            @Override
            public int max() {
                return high;
            }
        };
    }

    private static IntSource weighted(final JsonArray distribution) {
        final IntSource[] sources = new IntSource[distribution.size()];
        final int[] weights = new int[distribution.size()];
        int total = 0;
        int low = Integer.MAX_VALUE;
        int high = Integer.MIN_VALUE;
        for (int i = 0; i < sources.length; i++) {
            final JsonObject entry = distribution.get(i).getAsJsonObject();
            sources[i] = parse(entry.get("data"));
            weights[i] = entry.get("weight").getAsInt();
            total += weights[i];
            low = Math.min(low, sources[i].min());
            high = Math.max(high, sources[i].max());
        }
        final int totalWeight = total;
        final int lowest = low;
        final int highest = high;
        return new IntSource() {
            @Override
            public int sample(final BitRandom random) {
                return sources[Values.pickWeighted(random, weights, totalWeight)].sample(random);
            }

            @Override
            public int min() {
                return lowest;
            }

            @Override
            public int max() {
                return highest;
            }
        };
    }

    /**
     * {@return the sources of a JSON array, each one read with {@link #parse}}
     */
    static IntSource[] parseAll(final JsonArray array) {
        final IntSource[] sources = new IntSource[array.size()];
        for (int i = 0; i < sources.length; i++) {
            sources[i] = parse(array.get(i));
        }
        return sources;
    }
}
