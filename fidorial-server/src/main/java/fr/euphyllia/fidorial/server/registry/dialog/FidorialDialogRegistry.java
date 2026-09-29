package fr.euphyllia.fidorial.server.registry.dialog;

import fr.euphyllia.fidorial.server.codecs.dialog.DialogCodecs;
import fr.euphyllia.fidorial.server.registry.RegistryEntry;
import fr.euphyllia.fidorial.server.registry.RegistryHolder;
import fr.fidorial.dialog.DialogDefinition;
import fr.fidorial.dialog.DialogRegistry;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class FidorialDialogRegistry implements DialogRegistry {

    public static final Key REGISTRY_NAME = Key.key("dialog");

    public static final Key PAUSE_SCREEN_ADDITIONS = Key.key("pause_screen_additions");

    public static final Key QUICK_ACTIONS = Key.key("quick_actions");

    private static final ComponentLogger LOGGER = ComponentLogger.logger(FidorialDialogRegistry.class);

    private final Map<Key, BinaryTag> vanillaData;

    private volatile Snapshot snapshot;

    private FidorialDialogRegistry(final List<Key> vanilla, final Map<Key, BinaryTag> vanillaData) {
        this.vanillaData = Map.copyOf(vanillaData);

        final Map<Key, @Nullable DialogDefinition> initial = new LinkedHashMap<>();
        for (final Key key : vanilla) {
            initial.put(key, null);
        }
        this.snapshot = Snapshot.of(initial, Set.of(), Set.of());
    }

    public static FidorialDialogRegistry bootstrap(final RegistryHolder dynamic) {
        final fr.euphyllia.fidorial.server.registry.Registry source = dynamic.get(REGISTRY_NAME);
        final List<Key> entries = source == null ? List.of() : source.entries();
        final Map<Key, BinaryTag> data = new HashMap<>();

        if (source != null) {
            for (final RegistryEntry entry : source.networkEntries()) {
                if (entry.data() != null) {
                    data.put(entry.key(), entry.data());
                }
            }
        }

        if (entries.isEmpty()) {
            LOGGER.warn("No vanilla dialog found in the registry dump, starting empty.");
        }

        return new FidorialDialogRegistry(entries, data);
    }

    @Override
    public DialogDefinition register(final Key key, final DialogDefinition dialog) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(dialog, "dialog");
        synchronized (this) {
            if (snapshot.order.contains(key)) {
                throw new IllegalStateException("A dialog is already registered under " + key.asString()
                        + "; use overwrite(Key, DialogDefinition) to replace it.");
            }
            final Map<Key, @Nullable DialogDefinition> next = snapshot.mutableCopy();
            next.put(key, dialog);
            publish(next, snapshot.pauseScreen, snapshot.quickActions, "registered", key);
        }
        return dialog;
    }

    @Override
    public Optional<DialogDefinition> overwrite(final Key key, final DialogDefinition dialog) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(dialog, "dialog");
        synchronized (this) {
            final DialogDefinition previous = snapshot.definitions.get(key);
            final Map<Key, @Nullable DialogDefinition> next = snapshot.mutableCopy();
            next.put(key, dialog);
            publish(next, snapshot.pauseScreen, snapshot.quickActions,
                    previous == null && !snapshot.order.contains(key) ? "registered" : "redefined", key);
            return Optional.ofNullable(previous);
        }
    }

    @Override
    public boolean unregister(final Key key) {
        Objects.requireNonNull(key, "key");
        synchronized (this) {
            if (!snapshot.order.contains(key)) {
                return false;
            }
            final Map<Key, @Nullable DialogDefinition> next = snapshot.mutableCopy();
            next.remove(key);

            final Set<Key> pause = new LinkedHashSet<>(snapshot.pauseScreen);
            final Set<Key> quick = new LinkedHashSet<>(snapshot.quickActions);
            pause.remove(key);
            quick.remove(key);

            publish(next, pause, quick, "unregistered", key);
            return true;
        }
    }

    @Override
    public Optional<DialogDefinition> definition(final Key key) {
        return Optional.ofNullable(snapshot.definitions.get(key));
    }

    @Override
    public boolean contains(final Key key) {
        return snapshot.order.contains(key);
    }

    public boolean isCustom(final Key key) {
        return snapshot.definitions.containsKey(key);
    }

    @Override
    public Collection<Key> keys() {
        return snapshot.order;
    }

    @Override
    public int networkId(final Key key) {
        final Integer id = snapshot.ids.get(key);
        return id == null ? -1 : id;
    }

    @Override
    public void addToPauseScreen(final Key key) {
        tag(key, PAUSE_SCREEN_ADDITIONS);
    }

    @Override
    public void addToQuickActions(final Key key) {
        tag(key, QUICK_ACTIONS);
    }

    @Override
    public boolean removeFromMenus(final Key key) {
        Objects.requireNonNull(key, "key");
        synchronized (this) {
            if (!snapshot.pauseScreen.contains(key) && !snapshot.quickActions.contains(key)) {
                return false;
            }
            final Set<Key> pause = new LinkedHashSet<>(snapshot.pauseScreen);
            final Set<Key> quick = new LinkedHashSet<>(snapshot.quickActions);
            pause.remove(key);
            quick.remove(key);
            publish(snapshot.mutableCopy(), pause, quick, "untagged", key);
            return true;
        }
    }

    @Override
    public Collection<Key> pauseScreenAdditions() {
        return snapshot.pauseScreen;
    }

    @Override
    public Collection<Key> quickActions() {
        return snapshot.quickActions;
    }

    @Override
    public DialogDefinition registerFromJson(final Key key, final String json) {
        return register(key, DialogCodecs.fromJson(key, json));
    }

    public List<RegistryEntry> networkEntries() {
        final Snapshot current = snapshot;
        final List<RegistryEntry> entries = new ArrayList<>(current.order.size());
        for (final Key key : current.order) {
            final BinaryTag data = current.payloads.containsKey(key)
                    ? current.payloads.get(key)
                    : vanillaData.get(key);
            entries.add(new RegistryEntry(key, data));
        }
        return entries;
    }

    public Map<Key, List<Key>> networkTags() {
        final Snapshot current = snapshot;
        return Map.of(
                PAUSE_SCREEN_ADDITIONS, List.copyOf(current.pauseScreen),
                QUICK_ACTIONS, List.copyOf(current.quickActions));
    }

    public int size() {
        return snapshot.order.size();
    }

    private void tag(final Key key, final Key tag) {
        Objects.requireNonNull(key, "key");
        synchronized (this) {
            if (!snapshot.order.contains(key)) {
                throw new IllegalArgumentException(
                        "Cannot add " + key.asString() + " to " + tag.asString() + ": no such dialog is registered");
            }
            final Set<Key> pause = new LinkedHashSet<>(snapshot.pauseScreen);
            final Set<Key> quick = new LinkedHashSet<>(snapshot.quickActions);
            if (!(tag.equals(PAUSE_SCREEN_ADDITIONS) ? pause : quick).add(key)) {
                return;
            }
            publish(snapshot.mutableCopy(), pause, quick, "added to " + tag.value(), key);
        }
    }

    private void publish(
            final Map<Key, @Nullable DialogDefinition> definitions,
            final Set<Key> pauseScreen,
            final Set<Key> quickActions,
            final String action,
            final Key key
    ) {
        this.snapshot = Snapshot.of(definitions, pauseScreen, quickActions);
        LOGGER.debug("Dialog {} {}, {} entries in the registry.", key.asString(), action, snapshot.order.size());
    }

    private record Snapshot(
            List<Key> order,
            Map<Key, DialogDefinition> definitions,
            Map<Key, CompoundBinaryTag> payloads,
            Map<Key, Integer> ids,
            Set<Key> pauseScreen,
            Set<Key> quickActions
    ) {

        static Snapshot of(
                final Map<Key, @Nullable DialogDefinition> source,
                final Set<Key> pauseScreen,
                final Set<Key> quickActions
        ) {
            final List<Key> order = List.copyOf(source.keySet());
            final Map<Key, DialogDefinition> definitions = new LinkedHashMap<>();
            final Map<Key, CompoundBinaryTag> payloads = new LinkedHashMap<>();
            final Map<Key, Integer> ids = new LinkedHashMap<>();

            int index = 0;
            for (final Map.Entry<Key, @Nullable DialogDefinition> entry : source.entrySet()) {
                ids.put(entry.getKey(), index++);
                final DialogDefinition dialog = entry.getValue();
                if (dialog != null) {
                    definitions.put(entry.getKey(), dialog);
                    payloads.put(entry.getKey(), DialogCodecs.toNbt(dialog));
                }
            }

            return new Snapshot(
                    order,
                    Map.copyOf(definitions),
                    Map.copyOf(payloads),
                    Map.copyOf(ids),
                    Set.copyOf(pauseScreen),
                    Set.copyOf(quickActions));
        }

        Map<Key, @Nullable DialogDefinition> mutableCopy() {
            final Map<Key, @Nullable DialogDefinition> copy = new LinkedHashMap<>();
            for (final Key key : order) {
                copy.put(key, definitions.get(key));
            }
            return copy;
        }
    }
}
