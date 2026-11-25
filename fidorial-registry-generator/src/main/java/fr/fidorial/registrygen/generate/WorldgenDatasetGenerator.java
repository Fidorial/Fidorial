package fr.fidorial.registrygen.generate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * Gathers everything the noise based world generator reads into one dataset: the noises, the density
 * functions, the noise settings, the material rules and conditions, the carvers, the features, the placed
 * features, the block state providers, what each biome generates, the parameter lists of the multi noise biome
 * sources and the worldgen tags.
 *
 * <p>Each section maps an identifier to the file exactly as the data generator wrote it, one entry per line and
 * sorted, so that a version bump shows up as a readable diff. Structures, structure sets, template pools and
 * processor lists are left out: the structure service reads them from data packs.</p>
 *
 * @since 0.1.0
 */
public final class WorldgenDatasetGenerator {

    private static final String WORLDGEN = "worldgen/";
    private static final String JSON_EXTENSION = ".json";
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    /**
     * The registries copied as they are: dataset section name, then the folder under each namespace.
     */
    private static final Map<String, String> REGISTRY_SECTIONS = sections();

    /**
     * The tag folders copied, under {@code <namespace>/tags/}.
     */
    private static final List<String> TAG_FOLDERS = List.of(
            "worldgen/feature",
            "worldgen/placed_feature",
            "worldgen/biome",
            "block"
    );

    /**
     * What a biome keeps in the dataset: the rest of the biome file is sent to clients and lives in the
     * registry dataset.
     */
    private static final List<String> BIOME_GENERATION_FIELDS = List.of(
            "carvers",
            "features",
            "temperature",
            "temperature_modifier",
            "downfall",
            "has_precipitation"
    );

    private static Map<String, String> sections() {
        final Map<String, String> sections = new LinkedHashMap<>();
        sections.put("noises", WORLDGEN + "noise");
        sections.put("density_functions", WORLDGEN + "density_function");
        sections.put("noise_settings", WORLDGEN + "noise_settings");
        sections.put("material_rules", WORLDGEN + "material_rule");
        sections.put("material_conditions", WORLDGEN + "material_condition");
        sections.put("carvers", WORLDGEN + "carver");
        sections.put("features", WORLDGEN + "feature");
        sections.put("placed_features", WORLDGEN + "placed_feature");
        sections.put("block_state_providers", WORLDGEN + "block_state_provider");
        sections.put("processor_lists", WORLDGEN + "processor_list");
        return sections;
    }

    /**
     * Reads the game data and reports and writes the dataset.
     *
     * @param dataDirectory    the {@code data} directory produced by the {@code --server} flag
     * @param reportsDirectory the {@code reports} directory produced by the {@code --reports} flag
     * @param outputFile       the dataset to write, replaced when it exists
     * @return how many entries were written, every section counted
     * @throws IOException if a file cannot be read or the dataset cannot be written
     */
    public int generate(final Path dataDirectory, final Path reportsDirectory, final Path outputFile) throws IOException {
        Objects.requireNonNull(dataDirectory, "dataDirectory");
        Objects.requireNonNull(reportsDirectory, "reportsDirectory");
        Objects.requireNonNull(outputFile, "outputFile");

        final Map<String, Map<String, JsonElement>> dataset = new LinkedHashMap<>();
        for (final Map.Entry<String, String> section : REGISTRY_SECTIONS.entrySet()) {
            dataset.put(section.getKey(), read(dataDirectory, section.getValue()));
        }
        dataset.put("biomes", biomes(dataDirectory));
        // the reports are laid out <namespace>/<name>.json directly under biome_parameters
        dataset.put("biome_parameters", read(reportsDirectory.resolve("biome_parameters"), ""));
        for (final String folder : TAG_FOLDERS) {
            final String name = folder.startsWith(WORLDGEN) ? folder.substring(WORLDGEN.length()) : folder;
            dataset.put("tags/" + name, read(dataDirectory, "tags/" + folder));
        }
        return write(outputFile, dataset);
    }

    /**
     * Writes sections of entries as a JSON object, one entry per line.
     *
     * @return how many entries were written
     */
    private static int write(final Path file, final Map<String, Map<String, JsonElement>> sections) throws IOException {
        final Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        int total = 0;
        try (final Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writer.write("{\n");
            int sectionIndex = 0;
            for (final Map.Entry<String, Map<String, JsonElement>> section : sections.entrySet()) {
                writer.write("  ");
                writer.write(GSON.toJson(section.getKey()));
                writer.write(": {\n");
                int written = 0;
                for (final Map.Entry<String, JsonElement> entry : section.getValue().entrySet()) {
                    writer.write("    ");
                    writer.write(GSON.toJson(entry.getKey()));
                    writer.write(": ");
                    writer.write(GSON.toJson(entry.getValue()));
                    writer.write(++written < section.getValue().size() ? ",\n" : "\n");
                }
                total += written;
                writer.write(++sectionIndex < sections.size() ? "  },\n" : "  }\n");
            }
            writer.write("}\n");
        }
        return total;
    }

    private static Map<String, JsonElement> biomes(final Path dataDirectory) throws IOException {
        final Map<String, JsonElement> biomes = new TreeMap<>();
        for (final Map.Entry<String, JsonElement> biome : read(dataDirectory, WORLDGEN + "biome").entrySet()) {
            final JsonObject full = biome.getValue().getAsJsonObject();
            final JsonObject kept = new JsonObject();
            for (final String field : BIOME_GENERATION_FIELDS) {
                if (full.has(field)) {
                    kept.add(field, full.get(field));
                }
            }
            if (!kept.has("features")) {
                kept.add("features", new JsonArray());
            }
            biomes.put(biome.getKey(), kept);
        }
        return biomes;
    }

    /**
     * @param root      a directory holding one folder per namespace
     * @param directory the folder under each namespace, such as {@code worldgen/noise}
     * @return every file of that folder, keyed by namespaced identifier and sorted
     * @throws IOException if a file cannot be read
     */
    private static Map<String, JsonElement> read(final Path root, final String directory) throws IOException {
        final Map<String, JsonElement> files = new TreeMap<>();
        if (!Files.isDirectory(root)) {
            return files;
        }
        try (final Stream<Path> namespaces = Files.list(root)) {
            for (final Path namespace : namespaces.sorted().toList()) {
                final Path folder = namespace.resolve(directory);
                if (!Files.isDirectory(folder)) {
                    continue;
                }
                try (final Stream<Path> walk = Files.walk(folder)) {
                    for (final Path file : walk.filter(path -> Files.isRegularFile(path)
                            && path.toString().endsWith(JSON_EXTENSION)).sorted().toList()) {
                        final String relative = folder.relativize(file).toString().replace('\\', '/');
                        final String id = namespace.getFileName() + ":"
                                + relative.substring(0, relative.length() - JSON_EXTENSION.length());
                        try (final Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                            files.put(id, JsonParser.parseReader(reader));
                        }
                    }
                }
            }
        }
        return files;
    }
}