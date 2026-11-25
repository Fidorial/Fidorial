package fr.euphyllia.fidorial.server.world.generation.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The world generation definitions read by the noise generator: noises, density functions, noise settings,
 * material rules, carvers, features and the rest, each keyed by its namespaced identifier.
 *
 * <p>The bundled dataset comes from the server jar of the supported version. Data packs can add entries or
 * replace bundled ones with {@link #override}. Immutable once built, safe to share between threads.</p>
 */
public final class WorldgenData {

    private static final String BUNDLED = "/fidorial-data/worldgen.json";
    private static final char TAG_MARK = '#';

    /**
     * The kinds of definitions.
     */
    public enum Section {
        NOISE("noises", "worldgen/noise"),
        DENSITY_FUNCTION("density_functions", "worldgen/density_function"),
        NOISE_SETTINGS("noise_settings", "worldgen/noise_settings"),
        MATERIAL_RULE("material_rules", "worldgen/material_rule"),
        MATERIAL_CONDITION("material_conditions", "worldgen/material_condition"),
        CARVER("carvers", "worldgen/carver"),
        FEATURE("features", "worldgen/feature"),
        PLACED_FEATURE("placed_features", "worldgen/placed_feature"),
        BLOCK_STATE_PROVIDER("block_state_providers", "worldgen/block_state_provider"),
        PROCESSOR_LIST("processor_lists", "worldgen/processor_list"),
        BIOME("biomes", "worldgen/biome"),
        BIOME_PARAMETERS("biome_parameters", null),
        FEATURE_TAG("tags/feature", "tags/worldgen/feature"),
        PLACED_FEATURE_TAG("tags/placed_feature", "tags/worldgen/placed_feature"),
        BIOME_TAG("tags/biome", "tags/worldgen/biome"),
        BLOCK_TAG("tags/block", "tags/block");

        private final String datasetName;
        private final @Nullable String folder;

        Section(final String datasetName, final @Nullable String folder) {
            this.datasetName = datasetName;
            this.folder = folder;
        }

        /**
         * {@return the folder under a namespace of a data pack holding this kind of file, {@code null} for the
         * sections that only come with the server}
         */
        public @Nullable String folder() {
            return folder;
        }
    }

    private final Map<Section, Map<String, JsonElement>> entries;

    private WorldgenData(final Map<Section, Map<String, JsonElement>> entries) {
        this.entries = entries;
    }

    /**
     * {@return the definitions shipped with the server}
     *
     * @throws UncheckedIOException if the dataset is missing or unreadable
     */
    public static WorldgenData bundled() {
        try (final InputStream stream = WorldgenData.class.getResourceAsStream(BUNDLED)) {
            if (stream == null) {
                throw new IOException("The world generation dataset " + BUNDLED + " is missing from the server jar");
            }
            try (final Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                return read(JsonParser.parseReader(reader).getAsJsonObject());
            }
        } catch (final IOException failure) {
            throw new UncheckedIOException(failure);
        }
    }

    /**
     * {@return the definitions of a dataset object, laid out as {@code section -> id -> definition}}
     */
    public static WorldgenData read(final JsonObject dataset) {
        final Map<Section, Map<String, JsonElement>> entries = new EnumMap<>(Section.class);
        for (final Section section : Section.values()) {
            final Map<String, JsonElement> values = new HashMap<>();
            final JsonElement found = dataset.get(section.datasetName);
            if (found != null && found.isJsonObject()) {
                for (final Map.Entry<String, JsonElement> entry : found.getAsJsonObject().entrySet()) {
                    values.put(normalize(entry.getKey()), entry.getValue());
                }
            }
            entries.put(section, values);
        }
        return new WorldgenData(entries);
    }

    /**
     * {@return a copy with some definitions added or replaced}
     *
     * @param additions definitions by section, then by identifier
     */
    public WorldgenData override(final Map<Section, Map<String, JsonElement>> additions) {
        final Map<Section, Map<String, JsonElement>> merged = new EnumMap<>(Section.class);
        for (final Section section : Section.values()) {
            final Map<String, JsonElement> values = new HashMap<>(entries.get(section));
            final Map<String, JsonElement> added = additions.get(section);
            if (added != null) {
                for (final Map.Entry<String, JsonElement> entry : added.entrySet()) {
                    values.put(normalize(entry.getKey()), entry.getValue());
                }
            }
            merged.put(section, values);
        }
        return new WorldgenData(merged);
    }

    /**
     * {@return an identifier with the default namespace when it has none}
     */
    public static String normalize(final String id) {
        final String trimmed = id.startsWith(String.valueOf(TAG_MARK)) ? id.substring(1) : id;
        return Key.key(trimmed).asString();
    }

    /**
     * {@return a definition, or {@code null} when there is none by that identifier}
     */
    public @Nullable JsonElement find(final Section section, final String id) {
        return entries.get(section).get(normalize(id));
    }

    /**
     * {@return a definition}
     *
     * @throws IllegalArgumentException when there is none by that identifier
     */
    public JsonElement get(final Section section, final String id) {
        final JsonElement found = find(section, id);
        if (found == null) {
            throw new IllegalArgumentException("Unknown " + section.datasetName + " entry " + id);
        }
        return found;
    }

    /**
     * {@return the identifiers of a section}
     */
    public Set<String> ids(final Section section) {
        return Collections.unmodifiableSet(entries.get(section).keySet());
    }

    /**
     * Resolves a tag of a tag section, tags inside it included, into identifiers.
     *
     * @param tagSection the section the tag lives in, such as {@link Section#PLACED_FEATURE_TAG}
     * @param tag        the tag identifier, with or without the leading {@code #}
     * @return the identifiers in the order the tag lists them, without duplicates
     */
    public List<String> tag(final Section tagSection, final String tag) {
        final Set<String> values = new LinkedHashSet<>();
        collect(tagSection, normalize(tag), values, new LinkedHashSet<>());
        return new ArrayList<>(values);
    }

    private void collect(final Section tagSection, final String tag, final Set<String> into, final Set<String> visiting) {
        if (!visiting.add(tag)) {
            return;
        }
        final JsonElement definition = find(tagSection, tag);
        if (definition == null || !definition.isJsonObject()) {
            return;
        }
        final JsonArray values = definition.getAsJsonObject().getAsJsonArray("values");
        if (values == null) {
            return;
        }
        for (final JsonElement value : values) {
            final String id = value.isJsonObject() ? value.getAsJsonObject().get("id").getAsString() : value.getAsString();
            if (id.charAt(0) == TAG_MARK) {
                collect(tagSection, normalize(id), into, visiting);
            } else {
                into.add(normalize(id));
            }
        }
    }
}
