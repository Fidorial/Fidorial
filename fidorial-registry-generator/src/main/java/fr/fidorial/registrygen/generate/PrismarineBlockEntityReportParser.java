package fr.fidorial.registrygen.generate;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.fidorial.registrygen.model.PrismarineBlockEntityDefinition;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses PrismarineJS's {@code minecraft-data} {@code blockEntities.json} report.
 *
 * @since 0.1.0
 */
public final class PrismarineBlockEntityReportParser {

    /**
     * Parses a Prismarine {@code blockEntities.json} file.
     *
     * @param blockEntitiesJson path to Prismarine's {@code blockEntities.json}
     * @return block entity definitions, in report order
     *
     * @throws IOException if the file cannot be read or isn't the expected array shape
     */
    public List<PrismarineBlockEntityDefinition> parse(final Path blockEntitiesJson) throws IOException {

        final JsonArray root;
        try (final Reader reader = Files.newBufferedReader(blockEntitiesJson, StandardCharsets.UTF_8)) {
            root = JsonParser.parseReader(reader).getAsJsonArray();
        }

        final List<PrismarineBlockEntityDefinition> blockEntities = new ArrayList<>();

        for (final JsonElement element : root) {

            final JsonObject blockEntityObject = element.getAsJsonObject();

            final List<String> blocks = new ArrayList<>();
            for (final JsonElement block : blockEntityObject.get("blocks").getAsJsonArray()) {
                blocks.add(block.getAsString());
            }

            blockEntities.add(new PrismarineBlockEntityDefinition(blockEntityObject.get("name").getAsString(), blocks));
        }

        return List.copyOf(blockEntities);
    }
}
