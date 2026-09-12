package fr.fidorial.registrygen.generate;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.fidorial.registrygen.model.PrismarineEntityDefinition;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Parses PrismarineJS's {@code minecraft-data} {@code entities.json} report.
 *
 * @since 0.1.0
 */
public final class PrismarineEntityReportParser {

    private static final float DEFAULT_WIDTH = 0.6f;
    private static final float DEFAULT_HEIGHT = 1.8f;
    private static final String DEFAULT_TYPE = "other";

    /**
     * Parses a Prismarine {@code entities.json} file into a name-keyed lookup.
     *
     * @param entitiesJson path to Prismarine's {@code entities.json}
     * @return entity definitions keyed by plain entity name, in report order
     *
     * @throws IOException if the file cannot be read or isn't the expected array shape
     */
    public Map<String, PrismarineEntityDefinition> parse(final Path entitiesJson) throws IOException {

        final JsonArray root;
        try (final Reader reader = Files.newBufferedReader(entitiesJson, StandardCharsets.UTF_8)) {
            root = JsonParser.parseReader(reader).getAsJsonArray();
        }

        final Map<String, PrismarineEntityDefinition> entities = new LinkedHashMap<>();

        for (final JsonElement element : root) {

            final JsonObject entityObject = element.getAsJsonObject();
            final String name = entityObject.get("name").getAsString();

            final String displayName = entityObject.has("displayName")
                    ? entityObject.get("displayName").getAsString()
                    : name;

            final float width = entityObject.has("width")
                    ? entityObject.get("width").getAsFloat()
                    : DEFAULT_WIDTH;

            final float height = entityObject.has("height")
                    ? entityObject.get("height").getAsFloat()
                    : DEFAULT_HEIGHT;

            final String type = entityObject.has("type")
                    ? entityObject.get("type").getAsString()
                    : DEFAULT_TYPE;

            entities.put(name, new PrismarineEntityDefinition(name, displayName, width, height, type));
        }

        return Map.copyOf(entities);
    }
}
