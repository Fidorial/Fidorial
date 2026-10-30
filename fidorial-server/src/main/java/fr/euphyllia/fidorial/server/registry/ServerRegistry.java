package fr.euphyllia.fidorial.server.registry;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import fr.euphyllia.fidorial.server.codecs.adventure.NbtCodecs;
import fr.fidorial.plugin.Plugin;
import fr.fidorial.registry.Registry;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TagKey;
import fr.fidorial.registry.TypedKey;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.nbt.BinaryTag;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * A registry: its entries in network order, their values, decoded on first use, and its tags.
 *
 * <p>Reads go through an immutable snapshot, replaced as a whole on every change.</p>
 *
 * @param <T> the value type
 */
public sealed class ServerRegistry<T> implements Registry<T> permits WritableServerRegistry {

    final ServerRegistries registries;
    private final RegistryKey<T> registryKey;
    private final RegistryBinding<T> binding;
    private final boolean synced;
    volatile Snapshot<T> snapshot;

    ServerRegistry(final ServerRegistries registries, final RegistryKey<T> registryKey, final RegistryBinding<T> binding,
                   final boolean synced, final List<@Nullable Entry<T>> entries, final Map<Key, List<Key>> tags) {
        this.registries = registries;
        this.registryKey = registryKey;
        this.binding = binding;
        this.synced = synced;
        this.snapshot = Snapshot.of(entries, tags);
    }

    /**
     * {@return the identifier of this registry, such as {@code minecraft:worldgen/biome}}
     */
    public final Key name() {
        return registryKey.key();
    }

    /**
     * {@return whether this registry is sent to clients during the configuration phase}
     */
    public final boolean synced() {
        return synced;
    }

    @Override
    public final RegistryKey<T> registryKey() {
        return registryKey;
    }

    @Override
    public final T get(final TypedKey<T> key) {
        final Entry<T> entry = snapshot.byKey.get(key.key());
        if (entry == null) {
            throw new NoSuchElementException("No entry " + key.key().asString() + " in registry " + name().asString());
        }
        return value(entry);
    }

    @Override
    public final Optional<T> find(final TypedKey<T> key) {
        final Entry<T> entry = snapshot.byKey.get(key.key());
        return entry == null ? Optional.empty() : Optional.of(value(entry));
    }

    @Override
    public final TypedKey<T> key(final T value) {
        Objects.requireNonNull(value, "value");
        final Snapshot<T> current = snapshot;
        if (value instanceof final Keyed keyed) {
            final Entry<T> entry = current.byKey.get(keyed.key());
            if (entry != null && matches(entry, value)) {
                return typed(entry.key);
            }
        } else {
            for (final Entry<T> entry : current.entries) {
                if (matches(entry, value)) {
                    return typed(entry.key);
                }
            }
        }
        throw new IllegalArgumentException(value + " is not registered in " + name().asString());
    }

    @Override
    public final boolean contains(final TypedKey<T> key) {
        return snapshot.byKey.containsKey(key.key());
    }

    @Override
    public final boolean isCustom(final TypedKey<T> key) {
        return isCustom(key.key());
    }

    @Override
    public final Collection<TypedKey<T>> keys() {
        final Snapshot<T> current = snapshot;
        List<TypedKey<T>> keys = current.typedKeys;
        if (keys == null) {
            final List<TypedKey<T>> built = new ArrayList<>(current.entries.size());
            for (final Entry<T> entry : current.entries) {
                built.add(typed(entry.key));
            }
            keys = List.copyOf(built);
            current.typedKeys = keys;
        }
        return keys;
    }

    @Override
    public final Collection<T> values() {
        final Snapshot<T> current = snapshot;
        List<T> values = current.values;
        if (values == null) {
            final List<T> built = new ArrayList<>(current.entries.size());
            for (final Entry<T> entry : current.entries) {
                built.add(value(entry));
            }
            values = Collections.unmodifiableList(built);
            current.values = values;
        }
        return values;
    }

    @Override
    public final Stream<T> stream() {
        return values().stream();
    }

    @Override
    public final int size() {
        return snapshot.entries.size();
    }

    @Override
    public final Collection<TagKey<T>> tags() {
        final List<TagKey<T>> tags = new ArrayList<>();
        for (final Key tag : snapshot.tags.keySet()) {
            tags.add(TagKey.create(registryKey, tag));
        }
        return Collections.unmodifiableList(tags);
    }

    @Override
    public final Collection<TypedKey<T>> tag(final TagKey<T> tag) {
        final List<Key> members = tag(tag.key());
        final List<TypedKey<T>> typed = new ArrayList<>(members.size());
        for (final Key member : members) {
            typed.add(typed(member));
        }
        return Collections.unmodifiableList(typed);
    }

    @Override
    public final boolean isTagged(final TypedKey<T> key, final TagKey<T> tag) {
        return isTagged(key.key(), tag.key());
    }

    /**
     * {@return the network identifier of {@code key}, or {@code -1} if it is not registered}
     */
    public final int networkId(final Key key) {
        return snapshot.ids.getInt(key);
    }

