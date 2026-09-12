package fr.fidorial.registrygen.generate;

import com.google.gson.stream.JsonWriter;
import fr.fidorial.registrygen.model.RegistryDefinition;
import fr.fidorial.registrygen.model.RegistryEntryDefinition;
import fr.fidorial.registrygen.model.RegistrySync;
import fr.fidorial.registrygen.model.RegistryTagDefinition;
import fr.fidorial.registrygen.model.RegistryTypeDefinition;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Writes the runtime registry dataset consumed by the server's
 * {@code RegistryDataLoader}: for every registry sent during the configuration
 * phase, its protocol-ordered entries and its tags.
 *
 * <p>Registries declared {@link RegistrySync#NONE} are skipped: they exist only to
 * give the server typed keys and are never sent. Registries declared
 * {@link RegistrySync#FROZEN} are skipped too: they are emitted as Java source by
 * {@code FrozenRegistriesGenerator}, which is the sole source of truth for them.</p>
 *
 * @since 0.1.0
 */
public final class RegistryDatasetGenerator {

    /**
     * File holding the configuration-phase registries.
     */
    public static final String DYNAMIC_FILE_NAME = "registries_dynamic.json";

    private static final String MINECRAFT_NAMESPACE = "minecraft";

    /**
     * Writes the dataset file.
     *
     * @param registries parsed registry definitions, keyed by namespaced identifier
     * @param tags       resolved tags, keyed by namespaced registry identifier
     * @param types      the generated registry types, carrying their {@link RegistrySync}
     * @param directory  directory to write the file to, created if missing
     * @throws IOException if the file cannot be written
     */
    public void generate(final Map<String, RegistryDefinition> registries,
                         final Map<String, List<RegistryTagDefinition>> tags,
                         final List<RegistryTypeDefinition> types,
                         final Path directory) throws IOException {

        Objects.requireNonNull(registries, "registries");
        Objects.requireNonNull(tags, "tags");
        Objects.requireNonNull(types, "types");
        Objects.requireNonNull(directory, "directory");

        Files.createDirectories(directory);

        write(directory.resolve(DYNAMIC_FILE_NAME), RegistrySync.DYNAMIC, registries, tags, types);
    }

    private static void write(final Path target,
                              final RegistrySync sync,
                              final Map<String, RegistryDefinition> registries,
                              final Map<String, List<RegistryTagDefinition>> tags,
                              final List<RegistryTypeDefinition> types) throws IOException {

        try (final Writer writer = Files.newBufferedWriter(target, StandardCharsets.UTF_8);
             final JsonWriter json = new JsonWriter(writer)) {

            json.setIndent("  ");
            json.beginObject();

            for (final RegistryTypeDefinition type : types) {

                if (type.sync() != sync) {
                    continue;
                }

                final RegistryDefinition registry = registries.get(type.identifier());

                if (registry == null) {
                    /*
                     * The registry is declared as reaching the client but is absent from
                     * this version's report - most likely renamed or removed upstream.
                     * Failing here beats shipping a dataset the client will reject.
                     */
                    throw new IllegalStateException("Registry '" + type.identifier() + "' is declared "
                            + sync + " but is missing from registries.json. Update SupportedRegistries "
                            + "for this Minecraft version.");
                }

                json.name(namespaced(type.identifier())).beginObject();
                writeEntries(json, registry);
                writeTags(json, tags.getOrDefault(type.identifier(), List.of()));
                json.endObject();
            }

            json.endObject();
            writer.write('\n');
        }
    }

    /**
     * Writes entries in strict ascending {@code protocol_id} order, so that the
     * array index is the entry's network ID.
     */
    private static void writeEntries(final JsonWriter json, final RegistryDefinition registry) throws IOException {

        final List<String> ordered = registry.entries().stream()
                .sorted(Comparator.comparingInt(RegistryEntryDefinition::protocolId))
                .map(RegistryEntryDefinition::identifier)
                .toList();

        json.name("entries").beginArray();

        for (final String identifier : ordered) {
            json.value(namespaced(identifier));
        }

        json.endArray();
    }

    private static void writeTags(final JsonWriter json, final List<RegistryTagDefinition> tags) throws IOException {

        json.name("tags").beginObject();

        for (final RegistryTagDefinition tag : tags) {

            json.name(namespaced(tag.identifier())).beginArray();

            for (final String entry : tag.entries()) {
                json.value(namespaced(entry));
            }

            json.endArray();
        }

        json.endObject();
    }

    /**
     * Expands a bare identifier into its explicit {@code minecraft} form, matching
     * the identifiers the dataset has always used.
     */
    private static String namespaced(final String identifier) {
        return (identifier.indexOf(':') < 0) ? MINECRAFT_NAMESPACE + ':' + identifier : identifier;
    }
}
