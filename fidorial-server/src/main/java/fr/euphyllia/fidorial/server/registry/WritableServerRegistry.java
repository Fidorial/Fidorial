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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * A data registry plugins can add entries to. The vanilla entries are kept aside, to be restored
 * when the plugin entries replacing them are unregistered.
 * <p>
 * Tag changes made by plugins are kept in a ledger and replayed over the base tags (vanilla and data packs).
 * This lets {@link #unregisterAll(Plugin)} roll back exactly what a plugin did, whatever
 * other plugins did in between, and lets data packs be reloaded without losing the changes made by plugins.
 *
 * @param <T> the value type
 */
public final class WritableServerRegistry<T> extends ServerRegistry<T> implements WritableRegistry<T> {

    /**
     * @param tag    the tag
     * @param member the entry added or removed, {@code null} to only make sure the tag exists
     * @param add    {@code true} to add the entry, {@code false} to remove it
     * @param owner  the plugin that made the change
     */
    private record TagChange(Key tag, @Nullable Key member, boolean add, Plugin owner) {
    }

    private final RegistryBinding.Data<T> data;
    private final Map<Key, Entry<T>> vanilla;
    private final List<TagChange> tagChanges = new ArrayList<>(); // guarded by this

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
            recordReferencedTags(value, owner);
            snapshot = Snapshot.of(next, effectiveTags());
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
            recordReferencedTags(value, owner);
            snapshot = Snapshot.of(next, effectiveTags());
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
            final boolean tagsTouched = tagChanges.removeIf(tagChange -> tagChange.owner() == owner);
            if (owned.isEmpty() && !tagsTouched) {
                return;
            }
            if (owned.isEmpty()) {
                snapshot = Snapshot.of(current.byId, effectiveTags());
                change = "Tag changes of " + owner.getClass().getSimpleName() + " rolled back";
            } else {
                change = remove(current, owned);
            }
        }
        changed(change);
    }

    @Override
    public boolean addToTag(final TagKey<T> tag, final TypedKey<T> key, final Plugin owner) {
        checkRegistry(tag.registry());
        checkRegistry(key.registry());
        Objects.requireNonNull(owner, "owner");
        final boolean added;
        synchronized (this) {
            final Snapshot<T> current = snapshot;
            if (!current.byKey.containsKey(key.key())) {
                throw new IllegalArgumentException("Nothing is registered under " + key.key().asString() + " in " + name().asString());
            }
            added = !isTagged(key.key(), tag.key());
            // Recorded even if the entry already belongs to the tag: another plugin's rollback must not strip it.
            record(new TagChange(tag.key(), key.key(), true, owner));
            snapshot = Snapshot.of(current.byId, effectiveTags());
        }
        if (added) {
            changed(key.key().asString() + " added to #" + tag.key().asString());
        }
        return added;
    }

    @Override
    public boolean removeFromTag(final TagKey<T> tag, final TypedKey<T> key, final Plugin owner) {
        checkRegistry(tag.registry());
        checkRegistry(key.registry());
        Objects.requireNonNull(owner, "owner");
        final boolean removed;
        synchronized (this) {
            final Snapshot<T> current = snapshot;
            if (!current.byKey.containsKey(key.key())) {
                return false;
            }
            removed = isTagged(key.key(), tag.key());
            record(new TagChange(tag.key(), key.key(), false, owner));
            snapshot = Snapshot.of(current.byId, effectiveTags());
        }
        if (removed) {
            changed(key.key().asString() + " removed from #" + tag.key().asString());
        }
        return removed;
    }

    /**
     * The base tags with every plugin tag change replayed on top, in the order they were made.
     */
    @Override
    Map<Key, List<Key>> effectiveTags() {
        if (tagChanges.isEmpty()) {
            return baseTags;
        }
        final Map<Key, Set<Key>> work = new LinkedHashMap<>();
        baseTags.forEach((tag, members) -> work.put(tag, new LinkedHashSet<>(members)));
        for (final TagChange change : tagChanges) {
            final Set<Key> members = work.computeIfAbsent(change.tag(), _ -> new LinkedHashSet<>());
            if (change.member() == null) {
                continue; // only makes sure the tag exists
            }
            if (change.add()) {
                members.add(change.member());
            } else {
                members.remove(change.member());
            }
        }
        final Map<Key, List<Key>> result = new LinkedHashMap<>(work.size());
        work.forEach((tag, members) -> result.put(tag, List.copyOf(members)));
        return result;
    }

    /**
     * Adds a change to the ledger. An equal change moves to the end, so the last word wins.
     */
    private void record(final TagChange change) {
        tagChanges.remove(change);
        tagChanges.add(change);
    }

    /**
     * Tags the value refers to are created empty if missing, so that clients find them. Rolled back with the plugin.
     */
    private void recordReferencedTags(final T value, final Plugin owner) {
        for (final Key tag : data.validator().referencedTags(value)) {
            record(new TagChange(tag, null, true, owner));
        }
    }

    /**
     * Restores the vanilla entries the given ones replace, and removes the others, with their tag changes.
     *
     * @return the change, for the log
     */
    private String remove(final Snapshot<T> current, final List<Entry<T>> removed) {
        final List<@Nullable Entry<T>> next = new ArrayList<>(current.byId);
        boolean restored = false;
        for (final Entry<T> entry : removed) {
            final Entry<T> stock = vanilla.get(entry.key);
            next.set(current.ids.getInt(entry.key), stock);
            if (stock != null) {
                restored = true;
            } else {
                // Its tag changes must not come back if the key is registered again.
                tagChanges.removeIf(tagChange -> entry.key.equals(tagChange.member()));
            }
        }
        // A data registry has no unused ID: the removed entries are the only holes.
        next.removeIf(Objects::isNull);
        snapshot = Snapshot.of(next, effectiveTags());
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

    private void changed(final String change) {
        registries.changed(this, change);
    }
}
