package fr.fidorial.math;

import com.google.common.base.Preconditions;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.World;
import org.jetbrains.annotations.Contract;

/**
 * A fine position in a given world, along with a rotation.
 *
 * @since 0.1.0
 */
public sealed interface Location extends FinePosition permits LocationImpl {
    /**
     * Creates a location at the given coordinates, facing a yaw and pitch of {@code 0}.
     *
     * @param world the world of the location
     * @param x     the X coordinate
     * @param y     the Y coordinate
     * @param z     the Z coordinate
     * @return a new location at the given coordinates, with a yaw and pitch of {@code 0}
     * @since 0.1.0
     */
    @Contract(value = "_, _, _, _ -> new", pure = true)
    static Location of(final World world, final double x, final double y, final double z) {
        return of(world, x, y, z, 0, 0);
    }

    /**
     * Creates a location at the given coordinates and rotation.
     *
     * @param world the world of the location
     * @param x     the X coordinate
     * @param y     the Y coordinate
     * @param z     the Z coordinate
     * @param yaw   the yaw, in degrees
     * @param pitch the pitch, in degrees
     * @return a new location at the given coordinates and rotation
     * @since 0.1.0
     */
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

    /**
     * Copies this location with different coordinates and rotation, in the same world.
     *
     * @param x     the new X coordinate
     * @param y     the new Y coordinate
     * @param z     the new Z coordinate
     * @param yaw   the new yaw, in degrees
     * @param pitch the new pitch, in degrees
     * @return a new location in the same world with all coordinates and the rotation replaced
     * @since 0.1.0
     */
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

    /**
     * Gets the rotation of this location.
     *
     * @return the yaw and pitch of this location
     * @since 0.1.0
     */
    @Contract(value = " -> new", pure = true)
    Rotation rotation();

    /**
     * Copies this location with a different rotation, at the same coordinates and in the same world.
     *
     * @param rotation the new rotation
     * @return a new location at the same coordinates with the rotation replaced
     * @since 0.1.0
     */
    @Contract(value = "_ -> new", pure = true)
    Location withRotation(Rotation rotation);

    /**
     * Copies this location with a different rotation, at the same coordinates and in the same world.
     *
     * @param yaw   the new yaw, in degrees
     * @param pitch the new pitch, in degrees
     * @return a new location at the same coordinates with the rotation replaced
     * @since 0.1.0
     */
    @Contract(value = "_, _ -> new", pure = true)
    Location withRotation(float yaw, float pitch);

    /**
     * Gets the world this location is in.
     *
     * @return the world of this location
     * @since 0.1.0
     */
    @Contract(pure = true)
    World world();

    /**
     * Gets the pitch of this location, which is its vertical rotation.
     *
     * @return the pitch of this location, in degrees
     * @since 0.1.0
     */
    @Contract(pure = true)
    float pitch();

    /**
     * Gets the yaw of this location, which is its horizontal rotation.
     *
     * @return the yaw of this location, in degrees
     * @since 0.1.0
     */
    @Contract(pure = true)
    float yaw();

    /**
     * Computes the squared distance between this location and another one in the same world.
     *
     * @param other the location to measure the distance to
     * @return the squared euclidean distance between this location and another
     * @throws IllegalArgumentException if the other location is in a different world
     * @see Position#distanceSquared(Position)
     * @since 0.1.0
     */
    @Contract(pure = true)
    default double distanceSquared(final Location other) {
        Preconditions.checkArgument(other.world().equals(this.world()), "Other locations must have the same world");
        final double dx = x() - other.x();
        final double dy = y() - other.y();
        final double dz = z() - other.z();
        return dx * dx + dy * dy + dz * dz;
    }
}
