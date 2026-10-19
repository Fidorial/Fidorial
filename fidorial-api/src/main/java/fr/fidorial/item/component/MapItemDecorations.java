package fr.fidorial.item.component;

import fr.fidorial.map.MapMarkers;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.MapDecorationType;

import java.util.Map;

/**
 * Icons a {@code filled_map} item adds to its map wherever it is carried, under
 * {@code minecraft:map_decorations}: the target of an explorer map, for instance.
 *
 * <p>Unlike {@linkplain MapMarkers markers}, they belong to the item: two copies of
 * the same map can show different targets. The client never sees this component, only the icons the
 * server draws from it.</p>
 *
 * @param entries the icons, by id
 * @since 0.1.0
 */
public record MapItemDecorations(Map<String, Entry> entries) {

    /**
     * One icon, placed in the world.
     *
     * @param type     what icon to draw
     * @param x        the X coordinate it marks
     * @param z        the Z coordinate it marks
     * @param rotation how far it is turned, in degrees clockwise
     * @since 0.1.0
     */
    public record Entry(TypedKey<MapDecorationType> type, double x, double z, float rotation) {
    }
}
