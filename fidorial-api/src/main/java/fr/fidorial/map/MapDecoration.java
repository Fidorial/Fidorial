package fr.fidorial.map;

import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.MapDecorationType;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

/**
 * An icon drawn over a map: a player arrow, a banner flag, a treasure cross...
 *
 * <p>Positions are in canvas pixels, like {@link MapCanvas}: {@code (0, 0)} is the top left corner
 * and {@code (64, 64)} the center. The client places icons on a grid of half pixels and clamps them
 * to the map. Use {@link MapView#pixelX(double)} and {@link MapView#pixelY(double)} to place an icon
 * at world coordinates.</p>
 *
 * @param type      what icon to draw, see {@link fr.fidorial.registry.keys.MapDecorationTypeKeys}
 * @param x         the column of the center of the icon, {@code 0}-{@code 128}
 * @param y         the row of the center of the icon, {@code 0}-{@code 128}
 * @param direction how far the icon is turned, in sixteenths of a turn clockwise, {@code 0}-{@code 15}:
 *                  {@code 0} upright, a player arrow pointing up (north on a terrain map), {@code 4}
 *                  pointing right (east), {@code 8} down; see {@link MapManager#directionOf(float)}
 * @param label     a name written under the icon, or {@code null}
 * @since 0.1.0
 */
public record MapDecoration(TypedKey<MapDecorationType> type, double x, double y, int direction,
                            @Nullable Component label) {
}
