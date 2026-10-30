package fr.euphyllia.fidorial.server.registry;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import fr.fidorial.plugin.Plugin;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TagKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.WritableRegistry;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * A data registry plugins can add entries to. The vanilla entries are kept aside, to be restored
 * when the plugin entries replacing them are unregistered.
 *
 * @param <T> the value type
 */
public final class WritableServerRegistry<T> extends ServerRegistry<T> implements WritableRegistry<T> {

    private final RegistryBinding.Data<T> data;
    private final Map<Key, Entry<T>> vanilla;

    WritableServerRegistry(final ServerRegistries registries, final RegistryKey<T> registryKey,
                           final RegistryBinding.Data<T> data, final boolean synced,
                           final List<@Nullable Entry<T>> entries, final Map<Key, List<Key>> tags) {
        super(registries, registryKey, data, synced, entries, tags);
        this.data = data;
        final Map<Key, Entry<T>> vanilla = new HashMap<>();
        for (final Entry<T> entry : entries) {
            if (entry != null) {
                vanilla.put(entry.key, entry);
            }
        }
        this.vanilla = Map.copyOf(vanilla);
    }

    @Override
    public T register(final TypedKey<T> key, final T value, final Plugin owner) {
        final Key checked = check(key, value, owner);
        synchronized (this) {
            final Snapshot<T> current = snapshot;
            if (current.byKey.containsKey(checked)) {
                throw new IllegalStateException("An entry is already registered under " + checked.asString() + " in "
                        + name().asString() + "; use overwrite() to replace it");
            }
            final List<@Nullable Entry<T>> next = new ArrayList<>(current.byId);
            next.add(Entry.custom(checked, value, owner));
            snapshot = Snapshot.of(next, withReferencedTags(current.tags, value));
        }
        changed(checked.asString() + " registered");
        return value;
    }

