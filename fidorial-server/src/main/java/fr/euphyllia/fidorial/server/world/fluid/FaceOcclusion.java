package fr.euphyllia.fidorial.server.world.fluid;

import fr.euphyllia.fidorial.server.world.block.CollisionShape;
import fr.fidorial.world.BlockFace;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * Whether two blocks side by side close the face between them, as fluids see it.
 */
final class FaceOcclusion {

    private static final double EPSILON = 1.0E-7;

    private FaceOcclusion() {
        throw new UnsupportedOperationException("FaceOcclusion cannot be instantiated.");
    }

    /**
     * {@return {@code true} if the parts of two shapes touching the face between them cover it
     * whole}
     *
     * @param shape     the shape of the block fluid leaves
     * @param occluder  the shape of the block beside it
     * @param direction the side of the first block the second one is on
     */
    static boolean sealsFace(final CollisionShape shape, final CollisionShape occluder, final BlockFace direction) {
        final boolean positive = direction.dx() + direction.dy() + direction.dz() > 0;
        final int axis = direction.dx() != 0 ? 0 : direction.dy() != 0 ? 1 : 2;
        // the block on the negative side shows its maximum face, the other one its minimum face
        final CollisionShape low = positive ? shape : occluder;
        final CollisionShape high = positive ? occluder : shape;
        final List<double[]> covered = new ArrayList<>();
        for (int box = 0; box < low.boxCount(); box++) {
            if (Math.abs(max(low, box, axis) - 1.0) < EPSILON) {
                covered.add(projection(low, box, axis));
            }
        }
        for (int box = 0; box < high.boxCount(); box++) {
            if (Math.abs(min(high, box, axis)) < EPSILON) {
                covered.add(projection(high, box, axis));
            }
        }
        return coversFace(covered);
    }

    /**
     * {@return {@code true} if rectangles cover the unit square}
     */
    private static boolean coversFace(final List<double[]> rectangles) {
        if (rectangles.isEmpty()) {
            return false;
        }
        final TreeSet<Double> us = new TreeSet<>(List.of(0.0, 1.0));
        final TreeSet<Double> vs = new TreeSet<>(List.of(0.0, 1.0));
        for (final double[] rectangle : rectangles) {
            us.add(clamp(rectangle[0]));
            us.add(clamp(rectangle[2]));
            vs.add(clamp(rectangle[1]));
            vs.add(clamp(rectangle[3]));
        }
        final Double[] u = us.toArray(Double[]::new);
        final Double[] v = vs.toArray(Double[]::new);
        for (int i = 0; i + 1 < u.length; i++) {
            if (u[i + 1] - u[i] < EPSILON) {
                continue;
            }
            final double centerU = (u[i] + u[i + 1]) / 2.0;
            for (int j = 0; j + 1 < v.length; j++) {
                if (v[j + 1] - v[j] < EPSILON) {
                    continue;
                }
                final double centerV = (v[j] + v[j + 1]) / 2.0;
                if (!covers(rectangles, centerU, centerV)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean covers(final List<double[]> rectangles, final double u, final double v) {
        for (final double[] rectangle : rectangles) {
            if (u > rectangle[0] && u < rectangle[2] && v > rectangle[1] && v < rectangle[3]) {
                return true;
            }
        }
        return false;
    }

    private static double clamp(final double value) {
        return Math.clamp(value, 0.0, 1.0);
    }

    /**
     * {@return a box seen along an axis: its minimum then maximum on the two other axes}
     */
    private static double[] projection(final CollisionShape shape, final int box, final int axis) {
        return switch (axis) {
            case 0 -> new double[] {shape.minY(box), shape.minZ(box), shape.maxY(box), shape.maxZ(box)};
            case 1 -> new double[] {shape.minX(box), shape.minZ(box), shape.maxX(box), shape.maxZ(box)};
            default -> new double[] {shape.minX(box), shape.minY(box), shape.maxX(box), shape.maxY(box)};
        };
    }

    private static double min(final CollisionShape shape, final int box, final int axis) {
        return switch (axis) {
            case 0 -> shape.minX(box);
            case 1 -> shape.minY(box);
            default -> shape.minZ(box);
        };
    }

    private static double max(final CollisionShape shape, final int box, final int axis) {
        return switch (axis) {
            case 0 -> shape.maxX(box);
            case 1 -> shape.maxY(box);
            default -> shape.maxZ(box);
        };
    }
}
