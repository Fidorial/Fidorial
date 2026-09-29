package fr.euphyllia.fidorial.server.registry;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.mojang.serialization.JsonOps;
import fr.euphyllia.fidorial.server.codecs.adventure.NbtCodecs;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RegistryDataLoader {

    private static final String DYNAMIC_RESOURCE = "/fidorial-data/registries_dynamic.json";

    private final Map<Key, Registry> dynamic = new LinkedHashMap<>();

    private RegistryDataLoader() {
    }

    static RegistryDataLoader load() {
        final RegistryDataLoader loader = new RegistryDataLoader();
        loader.read(DYNAMIC_RESOURCE, loader.dynamic);
        return loader;
    }

    Map<Key, Registry> dynamic() {
        return dynamic;
    }

    private void read(final String resource, final Map<Key, Registry> target) {
        try (final InputStream input = RegistryDataLoader.class.getResourceAsStream(resource)) {
            if (input == null) {
                throw new IllegalStateException("Missing resource " + resource);
            }
            readGroup(new InputStreamReader(input, StandardCharsets.UTF_8), target);
        } catch (final IOException exception) {
            throw new UncheckedIOException("Failed to load " + resource, exception);
        }
    }

    private void readGroup(final Reader input, final Map<Key, Registry> target) throws IOException {
        try (final JsonReader reader = new JsonReader(input)) {
            reader.beginObject();
            while (reader.hasNext()) {
                final Key name = Key.key(reader.nextName());
                target.put(name, readRegistry(name, reader));
            }
            reader.endObject();
        }
    }

    private Registry readRegistry(final Key name, final JsonReader reader) throws IOException {
        final List<Key> entries = new ArrayList<>();
        final List<RegistryEntry> network = new ArrayList<>();
        Map<Key, List<Key>> tags = Map.of();

        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "entries" -> {
                    reader.beginArray();
                    while (reader.hasNext()) {
                        if (reader.peek() == JsonToken.STRING) {
                            final Key key = Key.key(reader.nextString());
                            entries.add(key);
                            network.add(RegistryEntry.known(key));
                        } else {
                            final JsonObject object = JsonParser.parseReader(reader).getAsJsonObject();
                            final Key key = Key.key(object.get("id").getAsString());
                            entries.add(key);
                            network.add(new RegistryEntry(key, decodeData(name, key, object)));
                        }
                    }
                    reader.endArray();
                }
                case "tags" -> {
                    tags = new LinkedHashMap<>();
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
        return new Registry(name, entries, network, tags);
    }

    private static @Nullable BinaryTag decodeData(final Key registry, final Key key, final JsonObject object) {
        if (!object.has("data")) {
            return null;
        }
        return NbtCodecs.BINARY_TAG_CODEC
                .parse(JsonOps.INSTANCE, object.get("data"))
                .getOrThrow(message -> new IllegalStateException(
                        "Invalid data for " + key.asString() + " in " + registry.asString() + ": " + message));
    }

    private List<Key> readKeys(final JsonReader reader) throws IOException {
        final List<Key> values = new ArrayList<>();
        reader.beginArray();
        while (reader.hasNext()) {
            values.add(Key.key(reader.nextString()));
        }
        reader.endArray();
        return values;
    }
}
