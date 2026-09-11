package fr.euphyllia.fidorial.server.datapack;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import fr.euphyllia.fidorial.server.world.structure.Keys;
import fr.fidorial.world.structure.Datapack;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class DatapackLoader {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(DatapackLoader.class);
    private static final String MCMETA = "pack.mcmeta";

    private DatapackLoader() {
    }

    public static DatapackContents load(final Path folder) throws IOException {
        Files.createDirectories(folder);

        final List<Path> candidates;
        try (final Stream<Path> stream = Files.list(folder)) {
            candidates = stream
                    .filter(path -> Files.isDirectory(path) || path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".zip"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString().toLowerCase(Locale.ROOT)))
                    .toList();
        }

        final Builder builder = new Builder();
        for (final Path candidate : candidates) {
            try {
                final RawPack pack = Files.isDirectory(candidate) ? readFolder(candidate) : readZip(candidate);
                if (pack == null) {
                    LOGGER.warn("[datapacks] {} has no pack.mcmeta, it is not a datapack and is skipped", candidate.getFileName());
                    continue;
                }
                builder.add(pack);
            } catch (final IOException | RuntimeException failure) {
                LOGGER.error("[datapacks] Unable to read {}", candidate.getFileName(), failure);
            }
        }
        return builder.build();
    }

    private record RawPack(String id, Path source, String description, boolean overlays, Map<String, byte[]> files) {
    }

    private static @Nullable RawPack readFolder(final Path folder) throws IOException {
        Path root = folder;
        if (!Files.isRegularFile(root.resolve(MCMETA))) {
            root = nestedRoot(folder);
            if (root == null) {
                return null;
            }
        }
        final Map<String, byte[]> files = new LinkedHashMap<>();
        final Path data = root.resolve("data");
        if (Files.isDirectory(data)) {
            try (final Stream<Path> walk = Files.walk(data)) {
                for (final Path file : (Iterable<Path>) walk.filter(Files::isRegularFile)::iterator) {
                    final String relative = "data/" + data.relativize(file).toString().replace('\\', '/');
                    if (isRelevant(relative)) {
                        files.put(relative, Files.readAllBytes(file));
                    }
                }
            }
        }
        final String mcmeta = Files.readString(root.resolve(MCMETA), StandardCharsets.UTF_8);
        return rawPack("file/" + folder.getFileName(), folder, mcmeta, files);
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

    private static @Nullable RawPack readZip(final Path zip) throws IOException {
        try (final ZipFile file = new ZipFile(zip.toFile(), StandardCharsets.UTF_8)) {
            String prefix = "";
            ZipEntry mcmeta = file.getEntry(MCMETA);
            if (mcmeta == null) {
                final Enumeration<? extends ZipEntry> entries = file.entries();
                while (entries.hasMoreElements()) {
                    final ZipEntry entry = entries.nextElement();
                    final String name = entry.getName();
                    if (name.endsWith("/" + MCMETA) && name.indexOf('/') == name.length() - MCMETA.length() - 1) {
                        mcmeta = entry;
                        prefix = name.substring(0, name.length() - MCMETA.length());
                        break;
                    }
                }
            }
            if (mcmeta == null) {
                return null;
            }
            final Map<String, byte[]> files = new LinkedHashMap<>();
            final Enumeration<? extends ZipEntry> entries = file.entries();
            while (entries.hasMoreElements()) {
                final ZipEntry entry = entries.nextElement();
                if (entry.isDirectory() || !entry.getName().startsWith(prefix)) {
                    continue;
                }
                final String relative = entry.getName().substring(prefix.length());
                if (isRelevant(relative)) {
                    try (final InputStream in = file.getInputStream(entry)) {
                        files.put(relative, in.readAllBytes());
                    }
                }
            }
            final String meta;
            try (final InputStream in = file.getInputStream(mcmeta)) {
                meta = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
            return rawPack("file/" + zip.getFileName(), zip, meta, files);
        }
    }

    private static RawPack rawPack(final String id, final Path source, final String mcmeta, final Map<String, byte[]> files) {
        String description = "\"\"";
        boolean overlays = false;
        try {
            final JsonObject root = JsonParser.parseString(stripBom(mcmeta)).getAsJsonObject();
            final JsonObject pack = root.getAsJsonObject("pack");
            if (pack != null && pack.has("description")) {
                description = pack.get("description").toString();
            }
            overlays = root.has("overlays") && root.getAsJsonObject("overlays").has("entries")
                    && !root.getAsJsonObject("overlays").getAsJsonArray("entries").isEmpty();
        } catch (final RuntimeException invalid) {
            LOGGER.warn("[datapacks] pack.mcmeta of {} is not valid JSON", id);
        }
        return new RawPack(id, source, description, overlays, files);
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
        private final Map<Key, List<TagFile>> biomeTags = new LinkedHashMap<>();
        private final Map<Key, List<TagFile>> blockTags = new LinkedHashMap<>();

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
                switch (match.kind()) {
                    case BIOME_TAG -> addTag(biomeTags, key, element, pack.id(), path);
                    case BLOCK_TAG -> addTag(blockTags, key, element, pack.id(), path);
                    default -> json.computeIfAbsent(match.kind(), _ -> new LinkedHashMap<>()).put(key, element);
                }
            }
            packs.add(new Datapack(
                    pack.id(),
                    pack.source(),
                    pack.description(),
                    counts.getOrDefault(ResourceKind.TEMPLATE, 0),
                    counts.getOrDefault(ResourceKind.STRUCTURE, 0),
                    counts.getOrDefault(ResourceKind.STRUCTURE_SET, 0)));
            if (pack.overlays()) {
                LOGGER.info("[datapacks] {} declares overlays; overlays are not supported, the base content is used", pack.id());
            }
            LOGGER.info("[datapacks] Loaded {} ({} template(s), {} structure(s), {} structure set(s))",
                    pack.id(),
                    counts.getOrDefault(ResourceKind.TEMPLATE, 0),
                    counts.getOrDefault(ResourceKind.STRUCTURE, 0),
                    counts.getOrDefault(ResourceKind.STRUCTURE_SET, 0));
        }

        private static void addTag(final Map<Key, List<TagFile>> tags, final Key key, final JsonElement element,
                                   final String pack, final String path) {
            try {
                tags.computeIfAbsent(key, _ -> new ArrayList<>()).add(TagFile.parse(element));
            } catch (final RuntimeException invalid) {
                LOGGER.warn("[datapacks] {}: tag {} is invalid ({})", pack, path, invalid.getMessage());
            }
        }

        DatapackContents build() {
            return new DatapackContents(
                    List.copyOf(packs),
                    frozen(templates),
                    frozen(json.getOrDefault(ResourceKind.STRUCTURE, Map.of())),
                    frozen(json.getOrDefault(ResourceKind.STRUCTURE_SET, Map.of())),
                    frozen(json.getOrDefault(ResourceKind.TEMPLATE_POOL, Map.of())),
                    frozen(json.getOrDefault(ResourceKind.PROCESSOR_LIST, Map.of())),
                    frozen(biomeTags),
                    frozen(blockTags));
        }

        private static <K, V> Map<K, V> frozen(final Map<K, V> map) {
            return Collections.unmodifiableMap(new LinkedHashMap<>(map));
        }
    }
}
