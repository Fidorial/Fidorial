package fr.fidorial.registrygen.generate;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * The block tags of the vanilla datapack, as Mojang's data generator writes them
 * ({@code data/minecraft/tags/block}), with the tags they include resolved.
 *
 * @since 0.1.0
 */
final class BlockTags {

    private static final String NAMESPACE = "minecraft:";

    private final Path directory;

    BlockTags(final Path directory) throws IOException {
        if (!Files.isDirectory(directory)) {
            throw new IOException("No block tags in " + directory + ": run the data generator with \"--server\".");
        }
        this.directory = directory;
    }

    /**
     * {@return the blocks of a tag, without namespace}
     *
     * @param tag the tag, without namespace nor {@code #}
     */
    Set<String> blocks(final String tag) {
        final Set<String> blocks = new HashSet<>();
        collect(tag, blocks, new HashSet<>());
        return blocks;
    }

    /**
     * {@return the blocks named in {@code entries}, a {@code #tag} standing for all its blocks}
     */
    Set<String> expand(final Collection<String> entries) {
        final Set<String> blocks = new HashSet<>();
        for (final String entry : entries) {
            if (entry.startsWith("#")) {
                blocks.addAll(blocks(entry.substring(1)));
            } else {
                blocks.add(entry);
            }
        }
        return blocks;
    }

    private void collect(final String tag, final Set<String> blocks, final Set<String> visited) {
        if (!visited.add(tag)) {
            return;
        }
        final Path file = directory.resolve(tag + ".json");
        if (!Files.isRegularFile(file)) {
            throw new IllegalStateException("Unknown block tag #" + tag + " in " + directory);
        }
        final JsonObject json;
        try (final Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            json = JsonParser.parseReader(reader).getAsJsonObject();
        } catch (final IOException exception) {
            throw new UncheckedIOException(exception);
        }
        for (final JsonElement value : json.getAsJsonArray("values")) {
            final String id = value.isJsonObject() ? value.getAsJsonObject().get("id").getAsString() : value.getAsString();
            if (id.startsWith("#")) {
                collect(strip(id.substring(1)), blocks, visited);
            } else {
                blocks.add(strip(id));
            }
        }
    }

    private static String strip(final String id) {
        return id.startsWith(NAMESPACE) ? id.substring(NAMESPACE.length()) : id;
    }
}