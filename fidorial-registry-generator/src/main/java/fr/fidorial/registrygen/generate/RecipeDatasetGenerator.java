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
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * Bundles the vanilla recipe files dumped by Mojang's data generator into one dataset the server
 * reads at startup.
 *
 * <p>The output is a single JSON object, recipe identifier &rarr; the recipe exactly as Mojang
 * wrote it, one recipe per line and sorted by identifier so that a version bump shows up as a
 * readable diff. Every recipe type is kept, crafting or not: the server picks the ones it knows
 * how to run.</p>
 *
 * @since 0.1.0
 */
public final class RecipeDatasetGenerator {

    private static final String RECIPE_DIRECTORY = "recipe";
    private static final String JSON_EXTENSION = ".json";
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    /**
     * Reads every {@code <namespace>/recipe/**.json} of the vanilla data and writes the dataset.
     *
     * @param vanillaDataDirectory the {@code data} directory produced by the {@code --server} flag
     * @param outputFile           the dataset to write, replaced when it exists
     * @return how many recipes were written
     * @throws IOException if a recipe cannot be read or the dataset cannot be written
     */
    public int generate(final Path vanillaDataDirectory, final Path outputFile) throws IOException {
        Objects.requireNonNull(vanillaDataDirectory, "vanillaDataDirectory");
        Objects.requireNonNull(outputFile, "outputFile");

        final Map<String, JsonObject> recipes = read(vanillaDataDirectory);

        final Path parent = outputFile.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (final Writer writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8)) {
            writer.write("{\n");
            int written = 0;
            for (final Map.Entry<String, JsonObject> recipe : recipes.entrySet()) {
                writer.write("  ");
                writer.write(GSON.toJson(recipe.getKey()));
                writer.write(": ");
                writer.write(GSON.toJson(recipe.getValue()));
                writer.write(++written < recipes.size() ? ",\n" : "\n");
            }
            writer.write("}\n");
        }
        return recipes.size();
    }

    /**
     * @param vanillaDataDirectory the {@code data} directory produced by the {@code --server} flag
     * @return every recipe, keyed by namespaced identifier and sorted
     * @throws IOException if a recipe cannot be read
     */
    public Map<String, JsonObject> read(final Path vanillaDataDirectory) throws IOException {
        final Map<String, JsonObject> recipes = new TreeMap<>();
        if (!Files.isDirectory(vanillaDataDirectory)) {
            return recipes;
        }
        try (final Stream<Path> namespaces = Files.list(vanillaDataDirectory)) {
            for (final Path namespace : namespaces.sorted().toList()) {
                final Path recipeDirectory = namespace.resolve(RECIPE_DIRECTORY);
                if (Files.isDirectory(recipeDirectory)) {
                    readNamespace(namespace.getFileName().toString(), recipeDirectory, recipes);
                }
            }
        }
        return recipes;
    }

    private static void readNamespace(final String namespace, final Path recipeDirectory,
                                      final Map<String, JsonObject> recipes) throws IOException {
        try (final Stream<Path> files = Files.walk(recipeDirectory)) {
            for (final Path file : files.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(JSON_EXTENSION))
                    .sorted()
                    .toList()) {
                final String relative = recipeDirectory.relativize(file).toString().replace('\\', '/');
                final String path = relative.substring(0, relative.length() - JSON_EXTENSION.length());
                recipes.put(namespace + ":" + path, parse(file));
            }
        }
    }

    private static JsonObject parse(final Path file) throws IOException {
        try (final Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            final JsonElement element = JsonParser.parseReader(reader);
            if (!element.isJsonObject()) {
                throw new IOException("The recipe " + file + " is not a JSON object");
            }
            return element.getAsJsonObject();
        } catch (final RuntimeException exception) {
            throw new IOException("Unable to parse the recipe " + file, exception);
        }
    }
}
