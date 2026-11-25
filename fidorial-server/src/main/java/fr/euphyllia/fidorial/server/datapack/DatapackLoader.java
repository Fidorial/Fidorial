package fr.euphyllia.fidorial.server.datapack;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import fr.euphyllia.fidorial.server.world.structure.Keys;
import fr.fidorial.world.structure.Datapack;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public final class DatapackLoader {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(DatapackLoader.class);
    private static final String BUNDLED = "datapacks";
    private static final String MCMETA = "pack.mcmeta";
    private static final String DATA = "data";
    private static final String WORLDGEN = "worldgen/";
    private static final String BLOCK_TAGS = "block";
    private static final String LEGACY_BLOCK_TAGS = "blocks";

    private final List<RawPack> bundled;

    /**
     * @throws IllegalStateException if the bundled packs cannot be read
     */
    public DatapackLoader() {
        this.bundled = readBundled();
    }

    /**
     * {@return the bundled packs, then those of {@code folder}, by name}
     *
     * @throws IOException if {@code folder} cannot be listed
     */
    public DatapackContents load(final Path folder) throws IOException {
        Files.createDirectories(folder);

        final List<Path> candidates;
        try (final Stream<Path> stream = Files.list(folder)) {
            candidates = stream
                    .filter(path -> Files.isDirectory(path) || name(path).toLowerCase(Locale.ROOT).endsWith(".zip"))
                    .sorted(Comparator.comparing(path -> name(path).toLowerCase(Locale.ROOT)))
                    .toList();
        }

        final Builder builder = bundledBuilder();
        for (final Path candidate : candidates) {
            try {
                final RawPack pack = read(candidate);
                if (pack == null) {
                    LOGGER.warn("[datapacks] {} has no pack.mcmeta, it is not a datapack and is skipped", candidate.getFileName());
                    continue;
                }
                builder.add(pack);
            } catch (final IllegalArgumentException | JsonParseException invalid) {
                LOGGER.warn("[datapacks] {} is skipped: {}", candidate.getFileName(), invalid.getMessage());
            } catch (final IOException | RuntimeException failure) {
                LOGGER.error("[datapacks] Unable to read {}", candidate.getFileName(), failure);
            }
        }
        return builder.build();
    }

    /**
     * {@return the bundled packs alone}
     */
    public DatapackContents bundled() {
        return bundledBuilder().build();
    }

    private Builder bundledBuilder() {
        final Builder builder = new Builder();
        bundled.forEach(builder::add);
        return builder;
    }

    private record RawPack(String id, Path source, DatapackManifest manifest, Map<String, byte[]> files) {
    }

    private static List<RawPack> readBundled() {
        final URL root = DatapackLoader.class.getResource("/" + BUNDLED + "/");
        Preconditions.checkState(root != null, "The server has no bundled datapacks");
        try {
            if (root.openConnection() instanceof final JarURLConnection connection) {
                final Path jar = Path.of(connection.getJarFileURL().toURI());
                try (final FileSystem files = FileSystems.newFileSystem(jar)) {
                    return readBundled(files.getPath(BUNDLED), _ -> jar);
                }
            }
            return readBundled(Path.of(root.toURI()), UnaryOperator.identity());
        } catch (final IOException | URISyntaxException failure) {
            throw new IllegalStateException("Unable to read the bundled datapacks", failure);
        }
    }

    /**
     * @param source the file to report for a pack folder
     */
    private static List<RawPack> readBundled(final Path folder, final UnaryOperator<Path> source) throws IOException {
        final List<RawPack> packs = new ArrayList<>();
        try (final Stream<Path> children = Files.list(folder)) {
            for (final Path child : (Iterable<Path>) children.filter(Files::isDirectory).sorted()::iterator) {
                final String id = name(child);
                final RawPack pack = readRoot(child, id, source.apply(child));
                Preconditions.checkState(pack != null, "Bundled datapack %s has no %s", id, MCMETA);
                packs.add(pack);
            }
        }
        return List.copyOf(packs);
    }

    /**
     * {@return a pack of the world folder: a folder or a zip, {@code null} if it has no {@code pack.mcmeta}}
     */
    private static @Nullable RawPack read(final Path candidate) throws IOException {
        final String id = "file/" + name(candidate);
        if (Files.isDirectory(candidate)) {
            return readRoot(candidate, id, candidate);
        }
        try (final FileSystem zip = FileSystems.newFileSystem(candidate)) {
            return readRoot(zip.getPath("/"), id, candidate);
        }
    }

    /**
     * @param folder holds {@code pack.mcmeta}, or a single folder that does
     */
    private static @Nullable RawPack readRoot(final Path folder, final String id, final Path source) throws IOException {
        Path root = folder;
        if (!Files.isRegularFile(root.resolve(MCMETA))) {
            root = nestedRoot(folder);
            if (root == null) {
                return null;
            }
        }
        final DatapackManifest manifest = DatapackManifest.CODEC
                .parse(JsonOps.INSTANCE, JsonParser.parseString(stripBom(Files.readString(root.resolve(MCMETA), StandardCharsets.UTF_8))))
                .getOrThrow(message -> new IllegalArgumentException("invalid " + MCMETA + " of " + id + ": " + message));

        final Map<String, byte[]> files = new LinkedHashMap<>();
        final Path data = root.resolve(DATA);
        if (Files.isDirectory(data)) {
            try (final Stream<Path> walk = Files.walk(data)) {
                for (final Path file : (Iterable<Path>) walk.filter(Files::isRegularFile)::iterator) {
                    final String relative = DATA + "/" + data.relativize(file).toString().replace('\\', '/');
                    if (isRelevant(relative)) {
                        files.put(relative, Files.readAllBytes(file));
                    }
                }
            }
        }
        return new RawPack(id, source, manifest, files);
    }

    private static @Nullable Path nestedRoot(final Path folder) throws IOException {
        try (final Stream<Path> children = Files.list(folder)) {
            final List<Path> directories = children.filter(Files::isDirectory).toList();
            if (directories.size() == 1 && Files.isRegularFile(directories.getFirst().resolve(MCMETA))) {
                return directories.getFirst();
            }
        }
        return null;
    }

    private static String name(final Path path) {
        return String.valueOf(path.getFileName());
    }

    private static boolean isRelevant(final String path) {
        if (!path.startsWith("data/")) {
            return false;
        }
        final int namespaceEnd = path.indexOf('/', 5);
        return namespaceEnd > 5 && ResourceKind.classify(path.substring(namespaceEnd + 1)) != null;
    }

    private static String stripBom(final String text) {
        return !text.isEmpty() && text.charAt(0) == '\uFEFF' ? text.substring(1) : text;
    }

    private static final class Builder {

        private final List<Datapack> packs = new ArrayList<>();
        private final Map<Key, byte[]> templates = new LinkedHashMap<>();
        private final EnumMap<ResourceKind, Map<Key, JsonElement>> json = new EnumMap<>(ResourceKind.class);
        private final Map<String, Map<Key, List<TagFile>>> tags = new LinkedHashMap<>();

        void add(final RawPack pack) {
            final EnumMap<ResourceKind, Integer> counts = new EnumMap<>(ResourceKind.class);
            for (final Map.Entry<String, byte[]> file : pack.files().entrySet()) {
                final String path = file.getKey();
                final int namespaceEnd = path.indexOf('/', 5);
                final String namespace = path.substring(5, namespaceEnd);
                final ResourceKind.Match match = ResourceKind.classify(path.substring(namespaceEnd + 1));
                if (match == null) {
                    continue;
                }
                if (match.kind() == ResourceKind.TAG) {
                    addTag(namespace, match.path(), file.getValue(), pack.id(), path);
                    continue;
                }
                final Key key = Keys.parse(namespace + ":" + match.path());
                if (key == null) {
                    LOGGER.warn("[datapacks] {}: '{}' is not a valid resource location, file skipped", pack.id(), path);
                    continue;
                }
                counts.merge(match.kind(), 1, Integer::sum);
                if (match.kind() == ResourceKind.TEMPLATE) {
                    templates.put(key, file.getValue());
                    continue;
                }
                final JsonElement element;
                try {
                    element = JsonParser.parseString(stripBom(new String(file.getValue(), StandardCharsets.UTF_8)));
                } catch (final JsonParseException invalid) {
                    LOGGER.warn("[datapacks] {}: {} is not valid JSON ({})", pack.id(), path, invalid.getMessage());
                    continue;
                }
                json.computeIfAbsent(match.kind(), _ -> new LinkedHashMap<>()).put(key, element);
            }
            packs.add(new Datapack(
                    pack.id(),
                    pack.source(),
                    pack.manifest().pack().description(),
                    counts.getOrDefault(ResourceKind.TEMPLATE, 0),
                    counts.getOrDefault(ResourceKind.STRUCTURE, 0),
                    counts.getOrDefault(ResourceKind.STRUCTURE_SET, 0)));
            if (!pack.manifest().overlays().isEmpty()) {
                LOGGER.info("[datapacks] {} declares overlays; overlays are not supported, the base content is used", pack.id());
            }
            LOGGER.info("[datapacks] Loaded {} ({} template(s), {} structure(s), {} structure set(s))",
                    pack.id(),
                    counts.getOrDefault(ResourceKind.TEMPLATE, 0),
                    counts.getOrDefault(ResourceKind.STRUCTURE, 0),
                    counts.getOrDefault(ResourceKind.STRUCTURE_SET, 0));
        }

        /**
         * @param tagPath the path after {@code tags/}: the registry, then the tag
         */
        private void addTag(final String namespace, final String tagPath, final byte[] content,
                            final String pack, final String path) {
            final String registry = registryOf(tagPath);
            final Key key = registry == null ? null : Keys.parse(namespace + ":" + tagPath.substring(registry.length() + 1));
            if (registry == null || key == null) {
                LOGGER.warn("[datapacks] {}: '{}' is not a valid tag location, file skipped", pack, path);
                return;
            }
            final JsonElement element;
            try {
                element = JsonParser.parseString(stripBom(new String(content, StandardCharsets.UTF_8)));
            } catch (final JsonParseException invalid) {
                LOGGER.warn("[datapacks] {}: tag {} is not valid JSON ({})", pack, path, invalid.getMessage());
                return;
            }
            TagFile.CODEC.parse(JsonOps.INSTANCE, element)
                    .ifSuccess(file -> tags.computeIfAbsent(registry.equals(LEGACY_BLOCK_TAGS) ? BLOCK_TAGS : registry, _ -> new LinkedHashMap<>())
                            .computeIfAbsent(key, _ -> new ArrayList<>())
                            .add(file))
                    .ifError(error -> LOGGER.warn("[datapacks] {}: tag {} is invalid ({})", pack, path, error.message()));
        }

        /**
         * {@return the registry a tag path starts with: its first directory, or its first two for a
         * {@code worldgen} registry; {@code null} if the path has no tag after it}
         */
        private static @Nullable String registryOf(final String tagPath) {
            final int first = tagPath.indexOf('/');
            if (first < 0) {
                return null;
            }
            if (!tagPath.startsWith(WORLDGEN)) {
                return tagPath.substring(0, first);
            }
            final int second = tagPath.indexOf('/', first + 1);
            return second < 0 ? null : tagPath.substring(0, second);
        }

        DatapackContents build() {
            return new DatapackContents(
                    List.copyOf(packs),
                    frozen(templates),
                    frozen(json.getOrDefault(ResourceKind.STRUCTURE, Map.of())),
                    frozen(json.getOrDefault(ResourceKind.STRUCTURE_SET, Map.of())),
                    frozen(json.getOrDefault(ResourceKind.TEMPLATE_POOL, Map.of())),
                    frozen(json.getOrDefault(ResourceKind.PROCESSOR_LIST, Map.of())),
                    frozen(tags));
        }

        private static <K, V> Map<K, V> frozen(final Map<K, V> map) {
            return Collections.unmodifiableMap(new LinkedHashMap<>(map));
        }
    }
}
