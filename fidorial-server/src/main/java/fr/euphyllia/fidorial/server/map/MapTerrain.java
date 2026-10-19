package fr.euphyllia.fidorial.server.map;

import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.map.MapColor;
import fr.fidorial.map.MapShade;
import fr.fidorial.map.MapView;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Draws the terrain of a world on maps, the way vanilla clients expect it to look.
 *
 * <p>Each pixel covers a square of blocks. Its color is the most common color among the topmost
 * visible block of each column of the square; its shade shows relief, comparing the average height
 * of the square with that of the square north of it. Water is shaded by depth instead.</p>
 */
final class MapTerrain {

    /**
     * Blocks that are always full of water.
     */
    private static final Set<String> WATER_PLANTS = Set.of("seagrass", "tall_seagrass", "kelp", "kelp_plant",
            "bubble_column");

    private final BlockMapColors colors;

    MapTerrain(final BlockMapColors colors) {
        this.colors = colors;
    }

    /**
     * Where blocks are read from. Only chunks already in memory are used.
     */
    interface Source {

        int minY();

        /**
         * Whether the dimension has a ceiling, like the Nether: maps then show noise, not terrain.
         */
        boolean hasCeiling();

        @Nullable Column column(int chunkX, int chunkZ);
    }

    /**
     * One chunk column.
     */
    interface Column {

        /**
         * One past the highest block that may not be air.
         */
        int topY();

        BlockState block(int localX, int y, int localZ);
    }

    /**
     * Where pixels go.
     */
    interface Target {

        /**
         * Sets a pixel, returns whether it changed.
         */
        boolean set(int x, int y, byte pixel);
    }

    /**
     * What one square of blocks looks like.
     *
     * @param color  the color
     * @param height the average height of the surface
     * @param depth  the average depth of water on it
     */
    record Sample(MapColor color, double height, int depth) {
    }

    /**
     * Samples the square of {@code size×size} blocks whose north west corner is {@code (x, z)}. The
     * square lies in one chunk, since {@code size} divides 16 and {@code x}, {@code z} are multiples
     * of it.
     *
     * @return the sample, or {@code null} if the chunk is not loaded
     */
    @Nullable Sample sample(final Source source, final int x, final int z, final int size) {
        final Column column = source.column(x >> 4, z >> 4);
        if (column == null) {
            return null;
        }
        if (source.hasCeiling()) {
            int noise = x + z * 231871;
            noise = noise * noise * 31287121 + noise * 11;
            return new Sample((noise >> 20 & 1) == 0 ? MapColor.DIRT : MapColor.STONE, 100.0, 0);
        }
        final int minY = source.minY();
        final Map<MapColor, Integer> counts = new LinkedHashMap<>();
        double height = 0.0;
        int depth = 0;
        for (int dx = 0; dx < size; dx++) {
            for (int dz = 0; dz < size; dz++) {
                final int localX = (x + dx) & 15;
                final int localZ = (z + dz) & 15;
                int y = column.topY();
                MapColor color;
                if (y <= minY + 1) {
                    color = MapColor.STONE; // bedrock
                } else {
                    BlockState state;
                    do {
                        y--;
                        state = column.block(localX, y, localZ);
                        color = colors.of(state);
                    } while (color == MapColor.NONE && y > minY);
                    if (y > minY && isFluid(state)) {
                        int below = y - 1;
                        BlockState under;
                        do {
                            under = column.block(localX, below--, localZ);
                            depth++;
                        } while (below > minY && isFluid(under));
                        color = fluidColor(state, color);
                    }
                }
                height += (double) y / (size * size);
                counts.merge(color, 1, Integer::sum);
            }
        }
        MapColor best = MapColor.NONE;
        int bestCount = 0;
        for (final Map.Entry<MapColor, Integer> entry : counts.entrySet()) {
            if (entry.getValue() > bestCount) {
                best = entry.getKey();
                bestCount = entry.getValue();
            }
        }
        return new Sample(best, height, depth / (size * size));
    }

    /**
     * Picks the shade of a pixel.
     *
     * @param sample      the pixel
     * @param northHeight the average height of the pixel north of it
     * @param x           the column of the pixel, for the checkerboard
     * @param y           the row of the pixel
     * @param size        how many blocks a pixel covers
     */
    static MapShade shade(final Sample sample, final double northHeight, final int x, final int y, final int size) {
        final int checker = (x + y) & 1;
        if (sample.color() == MapColor.WATER) {
            final double level = sample.depth() * 0.1 + checker * 0.2;
            return level < 0.5 ? MapShade.LIGHT : level > 0.9 ? MapShade.SHADOW : MapShade.NORMAL;
        }
        final double slope = (sample.height() - northHeight) * 4.0 / (size + 4) + (checker - 0.5) * 0.4;
        return slope > 0.6 ? MapShade.LIGHT : slope < -0.6 ? MapShade.SHADOW : MapShade.NORMAL;
    }

