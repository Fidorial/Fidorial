package fr.euphyllia.fidorial.server.registry;

import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class RegistryHolder {

    private static final RegistryHolder EMPTY = new RegistryHolder(Map.of());

    private final Map<Key, Registry> registries;
    private final Map<Key, Map<Key, Set<Key>>> tagMembers;

    private RegistryHolder(final Map<Key, Registry> registries) {
        this.registries = registries;
        this.tagMembers = indexTags(registries);
    }

    private static Map<Key, Map<Key, Set<Key>>> indexTags(final Map<Key, Registry> registries) {
        final Map<Key, Map<Key, Set<Key>>> index = new HashMap<>();
        for (final Registry registry : registries.values()) {
            if (!registry.hasTags()) {
                continue;
            }
            final Map<Key, Set<Key>> members = new HashMap<>();
            for (final Map.Entry<Key, List<Key>> tag : registry.tags().entrySet()) {
                members.put(tag.getKey(), Set.copyOf(tag.getValue()));
            }
            index.put(registry.name(), Map.copyOf(members));
        }
        return Map.copyOf(index);
    }

    public static RegistryHolder of(final Map<Key, Registry> registries) {
        return new RegistryHolder(Collections.unmodifiableMap(new LinkedHashMap<>(registries)));
    }

    public static RegistryHolder merged(final RegistryHolder... holders) {
        final Map<Key, Registry> merged = new LinkedHashMap<>();
        for (final RegistryHolder holder : holders) {
            merged.putAll(holder.registries);
        }
        return new RegistryHolder(Collections.unmodifiableMap(merged));
    }

    public static RegistryHolder empty() {
        return EMPTY;
    }

    public boolean isEmpty() {
        return registries.isEmpty();
    }

    public int size() {
        return registries.size();
    }

    public Collection<Registry> all() {
        return registries.values();
    }

    public @Nullable Registry get(final Key name) {
        return registries.get(name);
    }

    public int networkId(final Key registry, final Key entry) {
        final Registry reg = registries.get(registry);
        return reg == null ? -1 : reg.networkId(entry);
    }

    public Set<Key> tag(final Key registry, final Key tag) {
        return tagMembers.getOrDefault(registry, Map.of()).getOrDefault(tag, Set.of());
    }

    public boolean isTagged(final Key registry, final Key tag, final Key entry) {
        return tag(registry, tag).contains(entry);
    }

    public int tagCount() {
        return registries.values().stream().mapToInt(r -> r.tags().size()).sum();
    }
}
