package fr.euphyllia.fidorial.server.world.structure.worldgen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.euphyllia.fidorial.server.world.structure.math.LegacyRandom;

import java.util.ArrayList;
import java.util.List;

public interface IntProvider {

    int sample(LegacyRandom random);

    int maxValue();

    static IntProvider constant(final int value) {
        return new IntProvider() {
            @Override
            public int sample(final LegacyRandom random) {
                return value;
            }

            @Override
            public int maxValue() {
                return value;
            }
        };
    }

    static IntProvider parse(final JsonElement element) {
        if (element.isJsonPrimitive()) {
            return constant(element.getAsInt());
        }
        final JsonObject object = element.getAsJsonObject();
        final String type = object.has("type") ? object.get("type").getAsString().replace("minecraft:", "") : "constant";
        return switch (type) {
            case "uniform" -> {
                final JsonObject values = object.has("value") ? object.getAsJsonObject("value") : object;
                final int min = values.get("min_inclusive").getAsInt();
                final int max = values.get("max_inclusive").getAsInt();
                yield new IntProvider() {
                    @Override
                    public int sample(final LegacyRandom random) {
                        return random.nextIntBetweenInclusive(min, max);
                    }

                    @Override
                    public int maxValue() {
                        return max;
                    }
                };
            }
            case "biased_to_bottom" -> {
                final JsonObject values = object.has("value") ? object.getAsJsonObject("value") : object;
                final int min = values.get("min_inclusive").getAsInt();
                final int max = values.get("max_inclusive").getAsInt();
                yield new IntProvider() {
                    @Override
                    public int sample(final LegacyRandom random) {
                        return min + random.nextInt(random.nextInt(max - min + 1) + 1);
                    }

                    @Override
                    public int maxValue() {
                        return max;
                    }
                };
            }
            case "clamped", "clamped_normal" -> {
                final IntProvider source = object.has("source") ? parse(object.get("source")) : constant(
                        (object.get("min_inclusive").getAsInt() + object.get("max_inclusive").getAsInt()) / 2);
                final int min = object.get("min_inclusive").getAsInt();
                final int max = object.get("max_inclusive").getAsInt();
                yield new IntProvider() {
                    @Override
                    public int sample(final LegacyRandom random) {
                        return Math.clamp(source.sample(random), min, max);
                    }

                    @Override
                    public int maxValue() {
                        return Math.min(max, source.maxValue());
                    }
                };
            }
            case "weighted_list" -> {
                final JsonArray distribution = object.getAsJsonArray("distribution");
                final List<IntProvider> providers = new ArrayList<>();
                final List<Integer> weights = new ArrayList<>();
                int total = 0;
                int max = Integer.MIN_VALUE;
                for (final JsonElement entry : distribution) {
                    final JsonObject weighted = entry.getAsJsonObject();
                    final IntProvider data = parse(weighted.get("data"));
                    final int weight = weighted.get("weight").getAsInt();
                    providers.add(data);
                    weights.add(weight);
                    total += weight;
                    max = Math.max(max, data.maxValue());
                }
                final int totalWeight = total;
                final int maxValue = max;
                yield new IntProvider() {
                    @Override
                    public int sample(final LegacyRandom random) {
                        if (totalWeight <= 0) {
                            return 0;
                        }
                        int roll = random.nextInt(totalWeight);
                        for (int i = 0; i < providers.size(); i++) {
                            roll -= weights.get(i);
                            if (roll < 0) {
                                return providers.get(i).sample(random);
                            }
                        }
                        return providers.getLast().sample(random);
                    }

                    @Override
                    public int maxValue() {
                        return maxValue;
                    }
                };
            }
            default -> constant(object.has("value") ? object.get("value").getAsInt() : 0);
        };
    }
}
