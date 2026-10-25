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

/**
 * Writes the dataset the server mines blocks with.
 *
 * <p>The output is a JSON object with two members. {@code blocks} maps a block to its
 * {@code hardness} ({@code -1} when it cannot be broken), its blast {@code resistance} and whether
 * it {@code requires_tool} to drop anything, all from PrismarineJS's {@code minecraft-data}, which
 * Mojang's reports do not cover. {@code tools} maps an item to its {@code minecraft:tool} component, exactly as Mojang's
 * item components report gives it. One entry per line, sorted by identifier.</p>
 *
 * @since 0.1.0
 */
public final class BlockMiningDatasetGenerator {

    private static final String NAMESPACE = "minecraft:";
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    /**
     * @param prismarineBlocks      Prismarine's {@code blocks.json}
     * @param itemComponentsReport  Mojang's item components report: the per-item directory
     *                              ({@code reports/minecraft/components/item}) or a single file
     * @param outputFile            the dataset to write, replaced when it exists
     * @return how many blocks were written
     * @throws IOException if a report cannot be read or the dataset cannot be written
     */
    public int generate(final Path prismarineBlocks, final Path itemComponentsReport, final Path outputFile)
            throws IOException {
        Objects.requireNonNull(prismarineBlocks, "prismarineBlocks");
        Objects.requireNonNull(itemComponentsReport, "itemComponentsReport");
        Objects.requireNonNull(outputFile, "outputFile");

        final Map<String, JsonObject> blocks = blocks(parse(prismarineBlocks));
        final Map<String, JsonObject> tools = new TreeMap<>();
        for (final Map.Entry<String, JsonObject> tool : new ItemComponentsReportParser().parseTools(itemComponentsReport).entrySet()) {
            tools.put(NAMESPACE + tool.getKey(), tool.getValue());
        }
        if (tools.isEmpty()) {
            throw new IOException("No minecraft:tool component in " + itemComponentsReport);
        }

        final Path parent = outputFile.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (final Writer writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8)) {
            writer.write("{\n");
            writeSection(writer, "blocks", blocks, false);
            writeSection(writer, "tools", tools, true);
            writer.write("}\n");
        }
        return blocks.size();
    }

    private static void writeSection(final Writer writer, final String name, final Map<String, JsonObject> entries,
                                     final boolean last) throws IOException {
        writer.write("  ");
        writer.write(GSON.toJson(name));
        writer.write(": {\n");
        int written = 0;
        for (final Map.Entry<String, JsonObject> entry : entries.entrySet()) {
            writer.write("    ");
            writer.write(GSON.toJson(entry.getKey()));
            writer.write(": ");
            writer.write(GSON.toJson(entry.getValue()));
            writer.write(++written < entries.size() ? ",\n" : "\n");
        }
        writer.write(last ? "  }\n" : "  },\n");
    }

    private static Map<String, JsonObject> blocks(final JsonElement report) {
        final Map<String, JsonObject> result = new TreeMap<>();
        for (final JsonElement element : report.getAsJsonArray()) {
            final JsonObject block = element.getAsJsonObject();
            final boolean diggable = !block.has("diggable") || block.get("diggable").getAsBoolean();
            final JsonElement hardness = block.get("hardness");
            final JsonElement harvestTools = block.get("harvestTools");
            final JsonElement resistance = block.get("resistance");

            final JsonObject json = new JsonObject();
            json.addProperty("hardness", !diggable || hardness == null || hardness.isJsonNull() ? -1.0f : hardness.getAsFloat());
            json.addProperty("resistance", resistance == null || resistance.isJsonNull() ? 0.0f : resistance.getAsFloat());
            json.addProperty("requires_tool", harvestTools != null && harvestTools.isJsonObject());
            result.put(NAMESPACE + block.get("name").getAsString(), json);
        }
        return result;
    }

    private static JsonElement parse(final Path file) throws IOException {
        try (final Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader);
        } catch (final RuntimeException exception) {
            throw new IOException("Unable to parse " + file, exception);
        }
    }
}