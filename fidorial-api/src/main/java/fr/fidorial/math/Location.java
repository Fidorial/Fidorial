package fr.fidorial.math;

import com.google.common.base.Preconditions;
import fr.fidorial.world.BlockFace;
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

    @Override
    @Contract(value = "_ -> new", pure = true)
    Location relative(BlockFace face);

    @Override
    @Contract(value = "_ -> new", pure = true)
    Location offsetX(double x);

    @Override
    @Contract(value = "_ -> new", pure = true)
    Location offsetY(double y);

    @Override
    @Contract(value = "_ -> new", pure = true)
    Location offsetZ(double z);

    @Override
    @Contract(value = "_, _, _ -> new", pure = true)
    Location offset(double x, double y, double z);

    @Override
    @Contract(value = "_ -> new", pure = true)
    Location offsetX(int x);

    @Override
    @Contract(value = "_ -> new", pure = true)
    Location offsetY(int y);

    @Override
    @Contract(value = "_ -> new", pure = true)
    Location offsetZ(int z);

    @Override
    @Contract(value = "_, _, _ -> new", pure = true)
    Location offset(int x, int y, int z);

    @Override
    @Contract(value = "_ -> new", pure = true)
    Location withX(double x);

    @Override
    @Contract(value = "_ -> new", pure = true)
    Location withY(double y);

    @Override
    @Contract(value = "_ -> new", pure = true)
    Location withZ(double z);

    @Override
    @Contract(value = "_, _, _ -> new", pure = true)
    Location with(double x, double y, double z);

    @Contract(value = "_, _, _, _, _ -> new", pure = true)
    Location with(double x, double y, double z, float yaw, float pitch);

    @Override
    @Contract(value = "_ -> new", pure = true)
    Location withX(int x);

    @Override
    @Contract(value = "_ -> new", pure = true)
    Location withY(int y);

    @Override
    @Contract(value = "_ -> new", pure = true)
    Location withZ(int z);

    @Override
    @Contract(value = "_, _, _ -> new", pure = true)
    Location with(int x, int y, int z);

    @Contract(value = " -> new", pure = true)
    Rotation rotation();

    @Contract(value = "_ -> new", pure = true)
    Location withRotation(Rotation rotation);

    @Contract(value = "_, _ -> new", pure = true)
    Location withRotation(float yaw, float pitch);

    @Contract(pure = true)
    World world();

    @Contract(pure = true)
    float pitch();

    @Contract(pure = true)
    float yaw();

    @Contract(pure = true)
    default double distanceSquared(final Location other) {
        Preconditions.checkArgument(other.world().equals(this.world()), "Other locations must have the same world");
        final double dx = x() - other.x();
        final double dy = y() - other.y();
        final double dz = z() - other.z();
        return dx * dx + dy * dy + dz * dz;
    }
}
