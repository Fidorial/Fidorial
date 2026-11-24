package fr.fidorial.registrygen.generate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/**
 * Writes the dataset the server fights with: the default components of every item that take part
 * in a fight.
 *
 * <p>The output is a JSON object with one member, {@code items}, which maps an item to the subset
 * of its default components listed in {@link #COMBAT_COMPONENTS}, exactly as Mojang's item
 * components report gives them. An item with none of them is left out, as is an empty
 * {@code minecraft:attribute_modifiers} list. One item per line, sorted by identifier.</p>
 *
 * @since 0.1.0
 */
public final class ItemCombatDatasetGenerator {

    /**
     * The components the server reads to settle a hit, a block, a shot or a death.
     */
    public static final Set<String> COMBAT_COMPONENTS = Set.of(
            "minecraft:attack_range",
            "minecraft:attribute_modifiers",
            "minecraft:blocks_attacks",
            "minecraft:damage_type",
            "minecraft:death_protection",
            "minecraft:kinetic_weapon",
            "minecraft:minimum_attack_charge",
            "minecraft:piercing_weapon",
            "minecraft:use_cooldown",
            "minecraft:weapon");

    private static final String NAMESPACE = "minecraft:";
    private static final String ATTRIBUTE_MODIFIERS = "minecraft:attribute_modifiers";
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    /**
     * @param itemComponentsReport Mojang's item components report: the per-item directory
     *                             ({@code reports/minecraft/components/item}) or a single file
     * @param outputFile           the dataset to write, replaced when it exists
     * @return how many items were written
     * @throws IOException if the report cannot be read or the dataset cannot be written
     */
    public int generate(final Path itemComponentsReport, final Path outputFile) throws IOException {
        Objects.requireNonNull(itemComponentsReport, "itemComponentsReport");
        Objects.requireNonNull(outputFile, "outputFile");

        final Map<String, JsonObject> items = new TreeMap<>();
        for (final Map.Entry<String, JsonObject> item : new ItemComponentsReportParser().parseComponents(itemComponentsReport).entrySet()) {
            final JsonObject kept = new JsonObject();
            for (final Map.Entry<String, JsonElement> component : new TreeMap<>(item.getValue().asMap()).entrySet()) {
                if (!COMBAT_COMPONENTS.contains(component.getKey()) || isEmptyModifierList(component)) {
                    continue;
                }
                kept.add(component.getKey(), component.getValue());
            }
            if (!kept.isEmpty()) {
                items.put(NAMESPACE + item.getKey(), kept);
            }
        }
        if (items.isEmpty()) {
            throw new IOException("No combat component in " + itemComponentsReport);
        }

        final Path parent = outputFile.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (final Writer writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8)) {
            writer.write("{\n  \"items\": {\n");
            int written = 0;
            for (final Map.Entry<String, JsonObject> entry : items.entrySet()) {
                writer.write("    ");
                writer.write(GSON.toJson(entry.getKey()));
                writer.write(": ");
                writer.write(GSON.toJson(entry.getValue()));
                writer.write(++written < items.size() ? ",\n" : "\n");
            }
            writer.write("  }\n}\n");
        }
        return items.size();
    }

    private static boolean isEmptyModifierList(final Map.Entry<String, JsonElement> component) {
        return ATTRIBUTE_MODIFIERS.equals(component.getKey())
                && component.getValue() instanceof final JsonArray modifiers
                && modifiers.isEmpty();
    }
}
