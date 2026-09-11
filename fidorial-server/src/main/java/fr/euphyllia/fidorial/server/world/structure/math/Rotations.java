package fr.euphyllia.fidorial.server.world.structure.math;

import fr.fidorial.world.structure.StructureRotation;

public final class Rotations {

    private Rotations() {
    }

    public static int x(final StructureRotation rotation, final int x, final int z) {
        return switch (rotation) {
            case NONE -> x;
            case CLOCKWISE_90 -> -z;
            case CLOCKWISE_180 -> -x;
            case COUNTERCLOCKWISE_90 -> z;
        };
    }

    public static int z(final StructureRotation rotation, final int x, final int z) {
        return switch (rotation) {
            case NONE -> z;
            case CLOCKWISE_90 -> x;
            case CLOCKWISE_180 -> -z;
            case COUNTERCLOCKWISE_90 -> -x;
        };
    }

    public static Box box(final int originX, final int originY, final int originZ,
                          final int sizeX, final int sizeY, final int sizeZ, final StructureRotation rotation) {
        final int ex = Math.max(0, sizeX - 1);
        final int ey = Math.max(0, sizeY - 1);
        final int ez = Math.max(0, sizeZ - 1);
        return Box.of(originX, originY, originZ,
                originX + x(rotation, ex, ez), originY + ey, originZ + z(rotation, ex, ez));
    }

    public static StructureRotation[] shuffled(final LegacyRandom random) {
        final StructureRotation[] rotations = StructureRotation.values().clone();
        for (int i = rotations.length; i > 1; i--) {
            final int j = random.nextInt(i);
            final StructureRotation tmp = rotations[i - 1];
            rotations[i - 1] = rotations[j];
            rotations[j] = tmp;
        }
        return rotations;
    }
}
