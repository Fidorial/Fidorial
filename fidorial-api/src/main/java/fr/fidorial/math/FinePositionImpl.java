package fr.fidorial.math;

import fr.fidorial.world.World;

record FinePositionImpl(double x, double y, double z) implements FinePosition {
    @Override
    public Location toLocation(final World world) {
        return Location.of(world, x, y, z);
    }
}
