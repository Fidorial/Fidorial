package fr.euphyllia.fidorial.server.world.generation.value;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.euphyllia.fidorial.server.world.generation.random.BitRandom;

/**
 * A float drawn from a stream: a constant, a uniform pick, a trapezoid or a clamped normal value.
 */
@FunctionalInterface
public interface FloatSource {

    float sample(BitRandom random);

    static FloatSource constant(final float value) {
        return random -> value;
    }

    /**
     * Reads a float source: a bare number, or an object with a {@code type}.
     */
    static FloatSource parse(final JsonElement element) {
        if (element.isJsonPrimitive()) {
            return constant(element.getAsFloat());
        }
        final JsonObject object = element.getAsJsonObject();
        final String type = Values.type(object);
        return switch (type) {
            case "constant" -> constant(object.get("value").getAsFloat());
            case "uniform" -> {
                final JsonObject bounds = object.has("value") ? object.getAsJsonObject("value") : object;
                final float min = bounds.has("min_inclusive") ? bounds.get("min_inclusive").getAsFloat() : bounds.get("min").getAsFloat();
                final float max = bounds.has("max_exclusive") ? bounds.get("max_exclusive").getAsFloat() : bounds.get("max").getAsFloat();
                yield random -> random.nextFloat() * (max - min) + min;
            }
            case "trapezoid" -> {
                final float min = object.get("min").getAsFloat();
                final float max = object.get("max").getAsFloat();
                final float plateau = object.has("plateau") ? object.get("plateau").getAsFloat() : 0.0F;
                yield random -> {
                    final float range = max - min;
                    final float plateauStart = (range - plateau) / 2.0F;
                    final float plateauEnd = range - plateauStart;
                    return min + random.nextFloat() * plateauEnd + random.nextFloat() * plateauStart;
                };
            }
            case "clamped_normal" -> {
                final float mean = object.get("mean").getAsFloat();
                final float deviation = object.get("deviation").getAsFloat();
                final float min = object.get("min").getAsFloat();
                final float max = object.get("max").getAsFloat();
                yield random -> Math.clamp(mean + (float) random.nextGaussian() * deviation, min, max);
            }
            default -> throw new IllegalArgumentException("Unknown float provider type " + type);
        };
    }
}