    @Override
    public T registerFromJson(final TypedKey<T> key, final String json, final Plugin owner) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(json, "json");
        final JsonElement element;
        try {
            element = JsonParser.parseString(json);
        } catch (final JsonParseException exception) {
            throw new IllegalArgumentException(key.key().asString() + " is not valid JSON", exception);
        }
        return register(key, data.codec().decode(key.key(), element), owner);
    }

    @Override
    public Optional<T> overwrite(final TypedKey<T> key, final T value, final Plugin owner) {
        final Key checked = check(key, value, owner);
        final Entry<T> previous;
        synchronized (this) {
            final Snapshot<T> current = snapshot;
            previous = current.byKey.get(checked);
            final List<@Nullable Entry<T>> next = new ArrayList<>(current.byId);
            final Entry<T> entry = Entry.custom(checked, value, owner);
            if (previous == null) {
                next.add(entry);
            } else {
                next.set(current.ids.getInt(checked), entry);
            }
            snapshot = Snapshot.of(next, withReferencedTags(current.tags, value));
        }
        changed(checked.asString() + (previous == null ? " registered" : " replaced"));
        return previous == null ? Optional.empty() : previousValue(previous);
    }

    /**
     * The replaced value, unless it is a vanilla one that cannot be read: the overwrite is done anyway.
     */
    private Optional<T> previousValue(final Entry<T> previous) {
        try {
            return Optional.of(value(previous));
        } catch (final IllegalStateException unreadable) {
            return Optional.empty();
        }
    }

    @Override
    public boolean unregister(final TypedKey<T> key) {
        Objects.requireNonNull(key, "key");
        final String change;
        synchronized (this) {
            final Snapshot<T> current = snapshot;
            final Entry<T> entry = current.byKey.get(key.key());
            if (entry == null || entry.owner == null) {
                return false;
            }
            change = remove(current, List.of(entry));
        }
        changed(change);
        return true;
    }

    @Override
    public void unregisterAll(final Plugin owner) {
        Objects.requireNonNull(owner, "owner");
        final String change;
        synchronized (this) {
            final Snapshot<T> current = snapshot;
            final List<Entry<T>> owned = new ArrayList<>();
            for (final Entry<T> entry : current.entries) {
                if (entry.owner == owner) {
                    owned.add(entry);
                }
            }
            if (owned.isEmpty()) {
                return;
            }
            change = remove(current, owned);
        }
        changed(change);
    }

    @Override
    public boolean addToTag(final TagKey<T> tag, final TypedKey<T> key) {
        checkRegistry(tag.registry());
        checkRegistry(key.registry());
        synchronized (this) {
            final Snapshot<T> current = snapshot;
            if (!current.byKey.containsKey(key.key())) {
                throw new IllegalArgumentException("Nothing is registered under " + key.key().asString() + " in " + name().asString());
            }
            final List<Key> members = current.tags.getOrDefault(tag.key(), List.of());
            if (members.contains(key.key())) {
                return false;
            }
            final List<Key> grown = new ArrayList<>(members);
            grown.add(key.key());
            final Map<Key, List<Key>> next = new LinkedHashMap<>(current.tags);
            next.put(tag.key(), grown);
            snapshot = Snapshot.of(current.byId, next);
        }
        changed(key.key().asString() + " added to #" + tag.key().asString());
        return true;
    }

    @Override
    public boolean removeFromTag(final TagKey<T> tag, final TypedKey<T> key) {
        checkRegistry(tag.registry());
        checkRegistry(key.registry());
        synchronized (this) {
            final Snapshot<T> current = snapshot;
            final List<Key> members = current.tags.get(tag.key());
            if (members == null || !members.contains(key.key())) {
                return false;
            }
            final List<Key> shrunk = new ArrayList<>(members);
            shrunk.remove(key.key());
            final Map<Key, List<Key>> next = new LinkedHashMap<>(current.tags);
            next.put(tag.key(), shrunk);
            snapshot = Snapshot.of(current.byId, next);
        }
        changed(key.key().asString() + " removed from #" + tag.key().asString());
        return true;
    }

    /**
     * Restores the vanilla entries the given ones replace, and removes the others, with their tags.
     * Called while holding the lock.
     *
     * @return the change, for the log
     */
    private String remove(final Snapshot<T> current, final List<Entry<T>> removed) {
        final List<@Nullable Entry<T>> next = new ArrayList<>(current.byId);
        Map<Key, List<Key>> tags = current.tags;
        boolean restored = false;
        for (final Entry<T> entry : removed) {
            final Entry<T> stock = vanilla.get(entry.key);
            next.set(current.ids.getInt(entry.key), stock);
            if (stock != null) {
                restored = true;
            } else {
                tags = withoutMember(tags, entry.key);
            }
        }
        // A data registry has no unused ID: the removed entries are the only holes.
        next.removeIf(Objects::isNull);
        snapshot = Snapshot.of(next, tags);
        if (removed.size() > 1) {
            return removed.size() + " entries unregistered";
        }
        return removed.getFirst().key.asString() + (restored ? " restored to vanilla" : " unregistered");
    }

    private Key check(final TypedKey<T> key, final T value, final Plugin owner) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(owner, "owner");
        checkRegistry(key.registry());
        if (value instanceof final Keyed keyed && !keyed.key().equals(key.key())) {
            throw new IllegalArgumentException("The value carries the key " + keyed.key().asString()
                    + " but is registered under " + key.key().asString());
        }
        data.validator().validate(registries, key.key(), value);
        // Fails now rather than when a player joins.
        data.codec().encode(key.key(), value);
        return key.key();
    }

    private void checkRegistry(final RegistryKey<?> registry) {
        if (!registry.key().equals(name())) {
            throw new IllegalArgumentException(registry.key().asString() + " is not " + name().asString());
        }
    }

    private Map<Key, List<Key>> withReferencedTags(final Map<Key, List<Key>> tags, final T value) {
        final Set<Key> referenced = data.validator().referencedTags(value);
        if (tags.keySet().containsAll(referenced)) {
            return tags;
        }
        final Map<Key, List<Key>> next = new LinkedHashMap<>(tags);
        for (final Key tag : referenced) {
            next.putIfAbsent(tag, List.of());
        }
        return next;
    }

    private void changed(final String change) {
        registries.changed(this, change);
    }
}
