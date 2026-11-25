package fr.euphyllia.fidorial.server.world.generation.climate;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.world.biome.BiomeDefinition;
import net.kyori.adventure.key.Key;

import java.util.ArrayList;
import java.util.List;

/**
 * What world generation needs to know about a biome: its climate, its carvers and its features.
 *
 * @param index               the number of the biome in its {@link BiomeTable}
 * @param key                 the biome
 * @param temperature         the base temperature
 * @param frozen              whether the temperature is lowered in patches, as in frozen oceans
 * @param downfall            how wet the biome is
 * @param hasPrecipitation    whether it rains or snows at all
 * @param carvers             the carvers started in the biome, by identifier
 * @param features            the placed features of each decoration step, by identifier
 */
public record BiomeInfo(int index, TypedKey<BiomeDefinition> key, float temperature, boolean frozen, float downfall,
                        boolean hasPrecipitation, List<String> carvers, List<List<String>> features) {

    /**
     * The temperature modifier of the biomes whose water freezes in patches.
     */
    private static final Key FROZEN_MODIFIER = Key.key("frozen");

    /**
     * Reads the generation part of a biome definition.
     */
    static BiomeInfo read(final int index, final TypedKey<BiomeDefinition> key, final JsonObject definition) {
        final float temperature = definition.has("temperature") ? definition.get("temperature").getAsFloat() : 0.5F;
        final boolean frozen = definition.has("temperature_modifier")
                && Key.key(definition.get("temperature_modifier").getAsString()).equals(FROZEN_MODIFIER);
        final float downfall = definition.has("downfall") ? definition.get("downfall").getAsFloat() : 0.5F;
        final boolean precipitation = !definition.has("has_precipitation") || definition.get("has_precipitation").getAsBoolean();
        final List<String> carvers = new ArrayList<>();
        final JsonElement carverList = definition.get("carvers");
        if (carverList != null) {
            if (carverList.isJsonArray()) {
                for (final JsonElement carver : carverList.getAsJsonArray()) {
                    carvers.add(carver.getAsString());
                }
            } else {
                carvers.add(carverList.getAsString());
            }
        }
        final List<List<String>> features = new ArrayList<>();
        final JsonArray steps = definition.has("features") ? definition.getAsJsonArray("features") : new JsonArray();
        for (final JsonElement step : steps) {
            final List<String> placed = new ArrayList<>();
            if (step.isJsonArray()) {
                for (final JsonElement feature : step.getAsJsonArray()) {
                    placed.add(feature.getAsString());
                }
            } else {
                placed.add(step.getAsString());
            }
            features.add(List.copyOf(placed));
        }
        return new BiomeInfo(index, key, temperature, frozen, downfall, precipitation, List.copyOf(carvers), List.copyOf(features));
    }
}
