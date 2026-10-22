package fr.fidorial.map;

import fr.fidorial.entity.player.Player;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.world.BlockFace;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * A map known to the server: the picture behind every {@code filled_map} item with its
 * {@linkplain #id() id}.
 *
 * <p>A map is made of three things, drawn in this order:</p>
 * <ol>
 *   <li>its {@linkplain #canvas() canvas}, the pixels it stores. On a map that shows a
 *   {@linkplain #world() world} and is not {@linkplain #locked() locked}, the terrain is drawn
 *   there as players carrying it explore, like in vanilla;</li>
 *   <li>its {@linkplain #layers() layers}, painted by plugins on top, never stored;</li>
 *   <li>icons: the persistent {@linkplain #markers() markers}, those of the layers, and the arrows of
 *   players when the map {@linkplain #tracksPlayers() tracks them}.</li>
 * </ol>
 *
 * <p>Players see a map while they carry it, while it is {@linkplain #show(Player) shown} to them,
 * and while it {@linkplain #hang hangs} in a frame only they see. Changes reach them on the next
 * tick, or right away with {@link #flush()}. A map is safe to use from any thread.</p>
 *
 * @see MapManager
 * @since 0.1.0
 */
public interface MapView {

    /**
     * {@return the id, stored on items in the {@code minecraft:map_id} component}
     *
     * @since 0.1.0
     */
    int id();

    /**
     * {@return whether the map is temporary: never saved, and forgotten with the server}
     *
     * @see MapManager#createTemporary()
     * @since 0.1.0
     */
    boolean temporary();

    /**
     * {@return the world whose terrain and players the map shows; empty for a blank map}
     *
     * @since 0.1.0
     */
    Optional<Key> world();

    /**
     * Sets the world the map shows. Pixels already drawn stay until explored again.
     *
     * @param world the key of the world, or {@code null} to show none
     * @since 0.1.0
     */
    void world(@Nullable Key world);

    /**
     * {@return the X coordinate of the block at the center of the map}
     *
     * @since 0.1.0
     */
    int centerX();

    /**
     * {@return the Z coordinate of the block at the center of the map}
     *
     * @since 0.1.0
     */
    int centerZ();

    /**
     * Moves the area the map shows. Pixels already drawn stay until explored again.
     *
     * @param x the X coordinate of the new center
     * @param z the Z coordinate of the new center
     * @see MapManager#alignedCenter(int, MapScale)
     * @since 0.1.0
     */
    void center(int x, int z);

    /**
     * {@return how many blocks a pixel covers}
     *
     * @since 0.1.0
     */
    MapScale scale();

    /**
     * Changes how many blocks a pixel covers. Pixels already drawn stay until explored again.
     *
     * @param scale the scale
     * @since 0.1.0
     */
    void scale(MapScale scale);

    /**
     * {@return whether the terrain stopped being drawn, like a map locked in a cartography table}
     *
     * @since 0.1.0
     */
    boolean locked();

    /**
     * Locks or unlocks the terrain. The canvas can still be drawn on by plugins either way.
     *
     * @param locked whether to stop drawing the terrain
     * @since 0.1.0
     */
    void locked(boolean locked);

    /**
     * {@return whether players in the world of the map are shown as arrows}
     *
     * @since 0.1.0
     */
    boolean tracksPlayers();

    /**
     * Shows or hides the arrows of players.
     *
     * @param tracksPlayers whether to show players
     * @since 0.1.0
     */
    void tracksPlayers(boolean tracksPlayers);

    /**
     * {@return whether players far away from the map are still shown, as small dots on its edge}
     *
     * <p>Otherwise, only players up to 320 blocks outside it are, like vanilla maps that were not
     * made from an explorer map.</p>
     *
     * @since 0.1.0
     */
    boolean tracksFarPlayers();

    /**
     * Shows or hides players far from the map.
     *
     * @param tracksFarPlayers whether to show far players
     * @since 0.1.0
     */
    void tracksFarPlayers(boolean tracksFarPlayers);

    /**
     * {@return the pixels the map stores}
     *
     * @since 0.1.0
     */
    MapCanvas canvas();

    /**
     * {@return the icons the map stores}
     *
     * @since 0.1.0
     */
    MapMarkers markers();

    /**
     * Adds a layer on top of the others, and paints it.
     *
     * @param layer the layer
     * @since 0.1.0
     */
    void addLayer(MapLayer layer);

    /**
     * Removes a layer.
     *
     * @param layer the layer
     * @return whether the map had it
     * @since 0.1.0
     */
    boolean removeLayer(MapLayer layer);

    /**
     * {@return the layers, bottom to top}
     *
     * @since 0.1.0
     */
    List<MapLayer> layers();

    /**
     * Repaints every layer on the next tick.
     *
     * @since 0.1.0
     */
    void repaint();

    /**
     * Repaints one layer on the next tick.
     *
     * @param layer the layer
     * @since 0.1.0
     */
    void repaint(MapLayer layer);

    /**
     * {@return the online players who currently see the map}
     *
     * @since 0.1.0
     */
    Collection<Player> viewers();

    /**
     * Sends the map to a player even when they do not carry it, until {@link #hide(Player)} or they
     * leave. Needed when the map is only visible to them in a frame or another item.
     *
     * @param player the player
     * @since 0.1.0
     */
    void show(Player player);

    /**
     * Stops sending the map to a player who does not carry it.
     *
     * @param player the player
     * @since 0.1.0
     */
    void hide(Player player);

    /**
     * Hangs the map in an item frame that only one player sees.
     *
     * <p>The frame takes up {@code block} in the world of the viewer and shows its front towards
     * {@code facing}: a frame facing {@link BlockFace#SOUTH} hangs on the north side of the block and
     * is looked at from the south. Nothing needs to be behind it. Several frames, even of the same
     * map, may take up the same block.</p>
     *
     * @param viewer    the only player who sees the frame
     * @param block     the block the frame takes up
     * @param facing    the direction the front of the frame faces
     * @param invisible whether to hide the frame itself, leaving only the map
     * @return the frame, hanging until {@linkplain MapFrame#remove() removed}
     * @throws IllegalArgumentException if the viewer is not connected, or the map was deleted
     * @since 0.1.0
     */
    MapFrame hang(Player viewer, BlockPosition block, BlockFace facing, boolean invisible);

    /**
     * {@return the frames this map hangs in}
     *
     * @since 0.1.0
     */
    Collection<MapFrame> frames();

    /**
     * Sends what changed to everyone who sees the map, now rather than on the next tick.
     *
     * @since 0.1.0
     */
    void flush();

    /**
     * Draws the terrain of the whole map now, instead of waiting for players to explore it.
     *
     * <p>Chunks that are not loaded are loaded, or generated, in the background. Does nothing on a
     * map without world.</p>
     *
     * @return completed once the canvas holds the whole area
     * @since 0.1.0
     */
    CompletableFuture<Void> renderTerrain();

    /**
     * Creates an item showing this map.
     *
     * @return a single {@code filled_map}
     * @since 0.1.0
     */
    ItemStack item();

    /**
     * Converts a world X coordinate to a canvas column.
     *
     * @param x the X coordinate
     * @return the column, outside {@code 0}-{@code 128} when the coordinate is off the map
     * @since 0.1.0
     */
    double pixelX(double x);

    /**
     * Converts a world Z coordinate to a canvas row.
     *
     * @param z the Z coordinate
     * @return the row, outside {@code 0}-{@code 128} when the coordinate is off the map
     * @since 0.1.0
     */
    double pixelY(double z);
}
