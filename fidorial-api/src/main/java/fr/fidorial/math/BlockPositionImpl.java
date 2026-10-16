package fr.fidorial.math;

import fr.fidorial.world.BlockFace;
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
    public FinePosition withX(final double x) {
        return with(x, blockY, blockZ);
    }

    @Override
    public FinePosition withY(final double y) {
        return with(blockX, y, blockZ);
    }

    @Override
    public FinePosition withZ(final double z) {
        return with(blockX, blockY, z);
    }

    @Override
    public FinePosition with(final double x, final double y, final double z) {
        return new FinePositionImpl(x, y, z);
    }

    @Override
    public Location toLocation(final World world) {
        return Location.of(world, blockX, blockY, blockZ);
    }

    @Override
    public BlockPosition relative(final BlockFace face, final int distance) {
        return offset(distance * face.dx(), distance * face.dy(), distance * face.dz());
    }

    @Override
    public BlockPosition relative(final BlockFace face) {
        return relative(face, 1);
    }

    @Override
    public BlockPosition offsetX(final int x) {
        return withX(blockX + x);
    }

    @Override
    public BlockPosition offsetY(final int y) {
        return withY(blockY + y);
    }

    @Override
    public BlockPosition offsetZ(final int z) {
        return withZ(blockZ + z);
    }

    @Override
    public BlockPosition offset(final int x, final int y, final int z) {
        return with(blockX + x, blockY + y, blockZ + z);
    }

    @Override
    public BlockPosition withX(final int x) {
        return with(x, blockY, blockZ);
    }

    @Override
    public BlockPosition withY(final int y) {
        return with(blockX, y, blockZ);
    }

    @Override
    public BlockPosition withZ(final int z) {
        return with(blockX, blockY, z);
    }

    @Override
    public BlockPosition with(final int x, final int y, final int z) {
        return new BlockPositionImpl(x, y, z);
    }
}
