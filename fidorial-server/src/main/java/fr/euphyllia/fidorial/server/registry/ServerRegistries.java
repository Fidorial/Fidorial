package fr.euphyllia.fidorial.server.registry;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.datapack.DatapackContents;
import fr.euphyllia.fidorial.server.datapack.TagFile;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.registry.ServerRegistry.Entry;
import fr.fidorial.plugin.Plugin;
import fr.fidorial.registry.RegistryKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Every registry of the server, built from the vanilla data, then completed by data packs and plugins.
 */
public final class ServerRegistries {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(ServerRegistries.class);

    private final Map<Key, ServerRegistry<?>> registries;
    private final List<ServerRegistry<?>> synced;
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final List<Consumer<ServerRegistry<?>>> listeners = new CopyOnWriteArrayList<>();

    private ServerRegistries(final List<RegistryDataLoader.Loaded> loaded) {
        final Map<Key, ServerRegistry<?>> registries = new LinkedHashMap<>();
        for (final RegistryDataLoader.Loaded registry : loaded) {
            registries.put(registry.name(), create(RegistryKey.of(registry.name()), registry));
        }
        this.registries = Collections.unmodifiableMap(registries);
        this.synced = registries.values().stream().filter(ServerRegistry::synced).toList();
    }

    /**
     * {@return the vanilla registries}
     *
     * @throws IllegalStateException if a registry has no {@linkplain RegistryBindings binding}
     */
    public static ServerRegistries load() {
        final ServerRegistries registries = new ServerRegistries(RegistryDataLoader.load());
        LOGGER.debug("Loaded {} registries, {} of them sent to clients", registries.registries.size(), registries.synced.size());
        return registries;
    }

    private <T> ServerRegistry<T> create(final RegistryKey<T> key, final RegistryDataLoader.Loaded loaded) {
        final RegistryBinding<T> binding = RegistryBindings.binding(key);
        final List<@Nullable Entry<T>> entries = new ArrayList<>(loaded.entries().size());
        for (final RegistryDataLoader.Vanilla vanilla : loaded.entries()) {
            entries.add(vanilla == null ? null : Entry.vanilla(vanilla.key(), vanilla.data()));
        }
        return switch (binding) {
            case final RegistryBinding.Data<T> data ->
                    new WritableServerRegistry<>(this, key, data, loaded.synced(), entries, loaded.tags());
            case final RegistryBinding.Fixed<T> fixed ->
                    new ServerRegistry<>(this, key, fixed, loaded.synced(), entries, loaded.tags());
        };
    }

    /**
     * @throws IllegalArgumentException if there is no such registry
     */
    @SuppressWarnings("unchecked")
    public <T> ServerRegistry<T> registry(final RegistryKey<T> key) {
        final ServerRegistry<?> registry = registries.get(key.key());
        if (registry == null) {
            throw new IllegalArgumentException("Unknown registry: " + key.key().asString());
        }
        return (ServerRegistry<T>) registry;
    }

    /**
     * @throws IllegalArgumentException if there is no such registry, or plugins cannot add to it
     */
    public <T> WritableServerRegistry<T> writable(final RegistryKey<T> key) {
        if (!(registry(key) instanceof final WritableServerRegistry<T> writable)) {
            throw new IllegalArgumentException("The registry " + key.key().asString() + " is not writable");
        }
        return writable;
    }

    /**
     * {@return the registry named {@code name}, {@code null} if there is none}
     */
    public @Nullable ServerRegistry<?> registry(final Key name) {
        return registries.get(name);
    }

    public Collection<ServerRegistry<?>> all() {
        return registries.values();
    }

    /**
     * {@return the registries sent to clients during the configuration phase, in the order they are sent}
     */
    public List<ServerRegistry<?>> synced() {
        return synced;
    }

    /**
     * Removes every entry a plugin added to a registry, and restores those it replaced.
     */
    public void unregisterAll(final Plugin owner) {
        for (final ServerRegistry<?> registry : registries.values()) {
            if (registry instanceof final WritableServerRegistry<?> writable) {
                writable.unregisterAll(owner);
            }
        }
    }

    /**
     * Applies the tags of the data packs on top of the vanilla ones. Only while the server loads.
     */
    public void applyDatapackTags(final DatapackContents contents) {
        for (final Map.Entry<String, Map<Key, List<TagFile>>> files : contents.tags().entrySet()) {
            final ServerRegistry<?> registry = registries.get(Key.key(files.getKey()));
            if (registry == null) {
                LOGGER.warn("[datapacks] Tags of the unknown registry {} are ignored", files.getKey());
                continue;
            }
            registry.replaceTags(TagResolver.resolve(registry, files.getValue()));
        }
    }

    /**
     * Once started, changing a registry sent to clients sends the players back to the configuration
     * phase to resync them.
     *
     * @param started whether players may be connected
     */
    public void setStarted(final boolean started) {
        this.started.set(started);
    }

    /**
     * @param listener called after every change of a registry plugins can add to
     */
    public void addListener(final Consumer<ServerRegistry<?>> listener) {
        listeners.add(listener);
    }

    void changed(final ServerRegistry<?> registry, final String change) {
        for (final Consumer<ServerRegistry<?>> listener : listeners) {
            listener.accept(registry);
        }
        if (started.get() && registry.synced()) {
            LOGGER.warn("{} in {} after startup: clients will be sent to the configuration phase to see the change.",
                    change, registry.name().asString());
            FidorialServer.getInstance().players().online().forEach(ServerPlayer::enterConfigurationPhase);
        } else {
            LOGGER.debug("{} in {}.", change, registry.name().asString());
        }
    }
}
