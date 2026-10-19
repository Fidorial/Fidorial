package fr.euphyllia.fidorial.server.map;

import net.kyori.adventure.key.Key;

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
}
