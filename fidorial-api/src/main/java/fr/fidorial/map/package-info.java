/**
 * Maps: the 128×128 pictures drawn on {@code filled_map} items.
 *
 * <ul>
 *   <li>{@link MapManager}, from {@link Server#maps()}, creates and finds
 *   {@link MapView maps}, saved with the world in the vanilla format, and makes
 *   canvases, images, fonts and layers;</li>
 *   <li>a {@link MapCanvas} holds pixels; draw shapes, {@linkplain
 *   .MapImage images} and text ({@link MapFont}) on it;</li>
 *   <li>{@link MapLayer layers} paint over a map without changing what it stores,
 *   once for everyone or once per viewer;</li>
 *   <li>{@link MapDecoration icons} are kept as {@linkplain MapMarkers
 *   markers} or painted by layers;</li>
 *   <li>a pixel is a {@link MapColor} in a {@link MapShade}, see
 *   {@link MapPalette}.</li>
 * </ul>
 *
 * @since 0.1.0
 */
        package fr.fidorial.map;

import fr.fidorial.Server;
