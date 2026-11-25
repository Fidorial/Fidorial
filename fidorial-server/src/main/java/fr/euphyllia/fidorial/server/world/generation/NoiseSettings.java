package fr.euphyllia.fidorial.server.world.generation;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.euphyllia.fidorial.server.world.generation.data.WorldgenData;
import org.jspecify.annotations.Nullable;

/**
 * The noise settings of a dimension, as a data pack writes them: the height of the terrain, the sea, the
 * aquifers, the material rule and the functions of the noise router.
 *
 * @param id            the identifier of the settings
 * @param minY          the lowest block of the terrain
 * @param height        how many blocks the terrain is high
 * @param seaLevel      the sea level
 * @param defaultFluid  the fluid of the seas
 * @param aquifers      the aquifer functions, {@code null} when the settings have none
 * @param materialRule  the material rule, an identifier or an inline rule
 * @param router        the noise router
 * @param spawnTarget   where players should spawn, in climate terms, {@code null} for anywhere
 */
public record NoiseSettings(String id, int minY, int height, int seaLevel, JsonElement defaultFluid,
                            @Nullable JsonObject aquifers, JsonElement materialRule, JsonObject router,
                            @Nullable JsonElement spawnTarget) {

    /**
     * Reads the noise settings of an identifier.
     */
    public static NoiseSettings read(final WorldgenData data, final String id) {
        final JsonObject object = data.get(WorldgenData.Section.NOISE_SETTINGS, id).getAsJsonObject();
        final JsonObject noise = object.getAsJsonObject("noise");
        final JsonElement materialRule = object.has("material_rule") ? object.get("material_rule") : object.get("surface_rule");
        return new NoiseSettings(WorldgenData.normalize(id),
                noise.get("min_y").getAsInt(),
                noise.get("height").getAsInt(),
                object.get("sea_level").getAsInt(),
                object.get("default_fluid"),
                object.has("aquifers") ? object.getAsJsonObject("aquifers") : null,
                materialRule,
                object.getAsJsonObject("noise_router"),
                object.get("spawn_target"));
    }

    /**
     * {@return a function of the noise router, or zero when the router leaves it out}
     */
    public JsonElement routed(final String name) {
        final JsonElement function = router.get(name);
        return function == null ? new com.google.gson.JsonPrimitive(0.0) : function;
    }
}
