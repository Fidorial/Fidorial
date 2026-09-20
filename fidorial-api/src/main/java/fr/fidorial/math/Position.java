package fr.fidorial.math;

import fr.fidorial.world.World;
import org.jetbrains.annotations.Contract;

public sealed interface Position permits BlockPosition, FinePosition {
    @Contract(value = "_, _, _ -> new", pure = true)
    static FinePosition of(final double x, final double y, final double z) {
        return new FinePositionImpl(x, y, z);
    }

    @Contract(value = "_, _, _ -> new", pure = true)
    static BlockPosition of(final int x, final int y, final int z) {
        return new BlockPositionImpl(x, y, z);
    }

    double x();

    double y();

    double z();

    default int blockX() {
        return (int) Math.floor(x());
    }

    default int blockY() {
        return (int) Math.floor(y());
    }

    default int blockZ() {
        return (int) Math.floor(z());
    }

    default int chunkX() {
        return blockX() >> 4;
    }

    default int chunkZ() {
        return blockZ() >> 4;
    }

    default double distanceSquared(final Position other) {
        final double dx = x() - other.x();
        final double dy = y() - other.y();
        final double dz = z() - other.z();
        return dx * dx + dy * dy + dz * dz;
    }

    @Contract(value = "_ -> new", pure = true)
    Location toLocation(World world);
}
