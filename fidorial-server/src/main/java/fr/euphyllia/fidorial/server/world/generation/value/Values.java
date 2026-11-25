package fr.euphyllia.fidorial.server.world.generation.value;

import com.google.gson.JsonObject;
import fr.euphyllia.fidorial.server.world.generation.random.BitRandom;

/**
 * Small helpers shared by the value sources.
 */
public final class Values {

    private Values() {
    }

    /**
     * {@return the {@code type} of a definition, without the default namespace}
     */
    public static String type(final JsonObject object) {
        final String type = object.get("type").getAsString();
        return type.startsWith("minecraft:") ? type.substring("minecraft:".length()) : type;
    }

    /**
     * {@return the index of a weighted entry}: one draw below the total weight, then the entries are walked until
     * their cumulated weight passes it
     */
    public static int pickWeighted(final BitRandom random, final int[] weights, final int totalWeight) {
        if (totalWeight <= 0) {
            throw new IllegalStateException("A weighted list needs a positive total weight");
        }
        int selection = random.nextInt(totalWeight);
        for (int i = 0; i < weights.length; i++) {
            selection -= weights[i];
            if (selection < 0) {
                return i;
            }
        }
        return weights.length - 1;
    }
}
