package fr.euphyllia.fidorial.server.map;

import fr.fidorial.map.MapCanvas;
import fr.fidorial.map.MapDecoration;
import fr.fidorial.map.MapScale;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.MapDecorationType;
import fr.fidorial.registry.keys.MapDecorationTypeKeys;
import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.IntArrayBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * The content of a map data file, {@code data/minecraft/maps/<id>.dat}, in the vanilla format.
 *
 * <p>Vanilla fields: {@code dimension}, {@code xCenter}, {@code zCenter}, {@code scale},
 * {@code locked}, {@code trackingPosition}, {@code unlimitedTracking}, {@code colors},
 * {@code banners} and {@code frames}. Markers added by plugins go in {@code fidorial:markers},
 * which vanilla ignores.</p>
 */
final class MapFile {

    private static final String DIMENSION = "dimension";
    private static final String X_CENTER = "xCenter";
    private static final String Z_CENTER = "zCenter";
    private static final String SCALE = "scale";
    private static final String LOCKED = "locked";
    private static final String TRACKING = "trackingPosition";
    private static final String UNLIMITED = "unlimitedTracking";
    private static final String COLORS = "colors";
    private static final String BANNERS = "banners";
    private static final String FRAMES = "frames";
    private static final String MARKERS = "fidorial:markers";
    private static final String BLANK = "fidorial:blank";
    private static final Key OVERWORLD = Key.key("overworld");
    private static final String BANNER_TYPE_PREFIX = "banner_";
    private static final String BANNER_NAMESPACE = "fidorial";

    private MapFile() {
    }

    /**
     * Converts labels to NBT and back.
     */
    interface Labels {

        @Nullable BinaryTag write(Component label);

        @Nullable Component read(BinaryTag tag);
    }

    /**
     * Everything a map stores.
     *
     * @param world            the world shown, {@code null} for a blank map
     * @param centerX          the X coordinate of the center
     * @param centerZ          the Z coordinate of the center
     * @param scale            the scale
     * @param locked           whether the terrain is locked
     * @param tracksPlayers    whether players are shown
     * @param tracksFarPlayers whether far players are shown
     * @param pixels           the pixels
     * @param banners          the banners, by marker id
     * @param markers          the other markers, by id
     */
    record Data(@Nullable Key world, int centerX, int centerZ, MapScale scale, boolean locked, boolean tracksPlayers,
                boolean tracksFarPlayers, byte[] pixels, Map<Key, Banner> banners, Map<Key, MapDecoration> markers) {
    }

    /**
     * A marked banner.
     *
     * @param x          the X coordinate of the block
     * @param y          the Y coordinate
     * @param z          the Z coordinate
     * @param decoration the icon, whose type gives the color and label the name
     */
    record Banner(int x, int y, int z, MapDecoration decoration) {
    }

    static CompoundBinaryTag write(final Data data, final Labels labels) {
        final CompoundBinaryTag.Builder root = CompoundBinaryTag.builder()
                .putString(DIMENSION, (data.world() == null ? OVERWORLD : data.world()).asString())
                .putInt(X_CENTER, data.centerX())
                .putInt(Z_CENTER, data.centerZ())
                .putByte(SCALE, (byte) data.scale().level())
                .putBoolean(LOCKED, data.locked())
                .putBoolean(TRACKING, data.tracksPlayers())
                .putBoolean(UNLIMITED, data.tracksFarPlayers())
                .putByteArray(COLORS, data.pixels());
        if (data.world() == null) {
            // vanilla needs a dimension; remember that this map shows none
            root.putBoolean(BLANK, true);
        }
        final ListBinaryTag.Builder<CompoundBinaryTag> banners = ListBinaryTag.builder(BinaryTagTypes.COMPOUND);
        for (final Banner banner : data.banners().values()) {
            final CompoundBinaryTag.Builder tag = CompoundBinaryTag.builder()
                    .put("pos", IntArrayBinaryTag.intArrayBinaryTag(banner.x(), banner.y(), banner.z()))
                    .putString("color", bannerColor(banner.decoration().type()));
            final Component label = banner.decoration().label();
            if (label != null) {
                final BinaryTag name = labels.write(label);
                if (name != null) {
                    tag.put("name", name);
                }
            }
            banners.add(tag.build());
        }
        root.put(BANNERS, banners.build());
        root.put(FRAMES, ListBinaryTag.empty());
        if (!data.markers().isEmpty()) {
            final ListBinaryTag.Builder<CompoundBinaryTag> markers = ListBinaryTag.builder(BinaryTagTypes.COMPOUND);
            data.markers().forEach((id, decoration) -> {
                final CompoundBinaryTag.Builder tag = CompoundBinaryTag.builder()
                        .putString("id", id.asString())
                        .putString("type", decoration.type().key().asString())
                        .putDouble("x", decoration.x())
                        .putDouble("y", decoration.y())
                        .putInt("direction", decoration.direction());
                final Component label = decoration.label();
                if (label != null) {
                    final BinaryTag name = labels.write(label);
                    if (name != null) {
                        tag.put("label", name);
                    }
                }
                markers.add(tag.build());
            });
            root.put(MARKERS, markers.build());
        }
        return root.build();
    }

