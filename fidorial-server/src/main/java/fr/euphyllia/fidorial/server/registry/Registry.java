package fr.euphyllia.fidorial.server.registry;

import net.kyori.adventure.key.Key;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record Registry(Key name, List<Key> entries, List<RegistryEntry> networkEntries, Map<Key, List<Key>> tags) {

    public Registry {
        entries = List.copyOf(entries);
        tags = Collections.unmodifiableMap(new LinkedHashMap<>(tags));
    }

    public static Registry of(final Key name, final List<Key> entries, final List<RegistryEntry> networkEntries) {
        return new Registry(name, entries, networkEntries, Map.of());
    }

    public int networkId(final Key entry) {
        return entries.indexOf(entry);
    }

    public boolean contains(final Key entry) {
        return entries.contains(entry);
    }

    public boolean hasTags() {
        return !tags.isEmpty();
    }
}
