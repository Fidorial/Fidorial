package fr.euphyllia.fidorial.server.plugin;

import fr.fidorial.plugin.Plugin;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A map where each value belongs to the plugin that put it there. A later
 * {@link #put} shadows the current value instead of losing it, and removing an
 * owner brings back whatever it shadowed.
 *
 * <p>Reads are lock-free; writes are rare (plugin load and unload) and serialized.</p>
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public final class PluginOwnedMap<K, V> {

    private final Map<K, Deque<Entry<V>>> stacks = new HashMap<>();
    private final Map<K, V> current = new ConcurrentHashMap<>();

    public synchronized void put(final K key, final V value, final Plugin owner) {
        stacks.computeIfAbsent(key, _ -> new ArrayDeque<>()).push(new Entry<>(value, owner));
        current.put(key, value);
    }

    public synchronized boolean remove(final K key, final Plugin owner) {
        final Deque<Entry<V>> stack = stacks.get(key);
        if (stack == null) {
            return false;
        }
        final boolean removed = stack.removeIf(entry -> entry.owner() == owner);
        final Entry<V> top = stack.peek();
        if (top == null) {
            stacks.remove(key);
            current.remove(key);
        } else {
            current.put(key, top.value());
        }
        return removed;
    }

    public synchronized void removeAll(final Plugin owner) {
        for (final K key : List.copyOf(stacks.keySet())) {
            remove(key, owner);
        }
    }

    public @Nullable V get(final K key) {
        return current.get(key);
    }

    public Map<K, V> view() {
        return Collections.unmodifiableMap(current);
    }

    private record Entry<V>(V value, Plugin owner) {
    }
}
