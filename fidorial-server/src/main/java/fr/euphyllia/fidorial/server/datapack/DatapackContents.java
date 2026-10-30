package fr.euphyllia.fidorial.server.datapack;

import com.google.gson.JsonElement;
import fr.fidorial.world.structure.Datapack;
import net.kyori.adventure.key.Key;

import java.util.List;
import java.util.Map;

public record DatapackContents(
        List<Datapack> packs,
        Map<Key, byte[]> templates,
        Map<Key, JsonElement> structures,
        Map<Key, JsonElement> structureSets,
        Map<Key, JsonElement> templatePools,
        Map<Key, JsonElement> processorLists,
        Map<String, Map<Key, List<TagFile>>> tags
) {

    public static DatapackContents empty() {
        return new DatapackContents(List.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of());
    }

    /**
     * {@return the tag files of a registry, by tag, in data pack order}
     *
     * @param registry the path of the registry, such as {@code worldgen/biome}
     */
    public Map<Key, List<TagFile>> tags(final String registry) {
        return tags.getOrDefault(registry, Map.of());
    }
}
