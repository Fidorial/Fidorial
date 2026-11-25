package fr.euphyllia.fidorial.server.registry;

import fr.euphyllia.fidorial.server.datapack.TagFile;
import fr.euphyllia.fidorial.server.world.structure.Keys;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Merges the tag files of the data packs, the bundled ones first, into the vanilla tags of a registry.
 * <p>
 * A tag file adds to the tag of the same name, or replaces it when it says {@code replace}.
 * Entries and nested tags that do not exist are skipped, with a warning when they are required.
 */
final class TagResolver {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(TagResolver.class);

    private final Key registry;
    private final Map<Key, List<Key>> base;
    private final Predicate<Key> entries;
    private final Map<Key, List<TagFile>> files;
    private final Map<Key, List<Key>> resolved = new HashMap<>();
    private final Set<Key> visiting = new HashSet<>();

    private TagResolver(final Key registry, final Map<Key, List<Key>> base, final Predicate<Key> entries,
                        final Map<Key, List<TagFile>> files) {
        this.registry = registry;
        this.base = base;
        this.entries = entries;
        this.files = files;
    }

    /**
     * {@return the tags of {@code registry} once the tag files of the data packs are applied to its
     * built-in tags}
     *
     * @param files the tag files, by tag, in data pack order
     */
    static Map<Key, List<Key>> resolve(final ServerRegistry<?> registry, final Map<Key, List<TagFile>> files) {
        return resolve(registry.name(), registry.vanillaTags(), registry::contains, files);
    }

    /**
     * {@return the tags of a registry once tag files are applied to some tags}
     *
     * @param registry the name of the registry
     * @param base     the tags the files apply to
     * @param entries  whether the registry holds an entry
     * @param files    the tag files, by tag, in order
     */
    private static Map<Key, List<Key>> resolve(final Key registry, final Map<Key, List<Key>> base, final Predicate<Key> entries,
                                               final Map<Key, List<TagFile>> files) {
        final TagResolver resolver = new TagResolver(registry, base, entries, files);
        final Map<Key, List<Key>> tags = new LinkedHashMap<>(base);
        for (final Key tag : files.keySet()) {
            tags.put(tag, resolver.tag(tag));
        }
        return tags;
    }

    private List<Key> tag(final Key tag) {
        final List<Key> done = resolved.get(tag);
        if (done != null) {
            return done;
        }
        final List<TagFile> definitions = files.get(tag);
        if (definitions == null) {
            return base.getOrDefault(tag, List.of());
        }
        if (!visiting.add(tag)) {
            LOGGER.warn("[datapacks] The tag #{} of {} references itself", tag.asString(), registry.asString());
            return List.of();
        }
        final Set<Key> members = new LinkedHashSet<>(base.getOrDefault(tag, List.of()));
        for (final TagFile file : definitions) {
            if (file.replace()) {
                members.clear();
            }
            for (final TagFile.Entry entry : file.values()) {
                add(tag, entry, members);
            }
        }
        visiting.remove(tag);
        final List<Key> result = List.copyOf(members);
        resolved.put(tag, result);
        return result;
    }

    private void add(final Key tag, final TagFile.Entry entry, final Set<Key> members) {
        final boolean nested = entry.isTag();
        final Key key = Keys.parse(nested ? entry.reference().substring(1) : entry.reference());
        final boolean exists = key != null
                && (nested ? files.containsKey(key) || base.containsKey(key) : entries.test(key));
        if (key == null || !exists) {
            if (entry.required()) {
                LOGGER.warn("[datapacks] The tag #{} of {} references the unknown {} {}, it is skipped",
                        tag.asString(), registry.asString(), nested ? "tag" : "entry", entry.reference());
            }
            return;
        }
        if (nested) {
            members.addAll(tag(key));
        } else {
            members.add(key);
        }
    }
}
