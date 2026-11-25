package fr.euphyllia.fidorial.server.world.generation.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The vertical extent heights are resolved against: data packs give heights relative to the bottom, the top or
 * the sea level, and they mean different blocks in different dimensions.
 *
 * @param minY     the lowest block of the dimension
 * @param height   how many blocks the dimension is high
 * @param seaLevel the sea level of the generator
 */
public record HeightContext(int minY, int height, int seaLevel) {

    /**
     * {@return the highest block of the dimension}
     */
    public int maxY() {
        return minY + height - 1;
    }

    /**
     * Resolves a height: an integer, or an object with one of {@code absolute}, {@code above_bottom},
     * {@code below_top} or {@code relative_to_sea_level}.
     */
    public int resolve(final JsonElement anchor) {
        if (anchor.isJsonPrimitive()) {
            return anchor.getAsInt();
        }
        final JsonObject object = anchor.getAsJsonObject();
        if (object.has("absolute")) {
            return object.get("absolute").getAsInt();
        }
        if (object.has("above_bottom")) {
            return minY + object.get("above_bottom").getAsInt();
        }
        if (object.has("below_top")) {
            return maxY() - object.get("below_top").getAsInt();
        }
        if (object.has("relative_to_sea_level")) {
            return seaLevel + object.get("relative_to_sea_level").getAsInt();
        }
        throw new IllegalArgumentException("Not a height: " + anchor);
    }
}