    static Data read(final CompoundBinaryTag root, final Labels labels) {
        final Key world = root.getBoolean(BLANK) ? null : parseKey(root.getString(DIMENSION), OVERWORLD);
        final int centerX = root.getInt(X_CENTER);
        final int centerZ = root.getInt(Z_CENTER);
        final MapScale scale = MapMath.scale(root.getByte(SCALE));
        final byte[] stored = root.getByteArray(COLORS);
        final byte[] pixels = new byte[MapCanvas.SIZE * MapCanvas.SIZE];
        System.arraycopy(stored, 0, pixels, 0, Math.min(stored.length, pixels.length));
        final Map<Key, Banner> banners = new LinkedHashMap<>();
        for (final BinaryTag element : root.getList(BANNERS, BinaryTagTypes.COMPOUND)) {
            final CompoundBinaryTag tag = (CompoundBinaryTag) element;
            final int[] pos = position(tag);
            if (pos == null) {
                continue;
            }
            final TypedKey<MapDecorationType> type = bannerType(tag.getString("color", "white"));
            final BinaryTag name = tag.get("name");
            final Component label = name == null ? null : labels.read(name);
            banners.put(bannerKey(pos[0], pos[1], pos[2]), new Banner(pos[0], pos[1], pos[2], new MapDecoration(type, 64, 64, 0, label)));
        }
        final Map<Key, MapDecoration> markers = new LinkedHashMap<>();
        for (final BinaryTag element : root.getList(MARKERS, BinaryTagTypes.COMPOUND)) {
            final CompoundBinaryTag tag = (CompoundBinaryTag) element;
            final Key type = parseKey(tag.getString("type"), null);
            final Key id = parseKey(tag.getString("id"), null);
            if (type == null || id == null) {
                continue;
            }
            final BinaryTag name = tag.get("label");
            final Component label = name == null ? null : labels.read(name);
            markers.put(id, new MapDecoration(TypedKey.create(RegistryKey.MAP_DECORATION_TYPE, type),
                    tag.getDouble("x"), tag.getDouble("y"), Math.floorMod(tag.getInt("direction"), 16), label));
        }
        return new Data(world, centerX, centerZ, scale, root.getBoolean(LOCKED),
                !root.keySet().contains(TRACKING) || root.getBoolean(TRACKING), root.getBoolean(UNLIMITED), pixels,
                banners, markers);
    }

    private static int @Nullable [] position(final CompoundBinaryTag tag) {
        final BinaryTag pos = tag.get("pos");
        if (pos instanceof final IntArrayBinaryTag array && array.size() == 3) {
            return new int[]{array.get(0), array.get(1), array.get(2)};
        }
        // before 1.20.5: Pos: {X, Y, Z}
        final CompoundBinaryTag legacy = tag.getCompound("Pos");
        if (legacy.keySet().contains("X")) {
            return new int[]{legacy.getInt("X"), legacy.getInt("Y"), legacy.getInt("Z")};
        }
        return null;
    }

    /**
     * {@return the key a marked banner is stored under, see {@link fr.fidorial.map.MapMarkers}}
     */
    static Key bannerKey(final int x, final int y, final int z) {
        return Key.key(BANNER_NAMESPACE, "banner/" + x + "/" + y + "/" + z);
    }

    private static String bannerColor(final TypedKey<MapDecorationType> type) {
        final String path = type.key().value();
        return path.startsWith(BANNER_TYPE_PREFIX) ? path.substring(BANNER_TYPE_PREFIX.length()) : "white";
    }

    static TypedKey<MapDecorationType> bannerType(final String color) {
        final String name = BANNER_TYPE_PREFIX + color.toLowerCase(Locale.ROOT);
        return MapDecorationTypeKeys.values()
                .filter(key -> key.key().value().equals(name))
                .findFirst()
                .orElse(MapDecorationTypeKeys.BANNER_WHITE);
    }

    private static @Nullable Key parseKey(final String value, final @Nullable Key fallback) {
        if (value == null || value.isEmpty()) {
            return fallback;
        }
        try {
            return Key.key(value);
        } catch (final InvalidKeyException _) {
            return fallback;
        }
    }
}
