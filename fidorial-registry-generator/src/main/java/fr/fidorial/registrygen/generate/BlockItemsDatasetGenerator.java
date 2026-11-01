package fr.fidorial.registrygen.generate;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Writes which block each item places, from the outputs of Mojang's data generator.
 *
 * <p>The output maps each item to an object: {@code block}, and for an item with a wall variant,
 * {@code wall} and {@code attachment} ({@code down} or {@code up}). One entry per line, sorted.</p>
 *
 * @since 0.1.0
 */
public final class BlockItemsDatasetGenerator {

    private static final String NAMESPACE = "minecraft:";
    private static final String BLOCK_TRANSLATION = "block.minecraft.";
    private static final Pattern LOOT_ITEM = Pattern.compile("\"name\"\\s*:\\s*\"minecraft:([a-z0-9_]+)\"");

    /**
     * @param blocksReport          Mojang's {@code reports/blocks.json}
     * @param itemComponentsReports Mojang's per-item components directory, {@code reports/minecraft/components/item}
     * @param blockLootTables       Mojang's block loot tables, {@code data/minecraft/loot_table/blocks}
     * @param blockTags             Mojang's block tags, {@code data/minecraft/tags/block}
     * @param outputFile            the dataset to write, replaced when it exists
     * @return how many items were written
     * @throws IOException if an input cannot be read or the dataset cannot be written
     */
    public int generate(final Path blocksReport, final Path itemComponentsReports, final Path blockLootTables,
                        final Path blockTags, final Path outputFile) throws IOException {
        Objects.requireNonNull(blocksReport, "blocksReport");
        Objects.requireNonNull(itemComponentsReports, "itemComponentsReports");
        Objects.requireNonNull(blockLootTables, "blockLootTables");
        Objects.requireNonNull(blockTags, "blockTags");
        Objects.requireNonNull(outputFile, "outputFile");

        final Set<String> blocks = new HashSet<>();
        for (final String key : parse(blocksReport).getAsJsonObject().keySet()) {
            blocks.add(strip(key));
        }

        // items named after the block they place
        final Map<String, String> itemNames = itemNames(itemComponentsReports);
        final Map<String, String> placed = new TreeMap<>();
        for (final Map.Entry<String, String> entry : itemNames.entrySet()) {
            final String name = entry.getValue();
            if (name.startsWith(BLOCK_TRANSLATION)) {
                final String block = name.substring(BLOCK_TRANSLATION.length());
                if (blocks.contains(block) && !"air".equals(block)) {
                    placed.put(entry.getKey(), block);
                }
            }
        }
        final Set<String> claimed = new HashSet<>(placed.values());

        // items named after themselves: the single item a block no item is named after drops
        final Map<String, List<String>> blocksDropping = new HashMap<>();
        final Map<String, Set<String>> dropsOfBlock = new HashMap<>();
        for (final String block : blocks) {
            final Path lootTable = blockLootTables.resolve(block + ".json");
            if (claimed.contains(block) || !Files.isRegularFile(lootTable)) {
                continue;
            }
            final Set<String> drops = new HashSet<>();
            final Matcher matcher = LOOT_ITEM.matcher(Files.readString(lootTable, StandardCharsets.UTF_8));
            while (matcher.find()) {
                if (itemNames.containsKey(matcher.group(1)) && !placed.containsKey(matcher.group(1))) {
                    drops.add(matcher.group(1));
                }
            }
            dropsOfBlock.put(block, drops);
            for (final String item : drops) {
                blocksDropping.computeIfAbsent(item, _ -> new ArrayList<>()).add(block);
            }
        }
        for (final Map.Entry<String, List<String>> entry : blocksDropping.entrySet()) {
            final List<String> candidates = entry.getValue();
            if (candidates.size() == 1 && dropsOfBlock.get(candidates.getFirst()).size() == 1) {
                placed.put(entry.getKey(), candidates.getFirst());
                claimed.add(candidates.getFirst());
            }
        }

        // wall variants, by name
        final Map<String, String> wallOf = new HashMap<>();
        for (final String block : blocks) {
            if (claimed.contains(block)) {
                continue;
            }
            final String standing;
            if (block.contains("_wall_")) {
                standing = block.replaceFirst("_wall_", "_");
            } else if (block.startsWith("wall_")) {
                standing = block.substring("wall_".length());
            } else {
                continue;
            }
            if (claimed.contains(standing)) {
                wallOf.put(standing, block);
            }
        }
        final Set<String> hanging = new BlockTags(blockTags).blocks("ceiling_hanging_signs");

        final StringBuilder json = new StringBuilder("{\n");
        boolean first = true;
        for (final Map.Entry<String, String> entry : placed.entrySet()) {
            if (!first) {
                json.append(",\n");
            }
            first = false;
            final String block = entry.getValue();
            json.append("  \"").append(NAMESPACE).append(entry.getKey()).append("\": {\"block\": \"").append(NAMESPACE).append(block).append('"');
            final String wall = wallOf.get(block);
            if (wall != null) {
                json.append(", \"wall\": \"").append(NAMESPACE).append(wall).append("\", \"attachment\": \"")
                        .append(hanging.contains(block) ? "up" : "down").append('"');
            }
            json.append('}');
        }
        json.append("\n}\n");
        final Path parent = outputFile.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(outputFile, json.toString(), StandardCharsets.UTF_8);
        return placed.size();
    }

    /**
     * {@return the translation key of the name of every item, by item}
     */
    private static Map<String, String> itemNames(final Path directory) throws IOException {
        if (!Files.isDirectory(directory)) {
            throw new IOException("No item components report in " + directory + ": run the data generator with \"--reports\".");
        }
        final Map<String, String> names = new HashMap<>();
        try (final Stream<Path> files = Files.list(directory)) {
            for (final Path file : files.filter(path -> path.toString().endsWith(".json")).toList()) {
                final String item = file.getFileName().toString().replace(".json", "");
                final JsonObject components = parse(file).getAsJsonObject().getAsJsonObject("components");
                final JsonElement name = components == null ? null : components.get("minecraft:item_name");
                final String translation = name != null && name.isJsonObject() && name.getAsJsonObject().has("translate")
                        ? name.getAsJsonObject().get("translate").getAsString()
                        : "";
                names.put(item, translation);
            }
        }
        return names;
    }

    private static String strip(final String id) {
        return id.startsWith(NAMESPACE) ? id.substring(NAMESPACE.length()) : id;
    }

    private static JsonElement parse(final Path file) throws IOException {
        try (final Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader);
        }
    }
}