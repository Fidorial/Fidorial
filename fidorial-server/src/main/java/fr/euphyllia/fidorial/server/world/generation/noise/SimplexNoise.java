package fr.euphyllia.fidorial.server.world.generation.noise;

import fr.euphyllia.fidorial.server.world.generation.random.BitRandom;

/**
 * Two-dimensional simplex noise: the plane is cut in triangles and each of the three corners around a point adds
 * a contribution that fades with the distance. Values stay within {@code [-1, 1]}.
 */
public final class SimplexNoise {

    private static final double SKEW = 0.5 * (Math.sqrt(3.0) - 1.0);
    private static final double UNSKEW = (3.0 - Math.sqrt(3.0)) / 6.0;
    private static final double CORNER_REACH = 0.5;
    private static final double OUTPUT_SCALE = 70.0;
    // only the twelve distinct directions of the lattice are used here
    private static final int DIRECTIONS = 12;
    private static final int CELL_MASK = 255;

    private final NoiseLattice lattice;

    /**
     * @param random      the stream the lattice is drawn from
     * @param originRange how far the lattice may be shifted on each axis, see {@link NoiseLattice}
     */
    public SimplexNoise(final BitRandom random, final double originRange) {
        this.lattice = new NoiseLattice(random, originRange);
    }

    private static double corner(final int direction, final double x, final double y) {
        final double falloff = CORNER_REACH - x * x - y * y;
        if (falloff < 0.0) {
            return 0.0;
        }
        final double squared = falloff * falloff;
        return squared * squared * (NoiseLattice.gradientX(direction) * x + NoiseLattice.gradientY(direction) * y);
    }

    /**
     * {@return the noise at a point of the plane}
     */
    public float sample(final double x, final double y) {
        final double px = x + lattice.originX();
        final double py = y + lattice.originY();

        // skew the plane so the triangles become halves of unit squares, and find the square holding the point
        final double skew = (px + py) * SKEW;
        final int cellX = (int) Math.floor(px + skew);
        final int cellY = (int) Math.floor(py + skew);
        final double unskew = (cellX + cellY) * UNSKEW;
        final double x0 = px - (cellX - unskew);
        final double y0 = py - (cellY - unskew);

        // the diagonal of the square tells which of its two triangles the point is in
        final int stepX;
        final int stepY;
        if (x0 > y0) {
            stepX = 1;
            stepY = 0;
        } else {
            stepX = 0;
            stepY = 1;
        }

        final double x1 = x0 - stepX + UNSKEW;
        final double y1 = y0 - stepY + UNSKEW;
        final double x2 = x0 - 1.0 + 2.0 * UNSKEW;
        final double y2 = y0 - 1.0 + 2.0 * UNSKEW;

        final int hashX = cellX & CELL_MASK;
        final int hashY = cellY & CELL_MASK;
        final int first = lattice.hash(hashX + lattice.hash(hashY)) % DIRECTIONS;
        final int second = lattice.hash(hashX + stepX + lattice.hash(hashY + stepY)) % DIRECTIONS;
        final int third = lattice.hash(hashX + 1 + lattice.hash(hashY + 1)) % DIRECTIONS;

        return (float) (OUTPUT_SCALE * (corner(first, x0, y0) + corner(second, x1, y1) + corner(third, x2, y2)));
    }
}
