package fr.fidorial.event.player;

import fr.fidorial.entity.player.Player;
import fr.fidorial.event.Cancellable;
import fr.fidorial.map.MapManager;
import fr.fidorial.map.MapScale;
import fr.fidorial.world.World;

/**
 * Fired when a player uses an empty map, before the new map is created.
 *
 * <p>Cancelling keeps the empty map. The area of the new map can be changed; by default it is the
 * {@linkplain MapManager#alignedCenter(int, MapScale) grid cell} of the player at
 * {@link MapScale#ONE_TO_ONE}.
 * Listeners run on the region thread of the player.</p>
 *
 * @since 0.1.0
 */
public final class PlayerCreateMapEvent implements PlayerEvent, Cancellable {

    private final Player player;
    private final World world;
    private int centerX;
    private int centerZ;
    private MapScale scale;
    private boolean cancelled;

    /**
     * Creates an event.
     *
     * @param player  the player
     * @param world   the world the map will show
     * @param centerX the X coordinate of the center
     * @param centerZ the Z coordinate of the center
     * @param scale   the scale
     * @since 0.1.0
     */
    public PlayerCreateMapEvent(final Player player, final World world, final int centerX, final int centerZ,
                                final MapScale scale) {
        this.player = player;
        this.world = world;
        this.centerX = centerX;
        this.centerZ = centerZ;
        this.scale = scale;
    }

    @Override
    public Player player() {
        return player;
    }

    /**
     * {@return the world the map will show}
     *
     * @since 0.1.0
     */
    public World world() {
        return world;
    }

    /**
     * {@return the X coordinate of the center of the map}
     *
     * @since 0.1.0
     */
    public int centerX() {
        return centerX;
    }

    /**
     * {@return the Z coordinate of the center of the map}
     *
     * @since 0.1.0
     */
    public int centerZ() {
        return centerZ;
    }

    /**
     * Moves the map.
     *
     * @param x the X coordinate of the center
     * @param z the Z coordinate of the center
     * @since 0.1.0
     */
    public void center(final int x, final int z) {
        this.centerX = x;
        this.centerZ = z;
    }

    /**
     * {@return how many blocks a pixel of the map will cover}
     *
     * @since 0.1.0
     */
    public MapScale scale() {
        return scale;
    }

    /**
     * Changes the scale. The center is kept: realign it with
     * {@link fr.fidorial.map.MapManager#alignedCenter(int, MapScale)} to keep maps of that scale
     * tiling the world.
     *
     * @param scale the scale
     * @since 0.1.0
     */
    public void scale(final MapScale scale) {
        this.scale = scale;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }
}
