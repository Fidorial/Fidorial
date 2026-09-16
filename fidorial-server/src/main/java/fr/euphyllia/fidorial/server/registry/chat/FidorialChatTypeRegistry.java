package fr.euphyllia.fidorial.server.registry.chat;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.codecs.adventure.ChatTypeCodecs;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.registry.RegistryEntry;
import fr.euphyllia.fidorial.server.registry.RegistryHolder;
import fr.fidorial.chat.ChatTypeDefinition;
import fr.fidorial.chat.ChatTypeRegistry;
import fr.fidorial.registry.Registry;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.ChatType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public final class FidorialChatTypeRegistry implements ChatTypeRegistry, Registry<ChatType> {

    public static final Key REGISTRY_NAME = Key.key("chat_type");

    private static final ComponentLogger LOGGER = ComponentLogger.logger(FidorialChatTypeRegistry.class);

    public final AtomicBoolean started = new AtomicBoolean(false);

    private volatile Snapshot snapshot;

    private FidorialChatTypeRegistry(final List<Key> vanilla) {
        final Map<Key, @Nullable ChatTypeDefinition> initial = new LinkedHashMap<>();
        for (final Key key : vanilla) {
            initial.put(key, null);
        }
        this.snapshot = Snapshot.of(initial);
    }

    public static FidorialChatTypeRegistry bootstrap(final RegistryHolder dynamic) {
        final fr.euphyllia.fidorial.server.registry.Registry source = dynamic.get(REGISTRY_NAME);
        final List<Key> entries = source == null ? List.of() : source.entries();

        if (entries.isEmpty()) {
            LOGGER.warn("No vanilla chat type found in the registry dump, starting empty.");
        }

        return new FidorialChatTypeRegistry(entries);
    }

    @Override
    public ChatTypeDefinition register(final ChatTypeDefinition definition) {
        Objects.requireNonNull(definition, "definition");
        synchronized (this) {
            if (snapshot.definitions.containsKey(definition.key()) || snapshot.ids.containsKey(definition.key())) {
                throw new IllegalStateException("A chat type is already registered under "
                        + definition.key().asString() + "; use overwrite(ChatTypeDefinition) to replace it.");
            }
            final Map<Key, @Nullable ChatTypeDefinition> next = snapshot.mutableCopy();
            next.put(definition.key(), definition);
            publish(next, "registered", definition.key());
        }
        return definition;
    }

    @Override
    public ChatTypeDefinition registerFromJson(final Key key, final String json) {
        return register(ChatTypeCodecs.fromJson(key, json));
    }

    @Override
    public Optional<ChatTypeDefinition> overwrite(final ChatTypeDefinition definition) {
        Objects.requireNonNull(definition, "definition");
        synchronized (this) {
            final ChatTypeDefinition previous = snapshot.definitions.get(definition.key());
            final Map<Key, @Nullable ChatTypeDefinition> next = snapshot.mutableCopy();
            next.put(definition.key(), definition);
            publish(next, previous == null && !snapshot.ids.containsKey(definition.key())
                    ? "registered" : "redefined", definition.key());
            return Optional.ofNullable(previous);
        }
    }

    @Override
    public boolean unregister(final Key key) {
        Objects.requireNonNull(key, "key");
        synchronized (this) {
            if (!snapshot.ids.containsKey(key)) {
                return false;
            }
            final Map<Key, @Nullable ChatTypeDefinition> next = snapshot.mutableCopy();
            next.remove(key);
            publish(next, "unregistered", key);
            return true;
        }
    }

    @Override
    public Optional<ChatTypeDefinition> definition(final Key key) {
        return Optional.ofNullable(snapshot.definitions.get(key));
    }

    @Override
    public boolean contains(final Key key) {
        return snapshot.ids.containsKey(key);
    }

    @Override
    public boolean isCustom(final Key key) {
        return snapshot.definitions.containsKey(key);
    }

    @Override
    public Collection<Key> keys() {
        return snapshot.order;
    }

    @Override
    public Collection<ChatTypeDefinition> definitions() {
        return snapshot.definitions.values();
    }

    @Override
    public int networkId(final Key key) {
        final Integer id = snapshot.ids.get(key);
        return id == null ? -1 : id;
    }

    @Override
    public int totalRegistered() {
        return snapshot.order.size();
    }

    public List<RegistryEntry> networkEntries() {
        final Snapshot current = snapshot;
        final List<RegistryEntry> entries = new ArrayList<>(current.order.size());
        for (final Key key : current.order) {
            entries.add(new RegistryEntry(key, current.payloads.get(key)));
        }
        return entries;
    }

    @Override
    public RegistryKey<ChatType> registryKey() {
        return RegistryKey.CHAT_TYPE;
    }

    @Override
    public ChatType get(final TypedKey<ChatType> key) {
        return snapshot.values.get(key.key());
    }

    @Override
    public Optional<ChatType> find(final TypedKey<ChatType> key) {
        return Optional.ofNullable(snapshot.values.get(key.key()));
    }

    @Override
    public TypedKey<ChatType> key(final ChatType value) {
        return TypedKey.create(RegistryKey.CHAT_TYPE, value.key());
    }

    @Override
    public Collection<ChatType> values() {
        return snapshot.valueList;
    }

    @Override
    public Stream<ChatType> stream() {
        return snapshot.valueList.stream();
    }

    private void publish(final Map<Key, @Nullable ChatTypeDefinition> next, final String action, final Key key) {
        this.snapshot = Snapshot.of(next);
        if (started.get()) {
            LOGGER.warn("Chat type {} {} after startup: clients will be sent to the configuration phase to see the change.",
                    key.asString(), action);
            FidorialServer.getInstance().players().forEach(ServerPlayer::enterConfigurationPhase);
        } else {
            LOGGER.debug("Chat type {} {}.", key.asString(), action);
        }
    }

    private record Snapshot(
            List<Key> order,
            Map<Key, Integer> ids,
            Map<Key, ChatTypeDefinition> definitions,
            Map<Key, CompoundBinaryTag> payloads,
            Map<Key, ChatType> values,
            List<ChatType> valueList
    ) {

        static Snapshot of(final Map<Key, @Nullable ChatTypeDefinition> source) {
            final List<Key> order = new ArrayList<>(source.size());
            final Map<Key, Integer> ids = new LinkedHashMap<>(source.size());
            final Map<Key, ChatTypeDefinition> definitions = new LinkedHashMap<>();
            final Map<Key, CompoundBinaryTag> payloads = new LinkedHashMap<>();
            final Map<Key, ChatType> values = new LinkedHashMap<>(source.size());
            final List<ChatType> valueList = new ArrayList<>(source.size());

            int index = 0;
            for (final Map.Entry<Key, @Nullable ChatTypeDefinition> entry : source.entrySet()) {
                final Key key = entry.getKey();
                final ChatTypeDefinition definition = entry.getValue();
                final ChatType value = definition != null ? definition : new VanillaChatType(key);

                order.add(key);
                ids.put(key, index++);
                if (definition != null) {
                    definitions.put(key, definition);
                    payloads.put(key, ChatTypeCodecs.encodeNbt(definition));
                }
                values.put(key, value);
                valueList.add(value);
            }

            return new Snapshot(
                    List.copyOf(order),
                    Map.copyOf(ids),
                    Collections.unmodifiableMap(definitions),
                    Map.copyOf(payloads),
                    Map.copyOf(values),
                    List.copyOf(valueList));
        }

        Map<Key, @Nullable ChatTypeDefinition> mutableCopy() {
            final Map<Key, @Nullable ChatTypeDefinition> copy = new LinkedHashMap<>();
            for (final Key key : order) {
                copy.put(key, definitions.get(key));
            }
            return copy;
        }
    }

    private record VanillaChatType(Key key) implements ChatType {
    }
}
