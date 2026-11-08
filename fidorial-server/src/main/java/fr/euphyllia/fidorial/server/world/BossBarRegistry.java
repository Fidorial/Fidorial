package fr.euphyllia.fidorial.server.world;

import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.world.storage.LevelData;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class BossBarRegistry {

    public record BossBarEntry(
            Key id,
            BossBar bar,
            int value,
            int max,
            boolean visible,
            Set<UUID> players
    ) {
    }

    private static final class Entry {
        final Key id;
        final BossBar bar;
        volatile int value;
        volatile int max;
        volatile boolean visible;
        volatile Set<UUID> players;
        final BossBar.Listener listener;

        Entry(final Key id, final BossBar bar, final int value, final int max,
              final boolean visible, final Set<UUID> players, final Consumer<Entry> onChange) {
            this.id = id;
            this.bar = bar;
            this.value = value;
            this.max = max;
            this.visible = visible;
            this.players = players;
            this.listener = new BossBar.Listener() {
                @Override
                public void bossBarProgressChanged(final BossBar b, final float old, final float now) {
                    onChange.accept(Entry.this);
                }

                @Override
                public void bossBarNameChanged(final BossBar b, final Component old, final Component now) {
                    onChange.accept(Entry.this);
                }

                @Override
                public void bossBarColorChanged(final BossBar b, final BossBar.Color old, final BossBar.Color now) {
                    onChange.accept(Entry.this);
                }

                @Override
                public void bossBarOverlayChanged(final BossBar b, final BossBar.Overlay old, final BossBar.Overlay now) {
                    onChange.accept(Entry.this);
                }

                @Override
                public void bossBarFlagsChanged(final BossBar b, final Set<BossBar.Flag> added, final Set<BossBar.Flag> removed) {
                    onChange.accept(Entry.this);
                }
            };
        }

        float progress() {
            return max > 0 ? Math.clamp(value / (float) max, 0f, 1f) : 0f;
        }
    }

    private final Map<Key, Entry> registered = new ConcurrentHashMap<>();

    private final LevelData levelData;
    private final Supplier<Iterable<ServerPlayer>> onlinePlayers;

    public BossBarRegistry(
            final LevelData levelData,
            final Supplier<Iterable<ServerPlayer>> onlinePlayers
    ) {
        this.levelData = levelData;
        this.onlinePlayers = onlinePlayers;
    }

    public boolean create(final Key id, final Component name) {
        if (registered.containsKey(id)) {
            return false;
        }

        final BossBar bar = BossBar.bossBar(name, 0f, BossBar.Color.WHITE, BossBar.Overlay.PROGRESS);
        register(id, bar, 0, 100, true, Set.of());
        return true;
    }

    private void register(
            final Key id,
            final BossBar bar,
            final int value,
            final int max,
            final boolean visible,
            final Set<UUID> players
    ) {
        if (registered.containsKey(id)) {
            unregister(id);
        }

        final Entry entry = new Entry(id, bar, value, max, visible, Set.copyOf(players), this::persist);

        bar.addListener(entry.listener);
        registered.put(id, entry);
        persist(entry);

        if (visible) {
            for (final ServerPlayer player : onlinePlayers.get()) {
                if (entry.players.contains(player.uuid())) {
                    player.showBossBar(bar);
                }
            }
        }
    }

    public void unregister(final Key id) {
        final Entry entry = registered.remove(id);
        if (entry == null) return;

        entry.bar.removeListener(entry.listener);
        levelData.bossBars.remove(id);

        for (final ServerPlayer player : onlinePlayers.get()) {
            player.hideBossBar(entry.bar);
        }
    }

    public void setValue(final Key id, final int value) {
        final Entry entry = registered.get(id);
        if (entry == null) return;

        synchronized (entry) {
            entry.value = Math.min(value, entry.max);
            entry.bar.progress(entry.progress());
        }
    }

    public void setMax(final Key id, final int max) {
        final Entry entry = registered.get(id);
        if (entry == null) return;

        synchronized (entry) {
            entry.max = max;
            if (entry.value > max) entry.value = max;
            entry.bar.progress(entry.progress());
        }
    }

    public void setVisible(final Key id, final boolean visible) {
        final Entry entry = registered.get(id);
        if (entry == null) return;

        synchronized (entry) {
            if (entry.visible == visible) return;
            entry.visible = visible;
            persist(entry);

            for (final ServerPlayer player : onlinePlayers.get()) {
                if (!entry.players.contains(player.uuid())) continue;
                if (visible) {
                    player.showBossBar(entry.bar);
                } else {
                    player.hideBossBar(entry.bar);
                }
            }
        }
    }

    public void setPlayers(final Key id, final Set<UUID> players) {
        final Entry entry = registered.get(id);
        if (entry == null) return;

        synchronized (entry) {
            applyPlayers(entry, players);
        }
    }

    public void addPlayer(final Key id, final UUID playerId) {
        final Entry entry = registered.get(id);
        if (entry == null) return;

        synchronized (entry) {
            if (entry.players.contains(playerId)) return;
            final Set<UUID> updated = new HashSet<>(entry.players);
            updated.add(playerId);
            applyPlayers(entry, updated);
        }
    }

    public void removePlayer(final Key id, final UUID playerId) {
        final Entry entry = registered.get(id);
        if (entry == null) return;

        synchronized (entry) {
            if (!entry.players.contains(playerId)) return;
            final Set<UUID> updated = new HashSet<>(entry.players);
            updated.remove(playerId);
            applyPlayers(entry, updated);
        }
    }

    private void applyPlayers(final Entry entry, final Set<UUID> newPlayers) {
        final Set<UUID> old = entry.players;
        final Set<UUID> updated = Set.copyOf(newPlayers);

        entry.players = updated;
        persist(entry);

        if (!entry.visible) return;

        for (final ServerPlayer player : onlinePlayers.get()) {
            final boolean was = old.contains(player.uuid());
            final boolean is = updated.contains(player.uuid());

            if (was && !is) {
                player.hideBossBar(entry.bar);
            } else if (!was && is) {
                player.showBossBar(entry.bar);
            }
        }
    }

    public void close() {
        for (final Entry entry : registered.values()) {
            persist(entry);
            entry.bar.removeListener(entry.listener);
        }
        registered.clear();
    }

    public Optional<BossBar> get(final Key id) {
        final Entry entry = registered.get(id);
        return entry == null ? Optional.empty() : Optional.of(entry.bar);
    }

    public Optional<BossBarEntry> getEntry(final Key id) {
        final Entry entry = registered.get(id);
        return entry == null ? Optional.empty() : Optional.of(toPublicEntry(entry));
    }

    public Collection<BossBarEntry> entries() {
        return registered.values().stream().map(this::toPublicEntry).toList();
    }

    private BossBarEntry toPublicEntry(final Entry entry) {
        return new BossBarEntry(entry.id, entry.bar, entry.value, entry.max, entry.visible, entry.players);
    }

    private void persist(final Entry entry) {
        levelData.bossBars.put(
                entry.id,
                new LevelData.BossBarData(
                        entry.bar.name(),
                        entry.value,
                        entry.max,
                        entry.bar.color(),
                        entry.bar.overlay(),
                        entry.bar.flags(),
                        entry.visible,
                        entry.players
                )
        );
    }

    public void syncTo(final ServerPlayer player) {
        for (final Entry entry : registered.values()) {
            if (entry.visible && entry.players.contains(player.uuid())) {
                player.showBossBar(entry.bar);
            }
        }
    }

    public void loadFromLevelData() {
        for (final Map.Entry<Key, LevelData.BossBarData> entry : levelData.bossBars.entrySet()) {
            final LevelData.BossBarData data = entry.getValue();

            final BossBar bar = BossBar.bossBar(
                    data.name(),
                    data.max() > 0 ? Math.clamp(data.value() / (float) data.max(), 0f, 1f) : 0f,
                    data.color(),
                    data.overlay(),
                    data.flags()
            );

            register(entry.getKey(), bar, data.value(), data.max(), data.visible(), data.players());
        }
    }
}
