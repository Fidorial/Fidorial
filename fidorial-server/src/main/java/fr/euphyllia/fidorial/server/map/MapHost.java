package fr.euphyllia.fidorial.server.map;

import fr.fidorial.entity.player.Player;
import fr.fidorial.map.MapFrame;
import fr.fidorial.map.MapView;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.world.BlockFace;
import net.kyori.adventure.key.Key;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * What a map needs from the module that owns it.
 */
interface MapHost {

    /**
     * Sends what changed on a map now.
     */
    void flush(ServerMapView view);

    /**
     * Draws the whole terrain of a map.
     */
    CompletableFuture<Void> renderTerrain(ServerMapView view);

    /**
     * {@return the network id of a decoration type, or {@code -1} if the client does not know it}
     */
    int decorationNetworkId(Key type);

    /**
     * Hangs a map in a frame only one player sees, see {@link MapView#hang}.
     */
    MapFrame hang(ServerMapView view, Player viewer, BlockPosition block, BlockFace facing, boolean invisible);

    /**
     * {@return the frames a map hangs in}
     */
    Collection<MapFrame> frames(ServerMapView view);
}
