package fr.fidorial.map;

import fr.fidorial.item.ItemStack;
import fr.fidorial.world.World;
import fr.fidorial.world.block.BlockData;

import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Creates and finds {@linkplain MapView maps}, and makes what plugins draw on them: canvases,
 * images, fonts and layers.
 *
 * <p>Maps are saved with the world, in {@code data/minecraft/maps}, under the same ids and in the
 * same format as vanilla, so maps made on a vanilla server keep working. They are loaded the first
 * time someone needs them and unloaded once nobody has looked at them for a while.</p>
 *
 * @see fr.fidorial.Server#maps()
 * @since 0.1.0
 */
public interface MapManager {

    /**
     * Creates a map of the terrain of a world, drawn as players carrying it explore.
     *
     * @param world   the world
     * @param centerX the X coordinate of the block at the center
     * @param centerZ the Z coordinate of the block at the center
     * @param scale   how many blocks a pixel covers
     * @return the new map, saved with the world
     * @see #alignedCenter(int, MapScale)
     * @since 0.1.0
     */
    MapView create(World world, int centerX, int centerZ, MapScale scale);

    /**
     * Creates a map that shows no world: a blank canvas for plugins to draw on.
     *
     * @return the new map, saved with the world
     * @since 0.1.0
     */
    MapView createBlank();

    /**
     * Creates a blank map that is never saved, for content that only makes sense while the server
     * runs: a game screen, a live chart. Its id is taken from the top of the id range, far from the
     * ids of saved maps, and given back by {@link #delete(MapView)}.
     *
     * @return the new map
     * @since 0.1.0
     */
    MapView createTemporary();

    /**
     * Finds a map, loading it from disk if needed.
     *
     * @param id the id
     * @return the map, or empty if no map has this id
     * @since 0.1.0
     */
    Optional<MapView> get(int id);

    /**
     * Finds the map an item shows.
     *
     * @param stack an item; only a {@code filled_map} with a {@code minecraft:map_id} shows one
     * @return the map, or empty if the item shows none, or one that does not exist
     * @since 0.1.0
     */
    Optional<MapView> of(ItemStack stack);

    /**
     * {@return the maps currently in memory}
     *
     * @since 0.1.0
     */
    Collection<MapView> loaded();

    /**
     * Deletes a map: its file for a saved map, its id for a temporary one. Items still showing it
     * show an unknown map.
     *
     * @param view the map
     * @since 0.1.0
     */
    void delete(MapView view);

    /**
     * Copies a map as a cartography table locks it: a new map with the same pixels and markers
     * whose terrain never changes again.
     *
     * @param source the map to copy
     * @return the new, locked map
     * @since 0.1.0
     */
    MapView lockedCopy(MapView source);

    /**
     * Makes the zoomed out version of a map, as a cartography table with paper does: a new, empty
     * map of the next {@linkplain MapScale scale}, centered on the grid cell holding the old center.
     *
     * @param source the map to zoom out
     * @return the new map; a copy of the same scale if the source is already the widest
     * @since 0.1.0
     */
    MapView zoomedOut(MapView source);

    /**
     * Returns the color a block is drawn with on terrain maps.
     *
     * <p>That is the color given by the {@linkplain fr.fidorial.world.block.BlockBehaviour#mapColor
     * behaviour} of the block if it gives one, or the vanilla one.</p>
     *
     * @param block a block state
     * @return its color, {@link MapColor#NONE} for blocks that maps look through
     * @since 0.1.0
     */
    MapColor colorOf(BlockData block);

    /**
     * {@return the colors of map pixels}
     *
     * @since 0.1.0
     */
    MapPalette palette();

    /**
     * Creates a canvas that belongs to no map, filled with transparent pixels. Useful to prepare a
     * picture, then {@linkplain MapCanvas#copy(MapCanvas) copy} it onto maps.
     *
     * @return the canvas
     * @since 0.1.0
     */
    MapCanvas createCanvas();

    /**
     * Wraps pixels that are already map pixels.
     *
     * @param width  the width, at least {@code 1}
     * @param height the height, at least {@code 1}
     * @param pixels {@code width * height} map pixels, row by row; copied
     * @return the image
     * @since 0.1.0
     */
    MapImage image(int width, int height, byte[] pixels);

    /**
     * Converts colors to map pixels. Colors with an alpha below {@code 128} become transparent, the
     * others take the closest map color.
     *
     * @param width     the width, at least {@code 1}
     * @param height    the height, at least {@code 1}
     * @param argb      {@code width * height} colors as {@code 0xAARRGGBB}, row by row
     * @param dithering how to approximate colors
     * @return the image
     * @since 0.1.0
     */
    MapImage image(int width, int height, int[] argb, MapImage.Dithering dithering);

    /**
     * Converts a picture to map pixels, at its own size.
     *
     * @param image     the picture
     * @param dithering how to approximate colors
     * @return the image
     * @since 0.1.0
     */
    MapImage image(BufferedImage image, MapImage.Dithering dithering);

    /**
     * Scales a picture, then converts it to map pixels. To fill a wall of {@code n×m} maps, scale to
     * {@code 128n×128m} and cut the result into {@linkplain MapImage#tile(int, int) tiles}.
     *
     * @param image     the picture
     * @param width     the width to scale to, at least {@code 1}
     * @param height    the height to scale to, at least {@code 1}
     * @param dithering how to approximate colors
     * @return the image
     * @since 0.1.0
     */
    MapImage image(BufferedImage image, int width, int height, MapImage.Dithering dithering);

    /**
     * {@return the built-in font: proportional, 7 pixels high, with the accents of Western European
     * languages above capitals}
     *
     * @since 0.1.0
     */
    MapFont standardFont();

    /**
     * Starts a font of your own.
     *
     * @param lineHeight how many pixels apart lines are, at least {@code 1}
     * @param ascent     how many pixels below the top of a line glyph row {@code 0} is, at least {@code 0}
     * @return the builder
     * @since 0.1.0
     */
    MapFont.Builder fontBuilder(int lineHeight, int ascent);

    /**
     * Creates a layer from a painter.
     *
     * @param scope           for whom it is painted
     * @param repaintInterval repaint every that many ticks; {@code 0} only repaints on
     *                        {@link MapView#repaint()} and, for a per-viewer layer, when someone starts
     *                        seeing the map
     * @param painter         what to paint
     * @return the layer, to {@linkplain MapView#addLayer(MapLayer) add} to maps
     * @since 0.1.0
     */
    MapLayer layer(MapLayer.Scope scope, int repaintInterval, Consumer<MapLayer.Context> painter);

    /**
     * Snaps a coordinate to the center of the grid cell it falls in, as vanilla does for new maps,
     * so that maps of the same scale tile the world without overlapping.
     *
     * @param coordinate a block X or Z coordinate
     * @param scale      the scale of the map
     * @return the X or Z of the center of the map covering that coordinate
     * @since 0.1.0
     */
    int alignedCenter(int coordinate, MapScale scale);

    /**
     * Converts a yaw to the {@linkplain MapDecoration#direction() direction} of an icon.
     *
     * @param yaw a yaw in degrees, as in a location: {@code 0} faces south, {@code 90} west
     * @return the direction a player arrow must have to point the same way on a terrain map
     * @since 0.1.0
     */
    int directionOf(float yaw);
}
