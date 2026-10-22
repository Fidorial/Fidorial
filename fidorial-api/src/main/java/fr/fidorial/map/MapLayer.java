package fr.fidorial.map;

import fr.fidorial.entity.player.Player;

import java.util.Optional;

/**
 * Something a plugin paints over a {@link MapView}, without touching what the map stores.
 *
 * <p>A layer is repainted from scratch: before each {@link #paint(Context)} its canvas is cleared to
 * transparent and its icons are dropped. What it paints covers the {@linkplain MapView#canvas()
 * canvas} of the map and the layers added before it; transparent pixels let them show through.
 * Nothing a layer paints is saved.</p>
 *
 * <p>A {@linkplain Scope#SHARED shared} layer is painted once for everyone, a
 * {@linkplain Scope#PER_VIEWER per-viewer} layer once for each player who sees the map, so it can
 * show something different to each. Layers are painted off the region threads: keep them quick and
 * do not touch the world from them.</p>
 *
 * @see MapManager#layer(Scope, int, java.util.function.Consumer)
 * @since 0.1.0
 */
public interface MapLayer {

    /**
     * Paints the layer.
     *
     * @param context where to paint, and for whom
     * @since 0.1.0
     */
    void paint(Context context);

    /**
     * {@return for whom the layer is painted}
     *
     * @since 0.1.0
     */
    Scope scope();

    /**
     * {@return how many ticks apart repaints happen; {@code 0} only repaints on
     * {@link MapView#repaint()} and, for a per-viewer layer, when someone starts seeing the map}
     *
     * @since 0.1.0
     */
    int repaintInterval();

    /**
     * For whom a layer is painted.
     *
     * @since 0.1.0
     */
    enum Scope {

        /**
         * Once, for everyone who sees the map.
         */
        SHARED,

        /**
         * Once for each player who sees the map.
         */
        PER_VIEWER
    }

    /**
     * What a layer paints on.
     *
     * @since 0.1.0
     */
    interface Context {

        /**
         * {@return the map being painted}
         *
         * @since 0.1.0
         */
        MapView view();

        /**
         * {@return the player the layer is painted for; empty for a shared layer}
         *
         * @since 0.1.0
         */
        Optional<Player> viewer();

        /**
         * {@return the canvas of the layer, cleared before each paint}
         *
         * @since 0.1.0
         */
        MapCanvas canvas();

        /**
         * Adds an icon until the next paint of this layer.
         *
         * @param decoration the icon
         * @since 0.1.0
         */
        void decorate(MapDecoration decoration);

        /**
         * {@return the current tick of the map ticker, to animate}
         *
         * @since 0.1.0
         */
        long tick();
    }
}
