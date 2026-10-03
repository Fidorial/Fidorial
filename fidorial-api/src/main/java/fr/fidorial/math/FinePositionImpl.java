package fr.fidorial.math;

import fr.fidorial.world.BlockFace;
import fr.fidorial.world.World;

record FinePositionImpl(double x, double y, double z) implements FinePosition {
    @Override
    public Location toLocation(final World world) {
        return Location.of(world, x, y, z);
    }

    @Override
    public FinePosition withX(final double x) {
        return with(x, y, z);
    }

    @Override
    public FinePosition withY(final double y) {
        return with(x, y, z);
    }

    @Override
    public FinePosition withZ(final double z) {
        return with(x, y, z);
    }

    @Override
    public FinePosition with(final double x, final double y, final double z) {
        return new FinePositionImpl(x, y, z);
    }

    @Override
    public FinePosition offsetX(final double x) {
        return withX(x() + x);
    }

    @Override
    public FinePosition offsetY(final double y) {
        return withY(y() + y);
    }

    @Override
    public FinePosition offsetZ(final double z) {
        return withZ(z() + z);
    }

    @Override
    public FinePosition offset(final double x, final double y, final double z) {
        return with(x() + x, y() + y, z() + z);
    }

    @Override
    public FinePosition relative(final BlockFace face, final int distance) {
        return offset(distance * face.dx(), distance * face.dy(), distance * face.dz());
    }

    @Override
    public FinePosition relative(final BlockFace face) {
        return relative(face, 1);
    }

    @Override
    public FinePosition offsetX(final int x) {
        return offsetX((double) x);
    }

    @Override
    public FinePosition offsetY(final int y) {
        return offsetY((double) y);
    }

    @Override
    public FinePosition offsetZ(final int z) {
        return offsetZ((double) z);
    }

    @Override
    public FinePosition offset(final int x, final int y, final int z) {
        return offset((double) x, y, z);
    }

    @Override
    public FinePosition withX(final int x) {
        return with(x, y, z);
    }

    @Override
    public FinePosition withY(final int y) {
        return with(x, y, z);
    }

    @Override
    public FinePosition withZ(final int z) {
        return with(x, y, z);
    }

    @Override
    public FinePosition with(final int x, final int y, final int z) {
        return new FinePositionImpl(x, y, z);
    }
}
