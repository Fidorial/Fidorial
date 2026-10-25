package fr.fidorial.registrygen.generate;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.fidorial.registrygen.GenerationUtils;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * Parses Mojang's report of the default components of every item, written by the data
 * generator's {@code --reports} flag.
 *
 * @since 0.1.0
 */
public final class ItemComponentsReportParser {

    private static final String COMPONENTS = "components";
    private static final String EQUIPPABLE = "minecraft:equippable";
    private static final String SLOT = "slot";
    private static final String TOOL = "minecraft:tool";
    private static final String JSON_EXTENSION = ".json";

    /**
     * @param report the per-item directory ({@code reports/minecraft/components/item}) or the
     *               legacy {@code reports/items.json}; nothing is read when it does not exist
     * @return the slot each equippable item is worn in ({@code head}, {@code chest}, {@code body}...),
     *         keyed by plain item name and sorted
     * @throws IOException if a file cannot be read or isn't the expected shape
     */
    public Map<String, String> parseEquipmentSlots(final Path report) throws IOException {
        final Map<String, String> slots = new TreeMap<>();
        for (final Map.Entry<String, JsonObject> item : parseComponents(report).entrySet()) {
            final JsonElement equippable = item.getValue().get(EQUIPPABLE);
            if (equippable != null && equippable.isJsonObject() && equippable.getAsJsonObject().has(SLOT)) {
                slots.put(item.getKey(), equippable.getAsJsonObject().get(SLOT).getAsString());
            }
        }
        return slots;
    }

    /**
     * @param report the per-item directory ({@code reports/minecraft/components/item}) or the
     *               legacy {@code reports/items.json}; nothing is read when it does not exist
     * @return the {@code minecraft:tool} component of every item that has one, keyed by plain item
     *         name and sorted
     * @throws IOException if a file cannot be read or isn't the expected shape
     */
    public Map<String, JsonObject> parseTools(final Path report) throws IOException {
        final Map<String, JsonObject> tools = new TreeMap<>();
        for (final Map.Entry<String, JsonObject> item : parseComponents(report).entrySet()) {
            final JsonElement tool = item.getValue().get(TOOL);
            if (tool != null && tool.isJsonObject()) {
                tools.put(item.getKey(), tool.getAsJsonObject());
            }
        }
        return tools;
    }

    /**
     * @param report the per-item directory ({@code reports/minecraft/components/item}) or the
     *               legacy {@code reports/items.json}; nothing is read when it does not exist
     * @return the default components of every item, keyed by plain item name and sorted
     * @throws IOException if a file cannot be read or isn't the expected shape
     */
    public Map<String, JsonObject> parseComponents(final Path report) throws IOException {
        Objects.requireNonNull(report, "report");

        final Map<String, JsonObject> items = new TreeMap<>();
        if (Files.isDirectory(report)) {
            final List<Path> files;
            try (final Stream<Path> walk = Files.walk(report)) {
                files = walk.filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().endsWith(JSON_EXTENSION))
                        .sorted()
                        .toList();
            }
            for (final Path file : files) {
                final String relative = report.relativize(file).toString().replace('\\', '/');
                final String item = relative.substring(0, relative.length() - JSON_EXTENSION.length());
                addComponents(items, item, read(file));
            }
        } else if (Files.isRegularFile(report)) {
            for (final Map.Entry<String, JsonElement> item : read(report).getAsJsonObject().entrySet()) {
                addComponents(items, GenerationUtils.path(item.getKey()), item.getValue());
            }
        }
        return items;
    }

    private static JsonElement read(final Path file) throws IOException {
        try (final Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader);
        } catch (final RuntimeException exception) {
            throw new IOException("Failed to parse the item components report " + file, exception);
        }
    }

    private static void addComponents(final Map<String, JsonObject> items, final String item, final JsonElement json) {
        final JsonObject components = components(json);
        if (components != null) {
            items.put(item, components);
        }
    }

    /**
     * The report wraps the components in a {@code components} object; tolerate a bare map too.
     */
    private static JsonObject components(final JsonElement item) {
        if (!item.isJsonObject()) {
            return null;
        }
        final JsonObject object = item.getAsJsonObject();
        return object.has(COMPONENTS) && object.get(COMPONENTS).isJsonObject()
                ? object.getAsJsonObject(COMPONENTS)
                : object;
    }
}
