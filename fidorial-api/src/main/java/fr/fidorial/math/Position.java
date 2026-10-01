package fr.fidorial.math;

import fr.fidorial.world.ChunkPos;
import fr.fidorial.world.World;
import org.jetbrains.annotations.Contract;

public sealed interface Position permits BlockPosition, FinePosition {
    @Contract(value = "_, _, _ -> new", pure = true)
    static FinePosition fine(final double x, final double y, final double z) {
        return new FinePositionImpl(x, y, z);
    }

    @Contract(value = "_, _, _ -> new", pure = true)
    static BlockPosition block(final int x, final int y, final int z) {
        return new BlockPositionImpl(x, y, z);
    }

    @Contract(pure = true)
    double x();

    @Contract(pure = true)
    double y();

    @Contract(pure = true)
    double z();

    @Contract(value = "_ -> new", pure = true)
    FinePosition offsetX(double x);

    @Contract(value = "_ -> new", pure = true)
    FinePosition offsetY(double y);

    @Contract(value = "_ -> new", pure = true)
    FinePosition offsetZ(double z);

    @Contract(value = "_, _, _ -> new", pure = true)
    FinePosition offset(double x, double y, double z);

    @Contract(value = "_ -> new", pure = true)
    Position offsetX(int x);

    @Contract(value = "_ -> new", pure = true)
    Position offsetY(int y);

    @Contract(value = "_ -> new", pure = true)
    Position offsetZ(int z);

    @Contract(value = "_, _, _ -> new", pure = true)
    Position offset(int x, int y, int z);

    @Contract(value = "_ -> new", pure = true)
    FinePosition withX(double x);

    @Contract(value = "_ -> new", pure = true)
    FinePosition withY(double y);

    @Contract(value = "_ -> new", pure = true)
    FinePosition withZ(double z);

    @Contract(value = "_, _, _ -> new", pure = true)
    FinePosition with(double x, double y, double z);

    @Contract(value = "_ -> new", pure = true)
    Position withX(int x);

    @Contract(value = "_ -> new", pure = true)
    Position withY(int y);

    @Contract(value = "_ -> new", pure = true)
    Position withZ(int z);

    @Contract(value = "_, _, _ -> new", pure = true)
    Position with(int x, int y, int z);

    @Contract(pure = true)
    default int blockX() {
        return (int) Math.floor(x());
    }

    @Contract(pure = true)
    default int blockY() {
        return (int) Math.floor(y());
    }

    @Contract(pure = true)
    default int blockZ() {
        return (int) Math.floor(z());
    }

    // todo: get rid of this or replace with a real chunk idk yet
    @Contract(value = " -> new", pure = true)
    default ChunkPos chunk() {
        return ChunkPos.fromBlock(blockX(), blockZ());
    }

    @Contract(pure = true)
    default int chunkX() {
        return blockX() >> 4;
    }

    @Contract(pure = true)
    default int chunkZ() {
        return blockZ() >> 4;
    }

    @Contract(pure = true)
    default double distanceSquared(final Position other) {
        final double dx = x() - other.x();
        final double dy = y() - other.y();
        final double dz = z() - other.z();
        return dx * dx + dy * dy + dz * dz;
    }

    @Contract(value = "_ -> new", pure = true)
    Location toLocation(World world);
}
