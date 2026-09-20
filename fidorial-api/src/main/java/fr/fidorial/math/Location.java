package fr.fidorial.math;

import com.google.common.base.Preconditions;
import fr.fidorial.world.ChunkPos;
import fr.fidorial.world.World;
import org.jetbrains.annotations.Contract;

public sealed interface Location extends FinePosition permits LocationImpl {
    @Contract(value = "_, _, _, _ -> new", pure = true)
    static Location of(final World world, final double x, final double y, final double z) {
        return of(world, x, y, z, 0, 0);
    }

    @Contract(value = "_, _, _, _, _, _ -> new", pure = true)
    static Location of(final World world, final double x, final double y, final double z, final float yaw, final float pitch) {
        return new LocationImpl(world, x, y, z, yaw, pitch);
    }

    @Contract(value = " -> new", pure = true)
    Rotation rotation();

    World world();

    float pitch();

    float yaw();

    // todo: get rid of this or replace with a real chunk idk yet
    default ChunkPos chunk() {
        return ChunkPos.fromBlock(blockX(), blockZ());
    }

    default double distanceSquared(final Location other) {
        Preconditions.checkArgument(other.world().equals(this.world()), "Other locations must have the same world");
        final double dx = x() - other.x();
        final double dy = y() - other.y();
        final double dz = z() - other.z();
        return dx * dx + dy * dy + dz * dz;
    }
}
