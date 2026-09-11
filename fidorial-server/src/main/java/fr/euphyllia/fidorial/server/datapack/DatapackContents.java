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
        Map<Key, List<TagFile>> biomeTags,
        Map<Key, List<TagFile>> blockTags
) {

    public static DatapackContents empty() {
        return new DatapackContents(List.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of());
    }
}
