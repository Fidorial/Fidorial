package fr.euphyllia.fidorial.server.map;

import com.google.common.base.Preconditions;
import fr.fidorial.entity.player.Player;
import fr.fidorial.item.ItemStack;
import fr.fidorial.item.component.MapItemDecorations;
import fr.fidorial.item.data.DataComponentTypes;
import fr.fidorial.map.MapCanvas;
import fr.fidorial.map.MapDecoration;
import fr.fidorial.map.MapLayer;
import fr.fidorial.map.MapMarkers;
import fr.fidorial.map.MapPalette;
import fr.fidorial.map.MapScale;
import fr.fidorial.map.MapView;
import fr.fidorial.registry.keys.ItemKeys;
import fr.fidorial.registry.keys.MapDecorationTypeKeys;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * A map tracked by the server: its stored pixels and icons, its layers, and what each viewer has
 * already received.
 *
 * <p>Stored state is guarded by {@link #lock}. Viewer state is only touched by the map ticker.</p>
 */
public final class ServerMapView implements MapView {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(ServerMapView.class);
    private static final int SIZE = MapCanvas.SIZE;
    private static final MapItemDecorations NO_DECORATIONS = new MapItemDecorations(Map.of());
    /**
     * A carrier stops being a viewer when they have not been seen carrying the map for that long.
     */
    private static final long CARRY_TIMEOUT_TICKS = 3;
    /**
     * Beyond this many blocks off the map, players are only shown when tracking far players.
     */
    private static final int OFF_MAP_RANGE = 320;

    private final MapHost module;
    private final int id;
    private final boolean temporary;
    final Object lock = new Object();

    private @Nullable Key world;
    private int centerX;
    private int centerZ;
    private MapScale scale = MapScale.ONE_TO_ONE;
    private boolean locked;
    private boolean tracksPlayers;
    private boolean tracksFarPlayers;

    private final byte[] pixels = new byte[SIZE * SIZE];
    private final LinkedHashMap<Key, MapDecoration> markers = new LinkedHashMap<>();
    private final Map<Key, int[]> bannerPositions = new LinkedHashMap<>();
    private final Canvas canvas = new Canvas();
    private final Markers markerView = new Markers();
    private boolean dirty;

    private final CopyOnWriteArrayList<LayerSlot> layers = new CopyOnWriteArrayList<>();
    private final Map<UUID, Boolean> shown = new ConcurrentHashMap<>();
    private final Map<UUID, Carrier> carriers = new ConcurrentHashMap<>();
    private final Map<UUID, Viewer> viewers = new ConcurrentHashMap<>();
    private volatile long lastViewedNanos = System.nanoTime();
    private volatile boolean deleted;

    ServerMapView(final MapHost module, final int id, final boolean temporary) {
        this.module = module;
        this.id = id;
        this.temporary = temporary;
    }

    @Override
    public int id() {
        return id;
    }

    @Override
    public boolean temporary() {
        return temporary;
    }

    @Override
    public Optional<Key> world() {
        synchronized (lock) {
            return Optional.ofNullable(world);
        }
    }

    @Override
    public void world(final @Nullable Key world) {
        synchronized (lock) {
            this.world = world;
            changedSettings();
        }
    }

    @Override
    public int centerX() {
        synchronized (lock) {
            return centerX;
        }
    }

    @Override
    public int centerZ() {
        synchronized (lock) {
            return centerZ;
        }
    }

    @Override
    public void center(final int x, final int z) {
        synchronized (lock) {
            this.centerX = x;
            this.centerZ = z;
            changedSettings();
        }
    }

    @Override
    public MapScale scale() {
        synchronized (lock) {
            return scale;
        }
    }

    @Override
    public void scale(final MapScale scale) {
        Preconditions.checkArgument(scale != null, "The scale of a map must not be null");
        synchronized (lock) {
            this.scale = scale;
            changedSettings();
        }
    }

    @Override
    public boolean locked() {
        synchronized (lock) {
            return locked;
        }
    }

    @Override
    public void locked(final boolean locked) {
        synchronized (lock) {
            this.locked = locked;
            changedSettings();
        }
    }

    @Override
    public boolean tracksPlayers() {
        synchronized (lock) {
            return tracksPlayers;
        }
    }

    @Override
    public void tracksPlayers(final boolean tracksPlayers) {
        synchronized (lock) {
            this.tracksPlayers = tracksPlayers;
            changedSettings();
        }
    }

    @Override
    public boolean tracksFarPlayers() {
        synchronized (lock) {
            return tracksFarPlayers;
        }
    }

    @Override
    public void tracksFarPlayers(final boolean tracksFarPlayers) {
        synchronized (lock) {
            this.tracksFarPlayers = tracksFarPlayers;
            changedSettings();
        }
    }

    private void changedSettings() {
        dirty = true;
    }

    /**
     * {@return the settings the terrain renderer needs, read at once}
     */
    MapTerrain.@Nullable Area terrainArea(final Key inWorld) {
        synchronized (lock) {
            if (locked || world == null || !world.equals(inWorld) || deleted) {
                return null;
            }
            return new MapTerrain.Area(centerX, centerZ, scale.blocksPerPixel());
        }
    }

    @Override
    public MapCanvas canvas() {
        return canvas;
    }

    @Override
    public MapMarkers markers() {
        return markerView;
    }

    /**
     * Sets a pixel of the stored canvas.
     *
     * @return whether it changed
     */
    boolean setPixel(final int x, final int y, final byte pixel) {
        if (x < 0 || y < 0 || x >= SIZE || y >= SIZE) {
            return false;
        }
        synchronized (lock) {
            final int index = y * SIZE + x;
            if (pixels[index] == pixel) {
                return false;
            }
            pixels[index] = pixel;
            dirty = true;
            return true;
        }
    }

    /**
     * Toggles the marker of a banner, as when a player uses a map on it.
     *
     * @return {@code true} if a marker was added, {@code false} if one was removed
     */
    boolean toggleBanner(final int x, final int y, final int z, final MapDecoration decoration) {
        final Key key = MapFile.bannerKey(x, y, z);
        synchronized (lock) {
            if (markers.remove(key) != null) {
                bannerPositions.remove(key);
                dirty = true;
                return false;
            }
            markers.put(key, decoration);
            bannerPositions.put(key, new int[]{x, y, z});
            dirty = true;
            return true;
        }
    }

    /**
     * {@return the banners marked on the map, by marker id, as block coordinates}
     */
    Map<Key, int[]> banners() {
        synchronized (lock) {
            final Map<Key, int[]> copy = new LinkedHashMap<>();
            bannerPositions.forEach((key, pos) -> copy.put(key, pos.clone()));
            return copy;
        }
    }

    /**
     * Reads the stored state at once, for saving.
     */
    MapFile.Data snapshot() {
        synchronized (lock) {
            final Map<Key, MapDecoration> plugin = new LinkedHashMap<>();
            final Map<Key, MapFile.Banner> banners = new LinkedHashMap<>();
            markers.forEach((key, decoration) -> {
                final int[] pos = bannerPositions.get(key);
                if (pos != null) {
                    banners.put(key, new MapFile.Banner(pos[0], pos[1], pos[2], decoration));
                } else {
                    plugin.put(key, decoration);
                }
            });
            return new MapFile.Data(world, centerX, centerZ, scale, locked, tracksPlayers, tracksFarPlayers,
                    pixels.clone(), banners, plugin);
        }
    }

    /**
     * Replaces the stored state, after loading.
     */
    void restore(final MapFile.Data data) {
        synchronized (lock) {
            world = data.world();
            centerX = data.centerX();
            centerZ = data.centerZ();
            scale = data.scale();
            locked = data.locked();
            tracksPlayers = data.tracksPlayers();
            tracksFarPlayers = data.tracksFarPlayers();
            System.arraycopy(data.pixels(), 0, pixels, 0, pixels.length);
            markers.clear();
            bannerPositions.clear();
            data.banners().forEach((key, banner) -> {
                markers.put(key, banner.decoration());
                bannerPositions.put(key, new int[]{banner.x(), banner.y(), banner.z()});
            });
            markers.putAll(data.markers());
            dirty = false;
        }
    }

    /**
     * {@return whether the map changed since it was last saved, and forgets it}
     */
    boolean takeDirty() {
        synchronized (lock) {
            final boolean was = dirty;
            dirty = false;
            return was;
        }
    }

    void markDirty() {
        synchronized (lock) {
            dirty = true;
        }
    }

    boolean isDeleted() {
        return deleted;
    }

    void markDeleted() {
        deleted = true;
    }

    @Override
    public void addLayer(final MapLayer layer) {
        Preconditions.checkArgument(layer != null, "A map layer must not be null");
        layers.add(new LayerSlot(layer));
    }

    @Override
    public boolean removeLayer(final MapLayer layer) {
        return layers.removeIf(slot -> slot.layer == layer);
    }

    @Override
    public List<MapLayer> layers() {
        final List<MapLayer> list = new ArrayList<>(layers.size());
        for (final LayerSlot slot : layers) {
            list.add(slot.layer);
        }
        return List.copyOf(list);
    }

    @Override
    public void repaint() {
        for (final LayerSlot slot : layers) {
            slot.requested = true;
        }
    }

    @Override
    public void repaint(final MapLayer layer) {
        for (final LayerSlot slot : layers) {
            if (slot.layer == layer) {
                slot.requested = true;
            }
        }
    }

    boolean hasLayers() {
        return !layers.isEmpty();
    }

    @Override
    public Collection<Player> viewers() {
        final List<Player> list = new ArrayList<>();
        for (final Viewer viewer : viewers.values()) {
            list.add(viewer.player.player());
        }
        return List.copyOf(list);
    }

    @Override
    public void show(final Player player) {
        Preconditions.checkArgument(player != null, "The player to show a map to must not be null");
        shown.put(player.uuid(), Boolean.TRUE);
        lastViewedNanos = System.nanoTime();
    }

    @Override
    public void hide(final Player player) {
        Preconditions.checkArgument(player != null, "The player to hide a map from must not be null");
        shown.remove(player.uuid());
    }

    boolean hasExplicitViewers() {
        return !shown.isEmpty();
    }

    /**
     * Records that a player carries the map, from their region thread.
     *
     * @return how many times this player updated the terrain so far, then counts one more
     */
    int carriedBy(final MapViewer player, final ItemStack stack, final long tick) {
        final Carrier carrier = carriers.compute(player.uuid(), (uuid, previous) -> {
            final Carrier next = previous == null || previous.player != player ? new Carrier(player) : previous;
            next.lastTick = tick;
            next.decorations = stack.getOrDefault(DataComponentTypes.MAP_DECORATIONS, NO_DECORATIONS);
            return next;
        });
        lastViewedNanos = System.nanoTime();
        return carrier.steps++;
    }

    long lastViewedNanos() {
        return lastViewedNanos;
    }

    @Override
    public void flush() {
        module.flush(this);
    }

    @Override
    public CompletableFuture<Void> renderTerrain() {
        return module.renderTerrain(this);
    }

    @Override
    public double pixelX(final double x) {
        synchronized (lock) {
            return (x - centerX) / scale.blocksPerPixel() + SIZE / 2.0;
        }
    }

    @Override
    public double pixelY(final double z) {
        synchronized (lock) {
            return (z - centerZ) / scale.blocksPerPixel() + SIZE / 2.0;
        }
    }

    @Override
    public ItemStack item() {
        return ItemStack.builder(ItemKeys.FILLED_MAP.key()).set(DataComponentTypes.MAP_ID, id).build();
    }

    /**
     * Paints the layers that are due and sends what changed to every viewer.
     *
     * @param tick   the ticker tick
     * @param online the online players, by id
     */
    void tick(final long tick, final Map<UUID, MapViewer> online) {
        final Map<UUID, MapViewer> current = currentViewers(tick, online);
        viewers.keySet().removeIf(uuid -> !current.containsKey(uuid));
        for (final Map.Entry<UUID, MapViewer> entry : current.entrySet()) {
            final Viewer viewer = viewers.get(entry.getKey());
            if (viewer == null || viewer.player != entry.getValue()) {
                viewers.put(entry.getKey(), new Viewer(entry.getValue()));
            }
        }
        paintLayers(tick);
        if (viewers.isEmpty()) {
            return;
        }
        lastViewedNanos = System.nanoTime();
        send(tick);
    }

    private Map<UUID, MapViewer> currentViewers(final long tick, final Map<UUID, MapViewer> online) {
        final Map<UUID, MapViewer> current = new LinkedHashMap<>();
        carriers.values().removeIf(carrier -> tick - carrier.lastTick > CARRY_TIMEOUT_TICKS
                || !online.containsKey(carrier.player.uuid()));
        for (final Carrier carrier : carriers.values()) {
            current.put(carrier.player.uuid(), carrier.player);
        }
        for (final UUID uuid : shown.keySet()) {
            final MapViewer player = online.get(uuid);
            if (player != null) {
                current.putIfAbsent(uuid, player);
            }
        }
        return current;
    }

    private void paintLayers(final long tick) {
        for (final LayerSlot slot : layers) {
            final boolean requested = slot.requested;
            slot.requested = false;
            final int interval = slot.layer.repaintInterval();
            final boolean due = requested || !slot.painted || interval > 0 && tick >= slot.nextTick;
            if (slot.layer.scope() == MapLayer.Scope.SHARED) {
                if (due) {
                    slot.shared.paint(this, slot.layer, null, tick);
                    slot.painted = true;
                    slot.nextTick = tick + Math.max(1, interval);
                }
                continue;
            }
            slot.perViewer.keySet().removeIf(uuid -> !viewers.containsKey(uuid));
            for (final Viewer viewer : viewers.values()) {
                final LayerPaint paint = slot.perViewer.get(viewer.player.uuid());
                if (paint == null || paint.player != viewer.player) {
                    final LayerPaint fresh = new LayerPaint(viewer.player);
                    slot.perViewer.put(viewer.player.uuid(), fresh);
                    fresh.paint(this, slot.layer, viewer.player, tick);
                } else if (due) {
                    paint.paint(this, slot.layer, viewer.player, tick);
                }
            }
            if (due) {
                slot.painted = true;
                slot.nextTick = tick + Math.max(1, interval);
            }
        }
    }

    private void send(final long tick) {
        final byte[] base;
        final List<MapDecoration> stored;
        final int scaleLevel;
        final boolean isLocked;
        final Key shownWorld;
        final boolean showPlayers;
        final boolean showFar;
        final int cx;
        final int cz;
        final int size;
        synchronized (lock) {
            base = pixels.clone();
            final List<MapDecoration> placed = new ArrayList<>(markers.size());
            final int blocks = scale.blocksPerPixel();
            markers.forEach((key, decoration) -> {
                final int[] banner = bannerPositions.get(key);
                if (banner == null) {
                    placed.add(decoration);
                    return;
                }
                // banners follow the map when it moves, and only show while on it
                final double x = (banner[0] + 0.5 - centerX) / blocks + SIZE / 2.0;
                final double y = (banner[2] + 0.5 - centerZ) / blocks + SIZE / 2.0;
                if (x >= 0 && y >= 0 && x <= SIZE && y <= SIZE) {
                    placed.add(new MapDecoration(decoration.type(), x, y, decoration.direction(), decoration.label()));
                }
            });
            stored = placed;
            scaleLevel = scale.level();
            isLocked = locked;
            shownWorld = world;
            showPlayers = tracksPlayers;
            showFar = tracksFarPlayers;
            cx = centerX;
            cz = centerZ;
            size = scale.blocksPerPixel();
        }
        boolean perViewer = false;
        for (final LayerSlot slot : layers) {
            if (slot.layer.scope() == MapLayer.Scope.SHARED) {
                slot.shared.overlay(base);
            } else {
                perViewer = true;
            }
        }
        final List<MapIcon> common = new ArrayList<>();
        for (final MapDecoration decoration : stored) {
            addIcon(common, decoration);
        }
        for (final LayerSlot slot : layers) {
            if (slot.layer.scope() == MapLayer.Scope.SHARED) {
                for (final MapDecoration decoration : slot.shared.decorations) {
                    addIcon(common, decoration);
                }
            }
        }
        if (shownWorld != null) {
            carrierIcons(common, shownWorld, showPlayers, showFar, cx, cz, size, tick);
        }
        for (final Viewer viewer : viewers.values()) {
            byte[] composite = base;
            List<MapIcon> icons = common;
            if (perViewer) {
                composite = base.clone();
                icons = new ArrayList<>(common);
                for (final LayerSlot slot : layers) {
                    if (slot.layer.scope() != MapLayer.Scope.PER_VIEWER) {
                        continue;
                    }
                    final LayerPaint paint = slot.perViewer.get(viewer.player.uuid());
                    if (paint != null) {
                        paint.overlay(composite);
                        for (final MapDecoration decoration : paint.decorations) {
                            addIcon(icons, decoration);
                        }
                    }
                }
            }
            sendTo(viewer, composite, icons, scaleLevel, isLocked);
        }
    }

    private void sendTo(final Viewer viewer, final byte[] composite,
                        final List<MapIcon> icons, final int scaleLevel,
                        final boolean isLocked) {
        final boolean iconsChanged = !icons.equals(viewer.icons);
        final boolean settingsChanged = viewer.scale != scaleLevel || viewer.locked != isLocked;
        // a new viewer may still hold an older picture of this map: send it whole
        final List<MapPatch> patches = viewer.fresh
                ? List.of(MapPatch.full(composite.clone()))
                : MapPatches.diff(viewer.sent, composite);
        if (patches.isEmpty() && !iconsChanged && !settingsChanged && !viewer.fresh) {
            return;
        }
        final List<MapIcon> sentIcons = iconsChanged || viewer.fresh ? List.copyOf(icons) : null;
        if (patches.isEmpty()) {
            viewer.player.send(id, scaleLevel, isLocked, sentIcons, null);
        } else {
            for (int i = 0; i < patches.size(); i++) {
                viewer.player.send(id, scaleLevel, isLocked, i == 0 ? sentIcons : null, patches.get(i));
            }
        }
        System.arraycopy(composite, 0, viewer.sent, 0, composite.length);
        viewer.icons = List.copyOf(icons);
        viewer.scale = scaleLevel;
        viewer.locked = isLocked;
        viewer.fresh = false;
    }

    /**
     * Adds the arrows of players carrying the map, and the icons their items add.
     */
    private void carrierIcons(final List<MapIcon> icons, final Key shownWorld,
                              final boolean showPlayers, final boolean showFar, final int cx, final int cz,
                              final int size, final long tick) {
        final Map<String, MapItemDecorations.Entry> fromItems = new LinkedHashMap<>();
        for (final Carrier carrier : carriers.values()) {
            final MapViewer player = carrier.player;
            carrier.decorations.entries().forEach(fromItems::putIfAbsent);
            if (!showPlayers || !player.world().equals(shownWorld)) {
                continue;
            }
            final double x = (player.x() - cx) / size;
            final double z = (player.z() - cz) / size;
            final boolean inside = x >= -63 && z >= -63 && x <= 63 && z <= 63;
            int rotation = 0;
            final Key type;
            if (inside) {
                type = MapDecorationTypeKeys.PLAYER.key();
                rotation = player.inCeilingWorld()
                        ? spin(tick)
                        : (MapMath.directionOf(player.yaw()) + 8) & 15;
            } else if (Math.abs(x) < OFF_MAP_RANGE && Math.abs(z) < OFF_MAP_RANGE) {
                type = MapDecorationTypeKeys.PLAYER_OFF_MAP.key();
            } else if (showFar) {
                type = MapDecorationTypeKeys.PLAYER_OFF_LIMITS.key();
            } else {
                continue;
            }
            addWireIcon(icons, type, halfPixels(x), halfPixels(z), rotation, null);
        }
        for (final MapItemDecorations.Entry entry : fromItems.values()) {
            final double x = (entry.x() - cx) / size;
            final double z = (entry.z() - cz) / size;
            final float degrees = entry.rotation();
            final int rotation = (int) ((degrees < 0 ? degrees - 8.0 : degrees + 8.0) * 16.0 / 360.0) & 15;
            addWireIcon(icons, entry.type().key(), halfPixels(x), halfPixels(z), rotation, null);
        }
    }

    /**
     * Where arrows point where compasses go mad.
     */
    private static int spin(final long tick) {
        final int k = (int) (tick / 10);
        return (k * k * 34187121 + k * 121) >> 15 & 15;
    }

    /**
     * Converts an offset from the center in pixels to the half pixels the client uses, clamped.
     */
    private static int halfPixels(final double pixels) {
        if (pixels <= -63) {
            return -128;
        }
        if (pixels >= 63) {
            return 127;
        }
        return (int) Math.round(pixels * 2.0);
    }

    private void addIcon(final List<MapIcon> icons, final MapDecoration decoration) {
        // the client draws icons upside down at 0: upright is 8 on the wire
        addWireIcon(icons, decoration.type().key(), halfPixels(decoration.x() - SIZE / 2.0),
                halfPixels(decoration.y() - SIZE / 2.0), (decoration.direction() + 8) & 15, decoration.label());
    }

    private void addWireIcon(final List<MapIcon> icons, final Key type, final int x,
                             final int y, final int rotation, final @Nullable Component label) {
        final int network = module.decorationNetworkId(type);
        if (network >= 0) {
            icons.add(new MapIcon(network, x, y, rotation, label));
        }
    }

    @Override
    public String toString() {
        return "MapView[id=" + id + (temporary ? ", temporary" : "") + "]";
    }

    /**
     * The stored pixels, drawn on under the lock of the map.
     */
    private final class Canvas extends AbstractMapCanvas {

        @Override
        public byte pixel(final int x, final int y) {
            if (x < 0 || y < 0 || x >= SIZE || y >= SIZE) {
                return MapPalette.TRANSPARENT;
            }
            synchronized (lock) {
                return pixels[y * SIZE + x];
            }
        }

        @Override
        public void pixel(final int x, final int y, final byte pixel) {
            setPixel(x, y, pixel);
        }

        @Override
        public void batch(final Consumer<? super MapCanvas> edits) {
            Preconditions.checkArgument(edits != null, "The edits of a map canvas batch must not be null");
            synchronized (lock) {
                edits.accept(this);
            }
        }

        @Override
        public byte[] pixels() {
            synchronized (lock) {
                return pixels.clone();
            }
        }

        @Override
        public void pixels(final byte[] replacement) {
            Preconditions.checkArgument(replacement != null && replacement.length == SIZE * SIZE,
                    "A map canvas holds exactly 128 * 128 pixels");
            synchronized (lock) {
                if (!Arrays.equals(pixels, replacement)) {
                    System.arraycopy(replacement, 0, pixels, 0, pixels.length);
                    dirty = true;
                }
            }
        }

        @Override
        public String toString() {
            return "MapCanvas[map " + id + "]";
        }
    }

    /**
     * The stored icons.
     */
    private final class Markers implements MapMarkers {

        @Override
        public void put(final Key key, final MapDecoration decoration) {
            Preconditions.checkArgument(key != null, "The key of a map marker must not be null");
            Preconditions.checkArgument(decoration != null, "A map marker must not be null");
            synchronized (lock) {
                markers.put(key, decoration);
                dirty = true;
            }
        }

        @Override
        public Optional<MapDecoration> get(final Key key) {
            synchronized (lock) {
                return Optional.ofNullable(markers.get(key));
            }
        }

        @Override
        public boolean remove(final Key key) {
            synchronized (lock) {
                final boolean removed = markers.remove(key) != null;
                if (removed) {
                    bannerPositions.remove(key);
                    dirty = true;
                }
                return removed;
            }
        }

        @Override
        public Map<Key, MapDecoration> all() {
            synchronized (lock) {
                return Map.copyOf(markers);
            }
        }

        @Override
        public void clear() {
            synchronized (lock) {
                if (!markers.isEmpty()) {
                    markers.clear();
                    bannerPositions.clear();
                    dirty = true;
                }
            }
        }
    }

    /**
     * Someone carrying the map.
     */
    private static final class Carrier {

        private final MapViewer player;
        private volatile long lastTick;
        private volatile MapItemDecorations decorations = NO_DECORATIONS;
        private int steps;

        private Carrier(final MapViewer player) {
            this.player = player;
        }
    }

    /**
     * What a viewer already received.
     */
    private static final class Viewer {

        private final MapViewer player;
        private final byte[] sent = new byte[SIZE * SIZE];
        private List<MapIcon> icons = List.of();
        private int scale = -1;
        private boolean locked;
        private boolean fresh = true;

        private Viewer(final MapViewer player) {
            this.player = player;
        }
    }

    /**
     * A layer and what it painted.
     */
    private static final class LayerSlot {

        private final MapLayer layer;
        private final LayerPaint shared = new LayerPaint(null);
        private final Map<UUID, LayerPaint> perViewer = new ConcurrentHashMap<>();
        private volatile boolean requested;
        private boolean painted;
        private long nextTick;

        private LayerSlot(final MapLayer layer) {
            this.layer = layer;
        }
    }

    /**
     * One painting of a layer, shared or for one viewer.
     */
    private static final class LayerPaint {

        private final @Nullable MapViewer player;
        private final MapCanvas canvas = new ArrayMapCanvas();
        private List<MapDecoration> decorations = List.of();
        private int version;
        private byte[] last = new byte[SIZE * SIZE];

        private LayerPaint(final @Nullable MapViewer player) {
            this.player = player;
        }

        private void paint(final ServerMapView view, final MapLayer layer, final @Nullable MapViewer viewer,
                           final long tick) {
            canvas.fill(MapPalette.TRANSPARENT);
            final List<MapDecoration> icons = new ArrayList<>();
            try {
                layer.paint(new MapLayer.Context() {
                    @Override
                    public MapView view() {
                        return view;
                    }

                    @Override
                    public Optional<Player> viewer() {
                        return viewer == null ? Optional.empty() : Optional.of(viewer.player());
                    }

                    @Override
                    public MapCanvas canvas() {
                        return canvas;
                    }

                    @Override
                    public void decorate(final MapDecoration decoration) {
                        Preconditions.checkArgument(decoration != null, "A map decoration must not be null");
                        icons.add(decoration);
                    }

                    @Override
                    public long tick() {
                        return tick;
                    }
                });
            } catch (final RuntimeException e) {
                LOGGER.error("Painting a layer of map {} failed", view.id, e);
            }
            final byte[] now = canvas.pixels();
            if (!Arrays.equals(now, last) || !icons.equals(decorations)) {
                version++;
            }
            last = now;
            decorations = List.copyOf(icons);
        }

        private void overlay(final byte[] target) {
            final byte[] layer = last;
            for (int i = 0; i < layer.length; i++) {
                if (!ServerMapPalette.transparent(layer[i])) {
                    target[i] = layer[i];
                }
            }
        }
    }
}
