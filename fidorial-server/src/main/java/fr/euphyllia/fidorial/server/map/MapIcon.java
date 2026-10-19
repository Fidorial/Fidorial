package fr.euphyllia.fidorial.server.map;

import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

/**
 * An icon as the client receives it.
 *
 * @param type     the network id of the {@code minecraft:map_decoration_type}
 * @param x        the column, in half pixels from the center, {@code -128}-{@code 127}
 * @param y        the row, in half pixels from the center, {@code -128}-{@code 127}
 * @param rotation the rotation on the wire, {@code 0}-{@code 15}, {@code 8} being upright
 * @param label    the label, or {@code null}
 */
public record MapIcon(int type, int x, int y, int rotation, @Nullable Component label) {
}
