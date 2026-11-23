package fr.euphyllia.fidorial.server.registry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import fr.euphyllia.fidorial.server.datapack.TagFile;
import fr.euphyllia.fidorial.server.registry.data.FrozenRegistries;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Reads the vanilla registries: the data registries from the generated dataset, the built-in ones
 * from {@link FrozenRegistries}.
 *
 * <p>The tags of the game are completed with those of Fidorial, read from
 * {@code /fidorial-data/tags.json}: by registry, the tag files of the tags of the {@code fidorial} namespace, such as
 * {@code fidorial:always_solid}. Data packs change them as any other tag.</p>
 */
final class RegistryDataLoader {

    private static final String DYNAMIC_RESOURCE = "/fidorial-data/registries_dynamic.json";
    private static final String OWN_TAGS_RESOURCE = "/fidorial-data/tags.json";

    private static final String GAP_NAMESPACE = "fidorial";
    private static final String GAP_PREFIX = "unused_";

    private RegistryDataLoader() {
        throw new UnsupportedOperationException("RegistryDataLoader cannot be instantiated.");
    }

    /**
     * A vanilla registry.
     *
     * @param entries indexed by network ID, {@code null} for an unused ID
     */
    record Loaded(Key name, boolean synced, List<@Nullable Vanilla> entries, Map<Key, List<Key>> tags) {

        /**
         * {@return this registry, its tags completed by tag files}
         */
        Loaded withTagFiles(final Map<Key, List<TagFile>> files) {
            final Set<Key> keys = new HashSet<>();
            for (final var entry : entries) {
                if (entry != null) {
                    keys.add(entry.key());
                }
            }
            return new Loaded(name, synced, entries, TagResolver.resolve(name, tags, keys::contains, files));
        }
    }

    /**
     * @param data the data pack JSON of a data registry entry, {@code null} for a built-in registry
     */
    record Vanilla(Key key, @Nullable JsonElement data) {
    }

    static List<Loaded> load() {
        final List<Loaded> loaded = new ArrayList<>(readDynamic());
        final Map<Key, Map<Key, List<Key>>> tags = FrozenRegistries.tags();
        FrozenRegistries.entries().forEach((name, keys) -> {
            final List<@Nullable Vanilla> entries = new ArrayList<>(keys.size());
            for (final Key key : keys) {
                entries.add(isGap(key) ? null : new Vanilla(key, null));
            }
            loaded.add(new Loaded(name, false, entries, tags.getOrDefault(name, Map.of())));
        });
        final Map<Key, Map<Key, List<TagFile>>> own = readOwnTags();
        final List<Loaded> completed = new ArrayList<>(loaded.size());
        for (final Loaded registry : loaded) {
            final Map<Key, List<TagFile>> files = own.get(registry.name());
            completed.add(files == null ? registry : registry.withTagFiles(files));
        }
        return completed;
    }

    /**
     * {@return the tag files of Fidorial, by registry, then by tag}
     */
    private static Map<Key, Map<Key, List<TagFile>>> readOwnTags() {
        try (final InputStream input = RegistryDataLoader.class.getResourceAsStream(OWN_TAGS_RESOURCE)) {
            if (input == null) {
                throw new IllegalStateException("Missing resource " + OWN_TAGS_RESOURCE);
            }
            final JsonObject root = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
            final Map<Key, Map<Key, List<TagFile>>> tags = new LinkedHashMap<>();
            for (final Map.Entry<String, JsonElement> registry : root.entrySet()) {
                final Map<Key, List<TagFile>> files = new LinkedHashMap<>();
                for (final Map.Entry<String, JsonElement> tag : registry.getValue().getAsJsonObject().entrySet()) {
                    files.put(Key.key(tag.getKey()), List.of(TagFile.parse(tag.getValue())));
                }
                tags.put(Key.key(registry.getKey()), files);
            }
            return tags;
        } catch (final IOException exception) {
            throw new UncheckedIOException("Failed to load " + OWN_TAGS_RESOURCE, exception);
        }
    }

    private static boolean isGap(final Key key) {
        return key.namespace().equals(GAP_NAMESPACE) && key.value().startsWith(GAP_PREFIX);
    }

    private static List<Loaded> readDynamic() {
        try (final InputStream input = RegistryDataLoader.class.getResourceAsStream(DYNAMIC_RESOURCE)) {
            if (input == null) {
                throw new IllegalStateException("Missing resource " + DYNAMIC_RESOURCE);
            }
            final List<Loaded> loaded = new ArrayList<>();
            try (final JsonReader reader = new JsonReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                reader.beginObject();
                while (reader.hasNext()) {
                    final Key name = Key.key(reader.nextName());
                    loaded.add(readRegistry(name, reader));
                }
                reader.endObject();
            }
            return loaded;
        } catch (final IOException exception) {
            throw new UncheckedIOException("Failed to load " + DYNAMIC_RESOURCE, exception);
        }
    }

    private static Loaded readRegistry(final Key name, final JsonReader reader) throws IOException {
        final List<@Nullable Vanilla> entries = new ArrayList<>();
        final Map<Key, List<Key>> tags = new LinkedHashMap<>();

        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "entries" -> {
                    reader.beginArray();
                    while (reader.hasNext()) {
                        if (reader.peek() == JsonToken.STRING) {
                            entries.add(new Vanilla(Key.key(reader.nextString()), null));
                        } else {
                            final JsonObject object = JsonParser.parseReader(reader).getAsJsonObject();
                            entries.add(new Vanilla(Key.key(object.get("id").getAsString()), object.get("data")));
                        }
                    }
                    reader.endArray();
                }
                case "tags" -> {
                    reader.beginObject();
                    while (reader.hasNext()) {
                        tags.put(Key.key(reader.nextName()), readKeys(reader));
                    }
                    reader.endObject();
                }
                default -> reader.skipValue();
            }
        }
        reader.endObject();
        return new Loaded(name, true, entries, tags);
    }

    private static List<Key> readKeys(final JsonReader reader) throws IOException {
        final List<Key> values = new ArrayList<>();
        reader.beginArray();
        while (reader.hasNext()) {
            values.add(Key.key(reader.nextString()));
        }
        reader.endArray();
        return values;
    }
}
