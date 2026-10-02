package fr.fidorial.math;

import fr.fidorial.world.BlockFace;
import fr.fidorial.world.World;

record LocationImpl(World world, double x, double y, double z, float yaw, float pitch) implements Location {
    @Override
    public Location relative(final BlockFace face) {
        return offset(face.dx(), face.dy(), face.dz());
    }

    @Override
    public Location offsetX(final double x) {
        return withX(x() + x);
    }

    @Override
    public Location offsetY(final double y) {
        return withY(y() + y);
    }

    @Override
    public Location offsetZ(final double z) {
        return withZ(z() + z);
    }

    @Override
    public Location offset(final double x, final double y, final double z) {
        return with(x() + x, y() + y, z() + z);
    }

    @Override
    public Location offsetX(final int x) {
        return offsetX((double) x);
    }

    @Override
    public Location offsetY(final int y) {
        return offsetY((double) y);
    }

    @Override
    public Location offsetZ(final int z) {
        return offsetZ((double) z);
    }

    @Override
    public Location offset(final int x, final int y, final int z) {
        return offset((double) x, y, z);
    }

    @Override
    public Location withX(final double x) {
        return with(x, y, z);
    }

    @Override
    public Location withY(final double y) {
        return with(x, y, z);
    }

    @Override
    public Location withZ(final double z) {
        return with(x, y, z);
    }

    @Override
    public Location with(final double x, final double y, final double z) {
        return with(x, y, z, yaw, pitch);
    }

    @Override
    public Location with(final double x, final double y, final double z, final float yaw, final float pitch) {
        return new LocationImpl(world, x, y, z, yaw, pitch);
    }

    @Override
    public Location withX(final int x) {
        return with(x, y, z);
    }

    @Override
    public Location withY(final int y) {
        return with(x, y, z);
    }

    @Override
    public Location withZ(final int z) {
        return with(x, y, z);
    }

    @Override
    public Location with(final int x, final int y, final int z) {
        return with((double) x, y, z);
    }

    @Override
    public Rotation rotation() {
        return Rotation.rotation(yaw, pitch);
    }

    @Override
    public Location withRotation(final Rotation rotation) {
        return withRotation(rotation.yaw(), rotation.pitch());
    }

    @Override
    public Location withRotation(final float yaw, final float pitch) {
        return new LocationImpl(world, x, y, z, yaw, pitch);
    }

    @Override
    public Location toLocation(final World world) {
        return new LocationImpl(world, x, y, z, yaw, pitch);
    }
}
