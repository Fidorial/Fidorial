package fr.fidorial.world;

/**
 * A precise position and orientation, independent of any world.
 *
 * @param x     the X coordinate
 * @param y     the Y coordinate, at the feet for an entity
 * @param z     the Z coordinate
 * @param yaw   the horizontal rotation in degrees, {@code 0} facing south and growing clockwise
 * @param pitch the vertical rotation in degrees, {@code -90} looking straight up and {@code 90} straight down
 * @since 0.1.0
 */
public record Location(double x, double y, double z, float yaw, float pitch) {

    /**
     * {@return a location at the given coordinates, facing south}
     *
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param z the Z coordinate
     * @since 0.1.0
     */
    public static Location of(final double x, final double y, final double z) {
        return new Location(x, y, z, 0f, 0f);
    }

    /**
     * {@return the position of the chunk holding this location}
     *
     * @since 0.1.0
     */
    public ChunkPos chunk() {
        return ChunkPos.fromBlock((int) Math.floor(x), (int) Math.floor(z));
    }

    /**
     * {@return the position of the block holding this location}
     *
     * @since 0.1.0
     */
    public BlockPos blockPos() {
        return new BlockPos((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
    }

    /**
     * {@return this location moved to other coordinates, keeping its orientation}
     *
     * @param x the new X coordinate
     * @param y the new Y coordinate
     * @param z the new Z coordinate
     * @since 0.1.0
     */
    public Location withPosition(final double x, final double y, final double z) {
        return new Location(x, y, z, yaw, pitch);
    }

    /**
     * {@return this location with another orientation, keeping its coordinates}
     *
     * @param yaw   the new horizontal rotation in degrees
     * @param pitch the new vertical rotation in degrees
     * @since 0.1.0
     */
    public Location withRotation(final float yaw, final float pitch) {
        return new Location(x, y, z, yaw, pitch);
    }

    /**
     * Computes the squared distance to another location, ignoring orientation.
     *
     * <p>Cheaper than {@link #distance(Location)}; prefer it to compare distances.</p>
     *
     * @param other the other location
     * @return the squared distance in blocks
     * @since 0.1.0
     */
    public double distanceSquared(final Location other) {
        final double dx = x - other.x;
        final double dy = y - other.y;
        final double dz = z - other.z;
        return dx * dx + dy * dy + dz * dz;
    }

    /**
     * Computes the distance to another location, ignoring orientation.
     *
     * @param other the other location
     * @return the distance in blocks
     * @since 0.1.0
     */
    public double distance(final Location other) {
        return Math.sqrt(distanceSquared(other));
    }
}
