package fr.fidorial.world.structure;

import org.jspecify.annotations.Nullable;

import java.util.Locale;

/**
 * A quarter-turn rotation around the vertical axis, as used by structure templates and jigsaw pieces.
 *
 * @since 0.1.0
 */
public enum StructureRotation {
    NONE,
    CLOCKWISE_90,
    CLOCKWISE_180,
    COUNTERCLOCKWISE_90;

    private static final StructureRotation[] VALUES = values();

    /**
     * @return the number of clockwise quarter turns, from 0 to 3
     */
    public int quarterTurns() {
        return switch (this) {
            case NONE -> 0;
            case CLOCKWISE_90 -> 1;
            case CLOCKWISE_180 -> 2;
            case COUNTERCLOCKWISE_90 -> 3;
        };
    }

    /**
     * @param quarterTurns clockwise quarter turns, any integer
     * @return the matching rotation
     */
    public static StructureRotation ofQuarterTurns(final int quarterTurns) {
        return switch (Math.floorMod(quarterTurns, 4)) {
            case 1 -> CLOCKWISE_90;
            case 2 -> CLOCKWISE_180;
            case 3 -> COUNTERCLOCKWISE_90;
            default -> NONE;
        };
    }

    /**
     * Applies {@code other} after this rotation.
     *
     * @param other the rotation to add
     * @return the combined rotation
     */
    public StructureRotation then(final StructureRotation other) {
        return ofQuarterTurns(quarterTurns() + other.quarterTurns());
    }

    /**
     * Rotates a value expressed on a circle of {@code steps} positions, such as the 16 sign rotations.
     *
     * @param value the current value
     * @param steps the number of positions on the circle
     * @return the rotated value
     */
    public int rotate(final int value, final int steps) {
        return Math.floorMod(value + quarterTurns() * steps / 4, steps);
    }

    /**
     * @return the lowercase name used by commands, e.g. {@code clockwise_90}
     */
    public String serializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static @Nullable StructureRotation byName(final String name) {
        for (final StructureRotation rotation : VALUES) {
            if (rotation.serializedName().equalsIgnoreCase(name) || rotation.name().equalsIgnoreCase(name)) {
                return rotation;
            }
        }
        return null;
    }
}
