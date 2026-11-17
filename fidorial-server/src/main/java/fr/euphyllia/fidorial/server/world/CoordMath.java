package fr.euphyllia.fidorial.server.world;

import fr.fidorial.annotation.SinceMinecraft;
import fr.fidorial.math.Location;

public final class CoordMath {

    private CoordMath() {
    }

    public static Location applyLocalCoords(final Location origin, final double left, final double up, final double forwards) {
        final double yaw = Math.toRadians(origin.yaw());
        final double pitch = Math.toRadians(origin.pitch());

        final double yCos = Math.cos(yaw + Math.PI / 2);
        final double ySin = Math.sin(yaw + Math.PI / 2);

        final double xCos = Math.cos(-pitch);
        final double xSin = Math.sin(-pitch);

        final double xCosUp = Math.cos(-pitch + Math.PI / 2);
        final double xSinUp = Math.sin(-pitch + Math.PI / 2);

        // forward vector
        final double fx = yCos * xCos;
        final double fy = xSin;
        final double fz = ySin * xCos;

        // up vector
        final double ux = yCos * xCosUp;
        final double uy = xSinUp;
        final double uz = ySin * xCosUp;

        // left vector
        final double lx = -(fy * uz - fz * uy);
        final double ly = -(fz * ux - fx * uz);
        final double lz = -(fx * uy - fy * ux);

        final double x = lx * left + ux * up + fx * forwards;
        final double y = ly * left + uy * up + fy * forwards;
        final double z = lz * left + uz * up + fz * forwards;

        return origin.offset(x, y, z);
    }

    /**
     * Applies local coordinates on the horizontal plane only ({@code ^left ^forwards}): only the yaw
     * of the origin is taken into account and the height is left unchanged. Used by the
     * two-dimensional local coordinates accepted.
     *
     * @param origin   the position and rotation the coordinates are relative to
     * @param left     the distance to the left
     * @param forwards the distance forwards
     * @return the resulting location
     */
    @SinceMinecraft("26.4-snapshot-9")
    public static Location applyLocalHorizontalCoords(final Location origin, final double left, final double forwards) {
        final double yaw = Math.toRadians(origin.yaw());

        // forward vector, flattened on the horizontal plane
        final double fx = Math.cos(yaw + Math.PI / 2);
        final double fz = Math.sin(yaw + Math.PI / 2);

        // left vector: -(forwards x up), with up = (0, 1, 0)
        final double lx = fz;
        final double lz = -fx;

        return origin.offset(fx * forwards + lx * left, 0, fz * forwards + lz * left);
    }
}
