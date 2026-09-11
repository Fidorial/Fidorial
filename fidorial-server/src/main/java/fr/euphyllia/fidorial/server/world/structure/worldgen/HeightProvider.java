package fr.euphyllia.fidorial.server.world.structure.worldgen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.euphyllia.fidorial.server.world.structure.math.LegacyRandom;

public interface HeightProvider {

    int sample(LegacyRandom random, int minY, int height);

    static int anchor(final JsonElement element, final int minY, final int height) {
        if (element.isJsonPrimitive()) {
            return element.getAsInt();
        }
        final JsonObject object = element.getAsJsonObject();
        if (object.has("absolute")) {
            return object.get("absolute").getAsInt();
        }
        if (object.has("above_bottom")) {
            return minY + object.get("above_bottom").getAsInt();
        }
        if (object.has("below_top")) {
            return minY + height - 1 - object.get("below_top").getAsInt();
        }
        return 0;
    }

    static HeightProvider parse(final JsonElement element) {
        if (element.isJsonPrimitive()) {
            final int value = element.getAsInt();
            return (random, minY, height) -> value;
        }
        final JsonObject object = element.getAsJsonObject();
        if (!object.has("type")) {
            return (random, minY, height) -> anchor(object, minY, height);
        }
        final String type = object.get("type").getAsString().replace("minecraft:", "");
        return switch (type) {
            case "uniform" -> (random, minY, height) -> {
                final int min = anchor(object.get("min_inclusive"), minY, height);
                final int max = anchor(object.get("max_inclusive"), minY, height);
                return min > max ? min : random.nextIntBetweenInclusive(min, max);
            };
            case "biased_to_bottom" -> (random, minY, height) -> {
                final int min = anchor(object.get("min_inclusive"), minY, height);
                final int max = anchor(object.get("max_inclusive"), minY, height);
                final int inner = object.has("inner") ? object.get("inner").getAsInt() : 1;
                if (max - min - inner + 1 <= 0) {
                    return min;
                }
                final int k = random.nextInt(max - min - inner + 1);
                return random.nextInt(k + inner) + min;
            };
            case "very_biased_to_bottom" -> (random, minY, height) -> {
                final int min = anchor(object.get("min_inclusive"), minY, height);
                final int max = anchor(object.get("max_inclusive"), minY, height);
                final int inner = object.has("inner") ? object.get("inner").getAsInt() : 1;
                if (max - min - inner + 1 <= 0) {
                    return min;
                }
                final int k = random.nextIntBetweenInclusive(min + inner, max);
                final int l = random.nextIntBetweenInclusive(min, k - 1);
                return random.nextIntBetweenInclusive(min, l - 1 + inner);
            };
            case "trapezoid" -> (random, minY, height) -> {
                final int min = anchor(object.get("min_inclusive"), minY, height);
                final int max = anchor(object.get("max_inclusive"), minY, height);
                final int plateau = object.has("plateau") ? object.get("plateau").getAsInt() : 0;
                final int span = max - min;
                if (plateau >= span) {
                    return random.nextIntBetweenInclusive(min, max);
                }
                final int side = (span - plateau) / 2;
                return min + random.nextIntBetweenInclusive(0, span - side) + random.nextIntBetweenInclusive(0, side);
            };
            default -> (random, minY, height) -> anchor(object.has("value") ? object.get("value") : object, minY, height);
        };
    }
}
