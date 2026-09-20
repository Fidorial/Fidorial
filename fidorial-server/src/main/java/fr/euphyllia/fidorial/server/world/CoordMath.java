package fr.euphyllia.fidorial.server.world;

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

        return Location.of(
                origin.world(),
                origin.x() + x,
                origin.y() + y,
                origin.z() + z,
                origin.yaw(),
                origin.pitch()
        );
    }
}
