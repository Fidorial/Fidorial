package fr.fidorial.math;

import fr.fidorial.world.World;

record LocationImpl(World world, double x, double y, double z, float yaw, float pitch) implements Location {
    @Override
    public Rotation rotation() {
        return Rotation.rotation(yaw, pitch);
    }

    @Override
    public Location toLocation(final World world) {
        return new LocationImpl(world, x, y, z, yaw, pitch);
    }
}
