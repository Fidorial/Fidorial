package fr.euphyllia.fidorial.server.map;

import fr.fidorial.map.MapScale;

/**
 * Grid and angle conversions shared by the map code.
 */
final class MapMath {

    private static final MapScale[] SCALES = MapScale.values();

    private MapMath() {
    }

    /**
     * {@return the center of the map of a scale covering a coordinate, as vanilla aligns new maps}
     */
    static int alignedCenter(final int coordinate, final MapScale scale) {
        final int size = scale.blocks();
        return Math.floorDiv(coordinate + 64, size) * size + size / 2 - 64;
    }

    /**
     * {@return the next scale out, or the same one if it is already the widest}
     */
    static MapScale zoomedOut(final MapScale scale) {
        return SCALES[Math.min(scale.ordinal() + 1, SCALES.length - 1)];
    }

    /**
     * {@return the scale of a zoom level, clamped to {@code 0}-{@code 4}}
     */
    static MapScale scale(final int level) {
        return SCALES[Math.clamp(level, 0, SCALES.length - 1)];
    }

    /**
     * {@return the direction of an icon pointing like a yaw, {@code 0} being up (north)}
     *
     * <p>Vanilla rounds a yaw of {@code 0} (south) to {@code 0} with a bias of 8 degrees; south is
     * {@code 8} here.</p>
     */
    static int directionOf(final float yaw) {
        final float wrapped = yaw % 360.0f;
        final double biased = wrapped < 0 ? wrapped - 8.0 : wrapped + 8.0;
        return Math.floorMod((int) (biased * 16.0 / 360.0) + 8, 16);
    }
}