    /**
     * {@return the key sent under {@code networkId}, {@code null} if there is none}
     */
    public final @Nullable Key byNetworkId(final int networkId) {
        final List<@Nullable Entry<T>> byId = snapshot.byId;
        if (networkId < 0 || networkId >= byId.size()) {
            return null;
        }
        final Entry<T> entry = byId.get(networkId);
        return entry == null ? null : entry.key;
    }

    public final boolean contains(final Key key) {
        return snapshot.byKey.containsKey(key);
    }

    /**
     * {@return the value registered under {@code key}, {@code null} if there is none}
     */
    public final @Nullable T value(final Key key) {
        final Entry<T> entry = snapshot.byKey.get(key);
        return entry == null ? null : value(entry);
    }

    public final boolean isCustom(final Key key) {
        final Entry<T> entry = snapshot.byKey.get(key);
        return entry != null && entry.custom;
    }

    /**
     * {@return every registered key, in network order}
     */
    public final List<Key> keyList() {
        return snapshot.keys;
    }

    public final boolean isTagged(final Key key, final Key tag) {
        final Set<Key> members = snapshot.tagSets.get(tag);
        return members != null && members.contains(key);
    }

    /**
     * {@return the members of {@code tag}, empty if the tag does not exist}
     */
    public final List<Key> tag(final Key tag) {
        return snapshot.tags.getOrDefault(tag, List.of());
    }

    /**
     * {@return every tag and its members}
     */
    public final Map<Key, List<Key>> tagMap() {
        return snapshot.tags;
    }

    /**
     * Adds an entry defined in code, such as the entity type of a plugin mob. It is never sent to clients.
     *
     * @throws IllegalStateException if something is already registered under {@code key}
     */
    public final void registerCode(final Key key, final T value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        synchronized (this) {
            final Snapshot<T> current = snapshot;
            if (current.byKey.containsKey(key)) {
                throw new IllegalStateException("An entry is already registered under " + key.asString() + " in " + name().asString());
            }
            final List<@Nullable Entry<T>> next = new ArrayList<>(current.byId);
            next.add(Entry.custom(key, value, null));
            snapshot = Snapshot.of(next, current.tags);
        }
    }

    /**
     * Removes an entry added by {@link #registerCode(Key, Object)}.
     *
     * @return {@code true} if it was removed
     */
    public final boolean unregisterCode(final Key key) {
        synchronized (this) {
            final Snapshot<T> current = snapshot;
            final Entry<T> entry = current.byKey.get(key);
            if (entry == null || !entry.custom || entry.owner != null) {
                return false;
            }
            final List<@Nullable Entry<T>> next = new ArrayList<>(current.byId);
            next.remove(current.ids.getInt(key));
            snapshot = Snapshot.of(next, withoutMember(current.tags, key));
            return true;
        }
    }

    /**
     * {@return the entries to send in the registry data}
     *
     * @param knowsVanilla whether the client has the vanilla data pack, so vanilla entries are sent without data
     */
    public final List<RegistryEntry> networkEntries(final boolean knowsVanilla) {
        final List<Entry<T>> entries = snapshot.entries;
        final List<RegistryEntry> network = new ArrayList<>(entries.size());
        for (final Entry<T> entry : entries) {
            final @Nullable BinaryTag data;
            if (entry.custom) {
                data = encode(entry);
            } else {
                data = knowsVanilla ? null : entry.vanillaNbt();
            }
            network.add(new RegistryEntry(entry.key, data));
        }
        return network;
    }

    /**
     * Replaces every tag, without notifying anyone: only while the server loads.
     */
    final void replaceTags(final Map<Key, List<Key>> tags) {
        synchronized (this) {
            snapshot = Snapshot.of(snapshot.byId, tags);
        }
    }

    final TypedKey<T> typed(final Key key) {
        return TypedKey.create(registryKey, key);
    }

    final T value(final Entry<T> entry) {
        T value = entry.value;
        if (value != null) {
            return value;
        }
        synchronized (entry) {
            value = entry.value;
            if (value == null) {
                value = create(entry);
                entry.value = value;
            }
        }
        return value;
    }

    private T create(final Entry<T> entry) {
        final T value;
        try {
            value = switch (binding) {
                case final RegistryBinding.Data<T> data -> {
                    final JsonElement json = entry.data;
                    if (json == null) {
                        throw new IllegalStateException("it has no data");
                    }
                    yield data.codec().decode(entry.key, json);
                }
                case final RegistryBinding.Fixed<T> fixed -> fixed.factory().apply(entry.key);
            };
        } catch (final RuntimeException exception) {
            throw new IllegalStateException("Could not read " + entry.key.asString() + " in " + name().asString()
                    + ": " + exception.getMessage(), exception);
        }
        if (value == null) {
            throw new IllegalStateException("No value for " + entry.key.asString() + " in " + name().asString());
        }
        return value;
    }

