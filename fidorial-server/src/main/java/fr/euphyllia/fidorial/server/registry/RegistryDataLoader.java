package fr.euphyllia.fidorial.server.registry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import fr.euphyllia.fidorial.server.registry.data.FrozenRegistries;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads the vanilla registries: the data registries from the generated dataset, the built-in ones
 * from {@link FrozenRegistries}.
 */
final class RegistryDataLoader {

    private static final String DYNAMIC_RESOURCE = "/fidorial-data/registries_dynamic.json";

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
        return loaded;
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