    /**
     * Updates the part of a map around someone carrying it, as vanilla does every tick: a disc of
     * pixels around them, one column in sixteen per tick, the next one too when a column changed.
     *
     * @param source the world
     * @param target the canvas
     * @param state  the settings of the map
     * @param worldX the X coordinate of the carrier
     * @param worldZ the Z coordinate of the carrier
     * @param step   how many times this carrier updated the map
     * @return whether a pixel changed
     */
    boolean explore(final Source source, final Target target, final Area state, final double worldX, final double worldZ,
                    final int step) {
        final int size = state.size();
        final int centerX = state.centerX();
        final int centerZ = state.centerZ();
        final int px = Math.floorDiv((int) Math.floor(worldX) - centerX, size) + 64;
        final int pz = Math.floorDiv((int) Math.floor(worldZ) - centerZ, size) + 64;
        int radius = 128 / size;
        if (source.hasCeiling()) {
            radius /= 2;
        }
        boolean changed = false;
        boolean carry = false;
        for (int x = px - radius + 1; x < px + radius; x++) {
            if ((x & 15) != (step & 15) && !carry) {
                continue;
            }
            carry = false;
            double north = 0.0;
            for (int y = pz - radius - 1; y < pz + radius; y++) {
                if (x < 0 || y < -1 || x >= 128 || y >= 128) {
                    continue;
                }
                final int distance = (x - px) * (x - px) + (y - pz) * (y - pz);
                final boolean rim = distance > (radius - 2) * (radius - 2);
                final int blockX = (Math.floorDiv(centerX, size) + x - 64) * size;
                final int blockZ = (Math.floorDiv(centerZ, size) + y - 64) * size;
                final Sample sample = sample(source, blockX, blockZ, size);
                if (sample == null) {
                    continue;
                }
                final MapShade shade = shade(sample, north, x, y, size);
                north = sample.height();
                if (y >= 0 && distance < radius * radius && (!rim || ((x + y) & 1) != 0)) {
                    final boolean pixelChanged = target.set(x, y, ServerMapPalette.of(sample.color(), shade));
                    carry |= pixelChanged;
                    changed |= pixelChanged;
                }
            }
        }
        return changed;
    }

    /**
     * Draws one column of pixels completely, for {@link MapView#renderTerrain()}.
     *
     * @return how many pixels could be drawn
     */
    int renderColumn(final Source source, final Target target, final Area state, final int x) {
        final int size = state.size();
        final int blockX = (Math.floorDiv(state.centerX(), size) + x - 64) * size;
        int drawn = 0;
        double north = Double.NaN;
        for (int y = -1; y < 128; y++) {
            final int blockZ = (Math.floorDiv(state.centerZ(), size) + y - 64) * size;
            final Sample sample = sample(source, blockX, blockZ, size);
            if (sample == null) {
                north = Double.NaN;
                continue;
            }
            final double previous = Double.isNaN(north) ? sample.height() : north;
            north = sample.height();
            if (y >= 0) {
                target.set(x, y, ServerMapPalette.of(sample.color(), shade(sample, previous, x, y, size)));
                drawn++;
            }
        }
        return drawn;
    }

    private static boolean isFluid(final BlockState state) {
        return state.isFluid() || WATER_PLANTS.contains(state.name().value());
    }

    /**
     * A block holding a fluid shows the fluid, unless its top face is solid.
     */
    private static MapColor fluidColor(final BlockState state, final MapColor own) {
        final String name = state.name().value();
        if (name.equals("water") || name.equals("lava")) {
            return own;
        }
        final Map<String, String> properties = state.properties();
        final boolean solidTop = "top".equals(properties.get("type")) || "double".equals(properties.get("type"))
                || "top".equals(properties.get("half")) && name.endsWith("_stairs")
                || name.endsWith("_grate");
        return solidTop ? own : MapColor.WATER;
    }

    /**
     * The settings of a map the renderer needs.
     *
     * @param centerX the X coordinate of the center
     * @param centerZ the Z coordinate of the center
     * @param size    how many blocks a pixel covers
     */
    record Area(int centerX, int centerZ, int size) {
    }
}
