package fr.fidorial.map;

import fr.fidorial.entity.player.Player;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.world.BlockFace;
import net.kyori.adventure.key.Key;

/**
 * An item frame holding a map that exists only for one player.
 *
 * <p>The frame is not part of the world: other players do not see it, nothing can break it and it is
 * never saved. While it hangs, the map is sent to its viewer as if {@linkplain MapView#show(Player)
 * shown} to them. Hang one with {@link MapView#hang}.</p>
 *
 * <p>A frame comes down when it is {@linkplain #remove() removed}, when its map is
 * {@linkplain MapManager#delete(MapView) deleted}, and when its viewer leaves or goes to another
 * world, since their client forgets it then. The client also forgets it when its chunk leaves the
 * view distance of the viewer, so hang frames near them.</p>
 *
 * @since 0.1.0
 */
public interface MapFrame {

    /**
     * {@return the map in the frame}
     *
     * @since 0.1.0
     */
    MapView map();

    /**
     * {@return the player who sees the frame; only valid while they stay connected}
     *
     * @since 0.1.0
     */
    Player viewer();

    /**
     * {@return the world the frame hangs in, the world of the viewer when it was hung}
     *
     * @since 0.1.0
     */
    Key world();

    /**
     * {@return the block the frame takes up}
     *
     * @since 0.1.0
     */
    BlockPosition block();

    /**
     * {@return the direction the front of the frame faces; it hangs on the opposite side of its block}
     *
     * @since 0.1.0
     */
    BlockFace facing();

    /**
     * {@return whether the frame itself is invisible, leaving only the map}
     *
     * @since 0.1.0
     */
    boolean invisible();

    /**
     * {@return whether the frame came down}
     *
     * @since 0.1.0
     */
    boolean isRemoved();

    /**
     * Takes the frame down. Does nothing when it already came down.
     *
     * @since 0.1.0
     */
    void remove();
}
