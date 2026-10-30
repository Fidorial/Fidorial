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

/**
 * Merges the tag files of the data packs into the tags of a registry.
 *
 * <p>A tag file adds to the tag of the same name, or replaces it when it says {@code replace}.
 * Entries and nested tags that do not exist are skipped, with a warning when they are required.</p>
 */
final class TagResolver {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(TagResolver.class);

    private final ServerRegistry<?> registry;
    private final Map<Key, List<TagFile>> files;
    private final Map<Key, List<Key>> resolved = new HashMap<>();
    private final Set<Key> visiting = new HashSet<>();

    private TagResolver(final ServerRegistry<?> registry, final Map<Key, List<TagFile>> files) {
        this.registry = registry;
        this.files = files;
    }

    /**
     * {@return the tags of {@code registry} once the tag files are applied}
     *
     * @param files the tag files, by tag, in data pack order
     */
    static Map<Key, List<Key>> resolve(final ServerRegistry<?> registry, final Map<Key, List<TagFile>> files) {
        final TagResolver resolver = new TagResolver(registry, files);
        final Map<Key, List<Key>> tags = new LinkedHashMap<>(registry.tagMap());
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
            return registry.tag(tag);
        }
        if (!visiting.add(tag)) {
            LOGGER.warn("[datapacks] The tag #{} of {} references itself", tag.asString(), registry.name().asString());
            return List.of();
        }
        final Set<Key> members = new LinkedHashSet<>(registry.tag(tag));
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
                && (nested ? files.containsKey(key) || registry.tagMap().containsKey(key) : registry.contains(key));
        if (!exists) {
            if (entry.required()) {
                LOGGER.warn("[datapacks] The tag #{} of {} references the unknown {} {}, it is skipped",
                        tag.asString(), registry.name().asString(), nested ? "tag" : "entry", entry.reference());
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
