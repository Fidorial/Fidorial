package fr.fidorial.math;

import fr.fidorial.world.World;

record BlockPositionImpl(int blockX, int blockY, int blockZ) implements BlockPosition {
    @Override
    public double x() {
        return blockX;
    }

    @Override
    public double y() {
        return blockY;
    }

    @Override
    public double z() {
        return blockZ;
    }

    @Override
    public Location toLocation(final World world) {
        return Location.of(world, blockX, blockY, blockZ);
    }
}
