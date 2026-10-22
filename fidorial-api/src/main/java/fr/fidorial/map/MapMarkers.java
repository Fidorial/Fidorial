package fr.fidorial.map;

import net.kyori.adventure.key.Key;

import java.util.Map;
import java.util.Optional;

/**
 * The icons a {@link MapView} keeps and saves, each under a key of your choosing.
 *
 * <p>Use your own namespace, {@code myplugin:shop} for instance, so that plugins never replace each
 * other's markers. Markers are shown to everyone who sees the map. Banners players mark with a map
 * are stored here too, under {@code fidorial:banner/<x>/<y>/<z>}; they disappear on their own when
 * the banner is broken. Icons that change often or depend on who looks are better drawn by a
 * {@link MapLayer}.</p>
 *
 * @since 0.1.0
 */
public interface MapMarkers {

    /**
     * Adds a marker, or replaces the one with the same key.
     *
     * @param key        the key
     * @param decoration the icon
     * @since 0.1.0
     */
    void put(Key key, MapDecoration decoration);

    /**
     * Returns a marker.
     *
     * @param key the key
     * @return the icon, or empty if there is no marker with this key
     * @since 0.1.0
     */
    Optional<MapDecoration> get(Key key);

    /**
     * Removes a marker.
     *
     * @param key the key
     * @return whether there was a marker with this key
     * @since 0.1.0
     */
    boolean remove(Key key);

    /**
     * {@return a snapshot of every marker, by key}
     *
     * @since 0.1.0
     */
    Map<Key, MapDecoration> all();

    /**
     * Removes every marker.
     *
     * @since 0.1.0
     */
    void clear();
}
