package fr.euphyllia.fidorial.server.world.generation.climate;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.euphyllia.fidorial.server.world.generation.data.WorldgenData;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.world.biome.BiomeDefinition;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Numbers the biomes a generator knows, so that chunks in the making store a {@code char} per biome cell, and
 * keeps what generation reads about each of them. Immutable, safe to share between threads.
 */
public final class BiomeTable {

    private final List<BiomeInfo> biomes;
    private final Map<String, BiomeInfo> byId;

    private BiomeTable(final List<BiomeInfo> biomes) {
        this.biomes = List.copyOf(biomes);
        this.byId = new HashMap<>();
        for (final BiomeInfo biome : biomes) {
            byId.put(biome.key().key().asString(), biome);
        }
    }

    /**
     * Builds the table.
     *
     * @param ids         every biome the generator may use, in the order the numbers are given
     * @param definitions the definition of a biome, from the registry of the server; {@code null} falls back to
     *                    the bundled dataset
     * @param data        the bundled dataset
     */
    public static BiomeTable of(final List<String> ids, final Function<String, @Nullable JsonElement> definitions, final WorldgenData data) {
        final List<BiomeInfo> biomes = new ArrayList<>(ids.size());
        for (final String raw : ids) {
            final String id = WorldgenData.normalize(raw);
            JsonElement definition = definitions.apply(id);
            if (definition == null || !definition.isJsonObject() || !definition.getAsJsonObject().has("features")) {
                definition = data.find(WorldgenData.Section.BIOME, id);
            }
            final JsonObject object = definition != null && definition.isJsonObject() ? definition.getAsJsonObject() : new JsonObject();
            biomes.add(BiomeInfo.read(biomes.size(), TypedKey.withRegistry(RegistryKey.BIOME, id), object));
        }
        return new BiomeTable(biomes);
    }

    public BiomeInfo get(final int index) {
        return biomes.get(index);
    }

    public @Nullable BiomeInfo find(final String id) {
        return byId.get(WorldgenData.normalize(id));
    }

    public List<BiomeInfo> all() {
        return biomes;
    }

    public int size() {
        return biomes.size();
    }
}