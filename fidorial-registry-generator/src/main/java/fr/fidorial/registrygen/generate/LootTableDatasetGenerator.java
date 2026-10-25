package fr.fidorial.registrygen.generate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * Bundles the vanilla loot tables, and the predicates they refer to by name, into one dataset the
 * server reads at startup.
 *
 * <p>The output is a JSON object with two members, {@code loot_tables} and {@code predicates},
 * each mapping an identifier to the file exactly as Mojang wrote it, one per line and sorted by
 * identifier so that a version bump shows up as a readable diff.</p>
 *
 * @since 0.1.0
 */
public final class LootTableDatasetGenerator {

    private static final String LOOT_TABLE_DIRECTORY = "loot_table";
    private static final String PREDICATE_DIRECTORY = "predicate";
    private static final String JSON_EXTENSION = ".json";
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    /**
     * Reads every {@code <namespace>/loot_table/**.json} and {@code <namespace>/predicate/**.json}
     * of the vanilla data and writes the dataset.
     *
     * @param vanillaDataDirectory the {@code data} directory produced by the {@code --server} flag
     * @param outputFile           the dataset to write, replaced when it exists
     * @return how many loot tables were written
     * @throws IOException if a file cannot be read or the dataset cannot be written
     */
    public int generate(final Path vanillaDataDirectory, final Path outputFile) throws IOException {
        Objects.requireNonNull(vanillaDataDirectory, "vanillaDataDirectory");
        Objects.requireNonNull(outputFile, "outputFile");

        final Map<String, Map<String, JsonObject>> sections = new LinkedHashMap<>();
        sections.put("loot_tables", read(vanillaDataDirectory, LOOT_TABLE_DIRECTORY));
        sections.put("predicates", read(vanillaDataDirectory, PREDICATE_DIRECTORY));

        final Path parent = outputFile.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (final Writer writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8)) {
            writer.write("{\n");
            int sectionIndex = 0;
            for (final Map.Entry<String, Map<String, JsonObject>> section : sections.entrySet()) {
                writer.write("  ");
                writer.write(GSON.toJson(section.getKey()));
                writer.write(": {\n");
                int written = 0;
                for (final Map.Entry<String, JsonObject> entry : section.getValue().entrySet()) {
                    writer.write("    ");
                    writer.write(GSON.toJson(entry.getKey()));
                    writer.write(": ");
                    writer.write(GSON.toJson(entry.getValue()));
                    writer.write(++written < section.getValue().size() ? ",\n" : "\n");
                }
                writer.write(++sectionIndex < sections.size() ? "  },\n" : "  }\n");
            }
            writer.write("}\n");
        }
        return sections.get("loot_tables").size();
    }

    /**
     * @param vanillaDataDirectory the {@code data} directory produced by the {@code --server} flag
     * @param directory            the directory under each namespace, such as {@code loot_table}
     * @return every file of that directory, keyed by namespaced identifier and sorted
     * @throws IOException if a file cannot be read
     */
    public Map<String, JsonObject> read(final Path vanillaDataDirectory, final String directory) throws IOException {
        final Map<String, JsonObject> files = new TreeMap<>();
        if (!Files.isDirectory(vanillaDataDirectory)) {
            return files;
        }
        try (final Stream<Path> namespaces = Files.list(vanillaDataDirectory)) {
            for (final Path namespace : namespaces.sorted().toList()) {
                final Path root = namespace.resolve(directory);
                if (Files.isDirectory(root)) {
                    readNamespace(namespace.getFileName().toString(), root, files);
                }
            }
        }
        return files;
    }

    private static void readNamespace(final String namespace, final Path root,
                                      final Map<String, JsonObject> files) throws IOException {
        try (final Stream<Path> walk = Files.walk(root)) {
            for (final Path file : walk.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(JSON_EXTENSION))
                    .sorted()
                    .toList()) {
                final String relative = root.relativize(file).toString().replace('\\', '/');
                final String path = relative.substring(0, relative.length() - JSON_EXTENSION.length());
                files.put(namespace + ":" + path, parse(file));
            }
        }
    }

    private static JsonObject parse(final Path file) throws IOException {
        try (final Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            final JsonElement element = JsonParser.parseReader(reader);
            if (!element.isJsonObject()) {
                throw new IOException("The file " + file + " is not a JSON object");
            }
            return element.getAsJsonObject();
        } catch (final RuntimeException exception) {
            throw new IOException("Unable to parse " + file, exception);
        }
    }
}