    private BinaryTag encode(final Entry<T> entry) {
        if (!(binding instanceof final RegistryBinding.Data<T> data)) {
            throw new IllegalStateException(entry.key.asString() + " in " + name().asString() + " cannot be sent to clients");
        }
        return data.codec().encode(entry.key, value(entry));
    }

    private boolean matches(final Entry<T> entry, final T value) {
        final T registered = value(entry);
        return registered == value || registered.equals(value);
    }

    static Map<Key, List<Key>> withoutMember(final Map<Key, List<Key>> tags, final Key member) {
        final Map<Key, List<Key>> next = new LinkedHashMap<>();
        tags.forEach((tag, members) -> {
            if (members.contains(member)) {
                final List<Key> kept = new ArrayList<>(members);
                kept.remove(member);
                next.put(tag, kept);
            } else {
                next.put(tag, members);
            }
        });
        return next;
    }

    /**
     * An entry: its key, where it comes from, and its value once made.
     */
    static final class Entry<T> {

        final Key key;
        /**
         * Whether it was added or replaced by a plugin or by code, rather than coming from vanilla data.
         */
        final boolean custom;
        final @Nullable Plugin owner;
        private final @Nullable JsonElement data;
        private volatile @Nullable T value;
        private volatile @Nullable BinaryTag vanillaNbt;

        private Entry(final Key key, final boolean custom, final @Nullable Plugin owner,
                      final @Nullable JsonElement data, final @Nullable T value) {
            this.key = key;
            this.custom = custom;
            this.owner = owner;
            this.data = data;
            this.value = value;
        }

        /**
         * @param data the vanilla data of a data registry entry, {@code null} for a built-in registry
         */
        static <T> Entry<T> vanilla(final Key key, final @Nullable JsonElement data) {
            return new Entry<>(key, false, null, data, null);
        }

        /**
         * @param owner the plugin registering it, {@code null} for an entry defined in code
         */
        static <T> Entry<T> custom(final Key key, final T value, final @Nullable Plugin owner) {
            return new Entry<>(key, true, owner, null, value);
        }

        private @Nullable BinaryTag vanillaNbt() {
            final JsonElement json = data;
            if (json == null) {
                return null;
            }
            BinaryTag nbt = vanillaNbt;
            if (nbt == null) {
                nbt = NbtCodecs.BINARY_TAG_CODEC.parse(JsonOps.INSTANCE, json)
                        .getOrThrow(message -> new IllegalStateException(
                                "Cannot convert " + key.asString() + " to NBT: " + message));
                vanillaNbt = nbt;
            }
            return nbt;
        }
    }

    /**
     * The state of a registry at one point in time.
     */
    static final class Snapshot<T> {

        /**
         * Indexed by network ID; {@code null} for an ID the vanilla data leaves unused.
         */
        final List<@Nullable Entry<T>> byId;
        final List<Entry<T>> entries;
        final List<Key> keys;
        final Map<Key, Entry<T>> byKey;
        final Object2IntMap<Key> ids;
        final Map<Key, List<Key>> tags;
        final Map<Key, Set<Key>> tagSets;
        volatile @Nullable List<TypedKey<T>> typedKeys;
        volatile @Nullable List<T> values;

        private Snapshot(final List<@Nullable Entry<T>> byId, final Map<Key, List<Key>> tags) {
            final List<Entry<T>> entries = new ArrayList<>(byId.size());
            final List<Key> keys = new ArrayList<>(byId.size());
            final Map<Key, Entry<T>> byKey = new LinkedHashMap<>(byId.size());
            final Object2IntOpenHashMap<Key> ids = new Object2IntOpenHashMap<>(byId.size());
            ids.defaultReturnValue(-1);
            for (int id = 0; id < byId.size(); id++) {
                final Entry<T> entry = byId.get(id);
                if (entry == null) {
                    continue;
                }
                entries.add(entry);
                keys.add(entry.key);
                byKey.put(entry.key, entry);
                ids.put(entry.key, id);
            }

            final Map<Key, List<Key>> copiedTags = new LinkedHashMap<>(tags.size());
            final Map<Key, Set<Key>> tagSets = new LinkedHashMap<>(tags.size());
            tags.forEach((tag, members) -> {
                final Set<Key> kept = new LinkedHashSet<>(members.size());
                for (final Key member : members) {
                    if (byKey.containsKey(member)) {
                        kept.add(member);
                    }
                }
                copiedTags.put(tag, List.copyOf(kept));
                tagSets.put(tag, Set.copyOf(kept));
            });

            this.byId = Collections.unmodifiableList(new ArrayList<>(byId));
            this.entries = Collections.unmodifiableList(entries);
            this.keys = Collections.unmodifiableList(keys);
            this.byKey = Collections.unmodifiableMap(byKey);
            this.ids = Object2IntMaps.unmodifiable(ids);
            this.tags = Collections.unmodifiableMap(copiedTags);
            this.tagSets = Collections.unmodifiableMap(tagSets);
        }

        static <T> Snapshot<T> of(final List<@Nullable Entry<T>> byId, final Map<Key, List<Key>> tags) {
            return new Snapshot<>(byId, tags);
        }
    }
}
