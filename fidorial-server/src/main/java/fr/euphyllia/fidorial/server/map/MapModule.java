package fr.euphyllia.fidorial.server.map;

import com.google.common.base.Preconditions;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.codecs.adventure.ComponentCodecs;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.lifecycle.Quietly;
import fr.euphyllia.fidorial.server.lifecycle.ServerModule;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundMapItemDataPacket;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.block.blockentity.BlockEntity;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.chunk.ChunkColumn;
import fr.euphyllia.fidorial.server.world.chunk.ChunkSection;
import fr.euphyllia.fidorial.server.world.storage.WorldDataFiles;
import fr.fidorial.entity.GameMode;
import fr.fidorial.event.player.PlayerCreateMapEvent;
import fr.fidorial.inventory.PlayerInventory;
import fr.fidorial.item.ItemStack;
import fr.fidorial.item.data.DataComponentTypes;
import fr.fidorial.map.MapCanvas;
import fr.fidorial.map.MapColor;
import fr.fidorial.map.MapDecoration;
import fr.fidorial.map.MapFont;
import fr.fidorial.map.MapImage;
import fr.fidorial.map.MapLayer;
import fr.fidorial.map.MapManager;
import fr.fidorial.map.MapPalette;
import fr.fidorial.map.MapScale;
import fr.fidorial.map.MapView;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.math.Location;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.ItemKeys;
import fr.fidorial.world.World;
import fr.fidorial.world.block.BlockData;
import io.papermc.adventurex.nbt.dfu.BinaryTagOps;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * The maps of the server: ids, files, carriers, terrain and what is sent to clients.
 *
 * <p>Players report the maps they carry from their region threads ({@link #tickPlayer}), which
 * also draws the terrain around them. A ticker thread paints layers and sends changes to viewers
 * twenty times per second.</p>
 */
public final class MapModule implements ServerModule, MapManager, MapHost {

    static final ComponentLogger LOGGER = ComponentLogger.logger(MapModule.class);

    private static final Key DECORATION_REGISTRY = Key.key("map_decoration_type");
    private static final Path MAPS = Path.of("minecraft", "maps");
    private static final String LAST_ID = "last_id.dat";
    /**
     * Saved maps nobody looked at for this long are unloaded.
     */
    private static final long IDLE_NANOS = TimeUnit.MINUTES.toNanos(5);
    /**
     * Missing ids are not looked for again before this long.
     */
    private static final long MISSING_RETRY_NANOS = TimeUnit.SECONDS.toNanos(30);
    private static final int BANNER_CHECK_INTERVAL = 20;
    private static final Sound CREATE_SOUND = Sound.sound(Key.key("ui.cartography_table.take_result"),
            Sound.Source.PLAYER, 1.0f, 1.0f);
    private static final MapFile.Labels LABELS = new MapFile.Labels() {
        @Override
        public @Nullable BinaryTag write(final Component label) {
            return ComponentCodecs.COMPONENT_CODEC.encodeStart(BinaryTagOps.binaryTagOps(), label).result().orElse(null);
        }

        @Override
        public @Nullable Component read(final BinaryTag tag) {
            return ComponentCodecs.COMPONENT_CODEC.parse(BinaryTagOps.binaryTagOps(), tag).result().orElse(null);
        }
    };

    private final FidorialServer server;
    private final BlockMapColors colors = BlockMapColors.get();
    private final MapTerrain terrain = new MapTerrain(colors);
    private final Map<Integer, ServerMapView> views = new ConcurrentHashMap<>();
    private final Map<Integer, Long> missing = new ConcurrentHashMap<>();
    private final Map<ServerMapView, Object> tickLocks = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerViewer> playerViewers = new ConcurrentHashMap<>();
    private final Deque<Integer> freeTemporaryIds = new ArrayDeque<>();
    private final AtomicLong tick = new AtomicLong();
    private final Map<Key, Integer> decorationIds = new ConcurrentHashMap<>();
    private final Set<Key> unknownDecorations = ConcurrentHashMap.newKeySet();
    private final ScheduledExecutorService ticker;
    private final ExecutorService renderers;
    private int lastId = -1;
    private int nextTemporaryId = Integer.MAX_VALUE;
    private boolean lastIdDirty;

    public MapModule(final FidorialServer server) {
        this.server = server;
        this.ticker = Executors.newSingleThreadScheduledExecutor(
                r -> Thread.ofPlatform().name("fidorial-map-ticker").daemon(true).unstarted(r));
        this.renderers = Executors.newFixedThreadPool(2,
                r -> Thread.ofPlatform().name("fidorial-map-renderer").daemon(true).unstarted(r));
    }

    private Path directory() {
        return server.worlds().manager().paths().dataDir().resolve(MAPS);
    }

    private Path legacyDirectory() {
        return server.worlds().manager().paths().dataDir();
    }

    @Override
    public void load() throws IOException {
        final Optional<CompoundBinaryTag> stored = WorldDataFiles.read(directory().resolve(LAST_ID))
                .or(() -> readQuietly(legacyDirectory().resolve("idcounts.dat")));
        synchronized (this) {
            lastId = stored.map(tag -> tag.getInt("map", -1)).orElse(-1);
        }
        LOGGER.debug("Last map id: {}", lastId);
    }

    @Override
    public void start() {
        ticker.scheduleAtFixedRate(() -> {
            try {
                tickAll();
            } catch (final Throwable t) {
                LOGGER.error("Map tick failed", t);
            }
        }, 50, 50, TimeUnit.MILLISECONDS);
    }

    @Override
    public void autoSave() {
        saveDirty();
        unloadIdle();
    }

    @Override
    public void stop() {
        Quietly.run("map ticker", ticker::shutdownNow);
        Quietly.run("map renderers", renderers::shutdownNow);
    }

    @Override
    public void close() {
        saveDirty();
    }

    @Override
    public MapView create(final World world, final int centerX, final int centerZ, final MapScale scale) {
        Preconditions.checkArgument(world != null, "The world of a map must not be null");
        Preconditions.checkArgument(scale != null, "The scale of a map must not be null");
        final ServerMapView view = register(new ServerMapView(this, allocateId(), false));
        view.restore(new MapFile.Data(world.key(), centerX, centerZ, scale, false, true, false,
                new byte[128 * 128], Map.of(), Map.of()));
        view.markDirty();
        return view;
    }

    @Override
    public MapView createBlank() {
        final ServerMapView view = register(new ServerMapView(this, allocateId(), false));
        view.restore(new MapFile.Data(null, 0, 0, MapScale.ONE_TO_ONE, false, false, false, new byte[128 * 128],
                Map.of(), Map.of()));
        view.markDirty();
        return view;
    }

    @Override
    public MapView createTemporary() {
        final int id;
        synchronized (this) {
            final Integer free = freeTemporaryIds.poll();
            id = free != null ? free : nextTemporaryId--;
        }
        final ServerMapView view = register(new ServerMapView(this, id, true));
        view.restore(new MapFile.Data(null, 0, 0, MapScale.ONE_TO_ONE, false, false, false, new byte[128 * 128],
                Map.of(), Map.of()));
        return view;
    }

    @Override
    public Optional<MapView> get(final int id) {
        return Optional.ofNullable(load(id));
    }

    @Override
    public Optional<MapView> of(final ItemStack stack) {
        Preconditions.checkArgument(stack != null, "The item must not be null");
        if (!stack.id().equals(ItemKeys.FILLED_MAP.key())) {
            return Optional.empty();
        }
        final Integer id = stack.get(DataComponentTypes.MAP_ID);
        return id == null ? Optional.empty() : get(id);
    }

    @Override
    public Collection<MapView> loaded() {
        return List.copyOf(views.values());
    }

    @Override
    public void delete(final MapView view) {
        Preconditions.checkArgument(view instanceof ServerMapView, "Not a map of this server: %s", view);
        final ServerMapView map = (ServerMapView) view;
        map.markDeleted();
        views.remove(map.id(), map);
        tickLocks.remove(map);
        if (map.temporary()) {
            synchronized (this) {
                freeTemporaryIds.push(map.id());
            }
            return;
        }
        try {
            Files.deleteIfExists(file(map.id()));
        } catch (final IOException e) {
            LOGGER.error("Deleting map {} failed", map.id(), e);
        }
    }

    @Override
    public MapView lockedCopy(final MapView source) {
        final ServerMapView from = cast(source);
        final MapFile.Data data = from.snapshot();
        final ServerMapView view = register(new ServerMapView(this, allocateId(), false));
        view.restore(new MapFile.Data(data.world(), data.centerX(), data.centerZ(), data.scale(), true,
                data.tracksPlayers(), data.tracksFarPlayers(), data.pixels(), data.banners(), data.markers()));
        view.markDirty();
        return view;
    }

    @Override
    public MapView zoomedOut(final MapView source) {
        final ServerMapView from = cast(source);
        final MapFile.Data data = from.snapshot();
        final MapScale scale = MapMath.zoomedOut(data.scale());
        final ServerMapView view = register(new ServerMapView(this, allocateId(), false));
        view.restore(new MapFile.Data(data.world(), MapMath.alignedCenter(data.centerX(), scale), MapMath.alignedCenter(data.centerZ(), scale),
                scale, false, data.tracksPlayers(), data.tracksFarPlayers(), new byte[128 * 128], Map.of(), Map.of()));
        view.markDirty();
        return view;
    }

    @Override
    public MapColor colorOf(final BlockData block) {
        Preconditions.checkArgument(block != null, "The block must not be null");
        return colors.of(block);
    }

    @Override
    public MapPalette palette() {
        return ServerMapPalette.INSTANCE;
    }

    @Override
    public MapCanvas createCanvas() {
        return new ArrayMapCanvas();
    }

    @Override
    public MapImage image(final int width, final int height, final byte[] pixels) {
        return ServerMapImage.of(width, height, pixels);
    }

    @Override
    public MapImage image(final int width, final int height, final int[] argb, final MapImage.Dithering dithering) {
        return ServerMapImage.fromArgb(width, height, argb, dithering);
    }

    @Override
    public MapImage image(final BufferedImage image, final MapImage.Dithering dithering) {
        return ServerMapImage.from(image, dithering);
    }

    @Override
    public MapImage image(final BufferedImage image, final int width, final int height,
                          final MapImage.Dithering dithering) {
        return ServerMapImage.from(image, width, height, dithering);
    }

    @Override
    public MapFont standardFont() {
        return StandardMapFont.INSTANCE;
    }

    @Override
    public MapFont.Builder fontBuilder(final int lineHeight, final int ascent) {
        return new MapFonts.Builder(lineHeight, ascent);
    }

    @Override
    public MapLayer layer(final MapLayer.Scope scope, final int repaintInterval,
                          final Consumer<MapLayer.Context> painter) {
        Preconditions.checkArgument(scope != null, "The scope of a map layer must not be null");
        Preconditions.checkArgument(repaintInterval >= 0, "The repaint interval of a map layer must not be negative");
        Preconditions.checkArgument(painter != null, "The painter of a map layer must not be null");
        return new PainterLayer(scope, repaintInterval, painter);
    }

    @Override
    public int alignedCenter(final int coordinate, final MapScale scale) {
        Preconditions.checkArgument(scale != null, "The scale must not be null");
        return MapMath.alignedCenter(coordinate, scale);
    }

    @Override
    public int directionOf(final float yaw) {
        return MapMath.directionOf(yaw);
    }

    /**
     * A layer made of a painter, see {@link #layer}.
     */
    private record PainterLayer(MapLayer.Scope scope, int repaintInterval, Consumer<MapLayer.Context> painter)
            implements MapLayer {

        @Override
        public void paint(final Context context) {
            painter.accept(context);
        }
    }

    private static ServerMapView cast(final MapView view) {
        Preconditions.checkArgument(view instanceof ServerMapView, "Not a map of this server: %s", view);
        return (ServerMapView) view;
    }

    private synchronized int allocateId() {
        lastId++;
        lastIdDirty = true;
        missing.remove(lastId);
        return lastId;
    }

    private ServerMapView register(final ServerMapView view) {
        views.put(view.id(), view);
        return view;
    }

    private Path file(final int id) {
        return directory().resolve(id + ".dat");
    }

    private @Nullable ServerMapView load(final int id) {
        final ServerMapView loaded = views.get(id);
        if (loaded != null) {
            return loaded;
        }
        final Long missingSince = missing.get(id);
        if (missingSince != null && System.nanoTime() - missingSince < MISSING_RETRY_NANOS) {
            return null;
        }
        synchronized (this) {
            final ServerMapView again = views.get(id);
            if (again != null) {
                return again;
            }
            final Optional<CompoundBinaryTag> stored;
            try {
                stored = WorldDataFiles.read(file(id))
                        .or(() -> readQuietly(legacyDirectory().resolve("map_" + id + ".dat")));
            } catch (final IOException | RuntimeException e) {
                LOGGER.error("Reading map {} failed", id, e);
                missing.put(id, System.nanoTime());
                return null;
            }
            if (stored.isEmpty()) {
                missing.put(id, System.nanoTime());
                return null;
            }
            final ServerMapView view = new ServerMapView(this, id, false);
            view.restore(MapFile.read(stored.get(), LABELS));
            missing.remove(id);
            views.put(id, view);
            return view;
        }
    }

    private static Optional<CompoundBinaryTag> readQuietly(final Path path) {
        try {
            return WorldDataFiles.read(path);
        } catch (final IOException | RuntimeException e) {
            LOGGER.warn("Reading {} failed", path, e);
            return Optional.empty();
        }
    }

    private void saveDirty() {
        for (final ServerMapView view : views.values()) {
            if (!view.temporary() && !view.isDeleted() && view.takeDirty()) {
                save(view);
            }
        }
        final int last;
        synchronized (this) {
            if (!lastIdDirty) {
                return;
            }
            lastIdDirty = false;
            last = lastId;
        }
        try {
            WorldDataFiles.write(directory().resolve(LAST_ID), CompoundBinaryTag.builder().putInt("map", last).build());
        } catch (final IOException e) {
            LOGGER.error("Saving the last map id failed", e);
            synchronized (this) {
                lastIdDirty = true;
            }
        }
    }

    private void save(final ServerMapView view) {
        try {
            WorldDataFiles.write(file(view.id()), MapFile.write(view.snapshot(), LABELS));
        } catch (final IOException | RuntimeException e) {
            LOGGER.error("Saving map {} failed", view.id(), e);
            view.markDirty();
        }
    }

    private void unloadIdle() {
        final long now = System.nanoTime();
        for (final ServerMapView view : views.values()) {
            if (view.temporary() || view.hasLayers() || view.hasExplicitViewers() || !view.viewers().isEmpty()
                    || now - view.lastViewedNanos() < IDLE_NANOS) {
                continue;
            }
            if (view.takeDirty()) {
                save(view);
            }
            views.remove(view.id(), view);
            tickLocks.remove(view);
        }
    }

    private void tickAll() {
        final long now = tick.incrementAndGet();
        final Map<UUID, MapViewer> online = onlineViewers();
        playerViewers.keySet().removeIf(uuid -> !online.containsKey(uuid));
        for (final ServerMapView view : views.values()) {
            tick(view, now, online);
        }
    }

    private Map<UUID, MapViewer> onlineViewers() {
        final Map<UUID, MapViewer> online = new HashMap<>();
        for (final ServerPlayer player : server.players().online()) {
            if (!player.isRemoved() && player.connection().isInPlayState()) {
                online.put(player.uuid(), viewerOf(player));
            }
        }
        return online;
    }

    /**
     * {@return the viewer of a player, the same object as long as the player stays connected}
     */
    private MapViewer viewerOf(final ServerPlayer player) {
        return playerViewers.compute(player.uuid(), (uuid, previous) ->
                previous != null && previous.player() == player ? previous : new PlayerViewer(player));
    }

    private void tick(final ServerMapView view, final long now, final Map<UUID, MapViewer> online) {
        if (view.isDeleted()) {
            return;
        }
        synchronized (tickLocks.computeIfAbsent(view, v -> new Object())) {
            try {
                view.tick(now, online);
            } catch (final RuntimeException e) {
                LOGGER.error("Ticking map {} failed", view.id(), e);
            }
        }
    }

    /**
     * Sends what changed on a map now.
     */
    @Override
    public void flush(final ServerMapView view) {
        tick(view, tick.get(), onlineViewers());
    }

    @Override
    public int decorationNetworkId(final Key type) {
        return decorationIds.computeIfAbsent(type, key -> {
            final int id = server.registries().frozen().networkId(DECORATION_REGISTRY, key);
            if (id < 0 && unknownDecorations.add(key)) {
                LOGGER.warn("Unknown map decoration type {}, not sending it", key.asString());
            }
            return id;
        });
    }

    // ------------------------------------------------------------------------------------------
    // players
    // ------------------------------------------------------------------------------------------

    /**
     * Records the maps a player carries and draws the terrain around them, on their region thread.
     *
     * @param player the player
     */
    public void tickPlayer(final ServerPlayer player) {
        final long now = tick.get();
        final PlayerInventory inventory = player.inventory();
        final int selected = player.selectedSlot();
        for (int slot = 0; slot < PlayerInventory.SIZE; slot++) {
            final ItemStack stack = inventory.get(slot);
            if (stack.isEmpty() || !stack.id().equals(ItemKeys.FILLED_MAP.key())) {
                continue;
            }
            final Integer id = stack.get(DataComponentTypes.MAP_ID);
            final ServerMapView view = id == null ? null : load(id);
            if (view == null) {
                continue;
            }
            final int steps = view.carriedBy(viewerOf(player), stack, now);
            if (slot == selected || slot == PlayerInventory.OFF_HAND) {
                explore(view, player, steps);
            }
        }
    }

    private void explore(final ServerMapView view, final ServerPlayer player, final int steps) {
        if (!(player.world() instanceof final ServerWorld world)) {
            return;
        }
        final MapTerrain.Area area = view.terrainArea(world.key());
        if (area == null) {
            return;
        }
        final Location location = player.location();
        final WorldSource source = new WorldSource(world);
        terrain.explore(source, view::setPixel, area, location.x(), location.z(), steps);
        if (steps % BANNER_CHECK_INTERVAL == 0) {
            removeBrokenBanners(view, world);
        }
    }

    private static void removeBrokenBanners(final ServerMapView view, final ServerWorld world) {
        for (final Map.Entry<Key, int[]> banner : view.banners().entrySet()) {
            final int[] pos = banner.getValue();
            final ChunkColumn column = world.loadedColumn(pos[0] >> 4, pos[2] >> 4);
            if (column != null && !isBanner(column.getBlock(pos[0] & 15, pos[1], pos[2] & 15))) {
                view.markers().remove(banner.getKey());
            }
        }
    }

    private static boolean isBanner(final BlockState state) {
        return state.name().value().endsWith("_banner");
    }

    /**
     * Creates a map from the empty map in a hand, as vanilla does, on the region thread of the player.
     *
     * @param player  the player
     * @param offHand whether the empty map is in the off hand
     * @return whether the hand held an empty map
     */
    public boolean useEmptyMap(final ServerPlayer player, final boolean offHand) {
        final PlayerInventory inventory = player.inventory();
        final int slot = offHand ? PlayerInventory.OFF_HAND : player.selectedSlot();
        final ItemStack stack = inventory.get(slot);
        if (stack.isEmpty() || !stack.id().equals(ItemKeys.MAP.key())) {
            return false;
        }
        final Location location = player.location();
        final PlayerCreateMapEvent event = server.events().post(new PlayerCreateMapEvent(player, player.world(),
                MapMath.alignedCenter((int) Math.floor(location.x()), MapScale.ONE_TO_ONE),
                MapMath.alignedCenter((int) Math.floor(location.z()), MapScale.ONE_TO_ONE), MapScale.ONE_TO_ONE));
        if (event.isCancelled()) {
            player.updateInventory();
            return true;
        }
        final boolean consume = player.gameMode() != GameMode.CREATIVE;
        final int target = consume && stack.count() == 1 ? slot : freeSlot(inventory);
        if (target < 0) {
            player.updateInventory();
            return true;
        }
        final MapView view = create(event.world(), event.centerX(), event.centerZ(), event.scale());
        if (consume && target != slot) {
            inventory.set(slot, stack.withCount(stack.count() - 1));
        }
        inventory.set(target, view.item());
        player.updateInventory();
        player.playSound(CREATE_SOUND, Sound.Emitter.self());
        return true;
    }

    private static int freeSlot(final PlayerInventory inventory) {
        for (int slot = PlayerInventory.HOTBAR_START; slot < PlayerInventory.MAIN_START + 27; slot++) {
            if (inventory.get(slot).isEmpty()) {
                return slot;
            }
        }
        return -1;
    }

    /**
     * Marks or unmarks a banner with the map held in the main hand, on the region thread of the block.
     *
     * @param player the player
     * @param pos    the block used
     * @return whether the block is a banner and the hand held a map, so nothing else should happen
     */
    public boolean useMapOn(final ServerPlayer player, final BlockPosition pos) {
        final ItemStack held = player.inventory().get(player.selectedSlot());
        if (held.isEmpty() || !held.id().equals(ItemKeys.FILLED_MAP.key())
                || !(player.world() instanceof final ServerWorld world)) {
            return false;
        }
        final ChunkColumn column = world.loadedColumn(pos.blockX() >> 4, pos.blockZ() >> 4);
        if (column == null) {
            return false;
        }
        final BlockState state = column.getBlock(pos.blockX() & 15, pos.blockY(), pos.blockZ() & 15);
        if (!isBanner(state)) {
            return false;
        }
        final ServerMapView view = (ServerMapView) of(held).orElse(null);
        if (view == null || !view.world().map(world.key()::equals).orElse(false)) {
            return true;
        }
        final double x = view.pixelX(pos.blockX() + 0.5);
        final double y = view.pixelY(pos.blockZ() + 0.5);
        if (x < 0 || y < 0 || x > 128 || y > 128) {
            return true;
        }
        final String name = state.name().value();
        final String color = name.replace("_wall_banner", "").replace("_banner", "");
        final BlockEntity entity = column.blockEntity(pos.blockX() & 15, pos.blockY(), pos.blockZ() & 15);
        final Component label = entity == null ? null : customName(entity.dataOrEmpty());
        view.toggleBanner(pos.blockX(), pos.blockY(), pos.blockZ(),
                new MapDecoration(MapFile.bannerType(color), x, y, 0, label));
        return true;
    }

    private static @Nullable Component customName(final CompoundBinaryTag data) {
        BinaryTag name = data.get("CustomName");
        if (name == null) {
            name = data.getCompound("components").get("minecraft:custom_name");
        }
        return name == null ? null : LABELS.read(name);
    }

    // ------------------------------------------------------------------------------------------
    // full renders
    // ------------------------------------------------------------------------------------------

    /**
     * Draws the whole terrain of a map, loading chunks one row at a time.
     */
    @Override
    public CompletableFuture<Void> renderTerrain(final ServerMapView view) {
        final Optional<Key> worldKey = view.world();
        if (worldKey.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        final ServerWorld world = server.worlds().manager().world(worldKey.get());
        if (world == null) {
            return CompletableFuture.failedFuture(new IllegalStateException("World " + worldKey.get() + " is not loaded"));
        }
        final MapTerrain.Area area = new MapTerrain.Area(view.centerX(), view.centerZ(), view.scale().blocksPerPixel());
        return CompletableFuture.runAsync(() -> renderAll(view, world, area), renderers);
    }

    private void renderAll(final ServerMapView view, final ServerWorld world, final MapTerrain.Area area) {
        final int size = area.size();
        final int west = (Math.floorDiv(area.centerX(), size) - 64) * size;
        final int north = (Math.floorDiv(area.centerZ(), size) - 65) * size;
        final int minChunkX = west >> 4;
        final int maxChunkX = (west + 128 * size - 1) >> 4;
        final int minChunkZ = north >> 4;
        final int maxChunkZ = (north + 129 * size - 1) >> 4;
        // load every chunk of the area: a map is at most 128x128 chunks, loaded row by row
        final List<CompletableFuture<?>> pending = new ArrayList<>();
        for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
            pending.clear();
            for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
                if (world.loadedColumn(chunkX, chunkZ) == null) {
                    pending.add(world.chunkAsync(chunkX, chunkZ));
                }
            }
            CompletableFuture.allOf(pending.toArray(CompletableFuture[]::new)).join();
        }
        // draw into a buffer first, so the map is not locked while blocks are read
        final WorldSource source = new WorldSource(world);
        final byte[] pixels = new byte[128 * 128];
        final boolean[] drawn = new boolean[128 * 128];
        for (int x = 0; x < 128; x++) {
            terrain.renderColumn(source, (px, py, pixel) -> {
                pixels[py * 128 + px] = pixel;
                drawn[py * 128 + px] = true;
                return true;
            }, area, x);
        }
        view.canvas().batch(canvas -> {
            for (int i = 0; i < pixels.length; i++) {
                if (drawn[i]) {
                    view.setPixel(i & 127, i >> 7, pixels[i]);
                }
            }
        });
    }

    /**
     * Reads blocks from the chunks of a world that are in memory.
     */
    private static final class WorldSource implements MapTerrain.Source {

        private static final BlockState AIR = BlockState.of(BlockTypeKeys.AIR.key());

        private final ServerWorld world;
        private final boolean ceiling;

        private WorldSource(final ServerWorld world) {
            this.world = world;
            this.ceiling = world.dimensionType().hasCeiling();
        }

        @Override
        public int minY() {
            return world.minY();
        }

        @Override
        public boolean hasCeiling() {
            return ceiling;
        }

        @Override
        public MapTerrain.@Nullable Column column(final int chunkX, final int chunkZ) {
            final ChunkColumn column = world.loadedColumn(chunkX, chunkZ);
            if (column == null) {
                return null;
            }
            final ChunkSection[] sections = column.sections();
            final int minSection = column.minSectionY();
            final int top = (column.topNonEmptySectionY() + 1) << 4;
            return new MapTerrain.Column() {
                @Override
                public int topY() {
                    return top;
                }

                @Override
                public BlockState block(final int localX, final int y, final int localZ) {
                    final int index = (y >> 4) - minSection;
                    if (index < 0 || index >= sections.length) {
                        return AIR;
                    }
                    final ChunkSection section = sections[index];
                    return section == null ? AIR : section.getBlock(localX, y & 15, localZ);
                }
            };
        }
    }

    /**
     * A connected player, seen by maps.
     *
     * @param player the player
     */
    private record PlayerViewer(ServerPlayer player) implements MapViewer {

        @Override
        public UUID uuid() {
            return player.uuid();
        }

        @Override
        public Key world() {
            return player.world().key();
        }

        @Override
        public boolean inCeilingWorld() {
            return player.world().dimensionType().hasCeiling();
        }

        @Override
        public double x() {
            return player.location().x();
        }

        @Override
        public double z() {
            return player.location().z();
        }

        @Override
        public float yaw() {
            return player.location().yaw();
        }

        @Override
        public void send(final int mapId, final int scale, final boolean locked, final @Nullable List<MapIcon> icons,
                         final @Nullable MapPatch patch) {
            List<ClientboundMapItemDataPacket.Decoration> decorations = null;
            if (icons != null) {
                decorations = new ArrayList<>(icons.size());
                for (final MapIcon icon : icons) {
                    decorations.add(new ClientboundMapItemDataPacket.Decoration(
                            icon.type(), icon.x(), icon.y(), icon.rotation(), icon.label()));
                }
            }
            player.connection().send(new ClientboundMapItemDataPacket(mapId, scale, locked, decorations, patch));
        }
    }
}
