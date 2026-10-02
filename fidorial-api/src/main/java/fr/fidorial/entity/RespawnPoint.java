package fr.fidorial.entity;

import com.google.common.base.Preconditions;
import fr.fidorial.world.Location;
import fr.fidorial.world.World;


/**
 * The place a player comes back to after dying.
 *
 * <p>A point is bound to a {@link World}: when that world is no longer loaded at respawn time the
 * server silently falls back to the configured world spawn and clears the point.</p>
 *
 * @param world    the world to respawn in
 * @param location the position and orientation to respawn at
 * @see Player#setRespawnPoint(RespawnPoint)
 * @since 0.1.0
 */
public record RespawnPoint(World world, Location location) {

    /**
     * Validates the components.
     */
    public RespawnPoint {
        Preconditions.checkArgument(world != null, "The world of a respawn point must not be null");
        Preconditions.checkArgument(location != null, "The location of a respawn point must not be null");
    }

    /**
     * @param world    the world to respawn in
     * @param location the position to respawn at
     * @return a point at that position
     */
    public static RespawnPoint of(final World world, final Location location) {
        return new RespawnPoint(world, location);
    }

    /**
     * @param world the world to respawn in
     * @param x     the x coordinate
     * @param y     the y coordinate
     * @param z     the z coordinate
     * @return a point at those coordinates, facing south
     */
    public static RespawnPoint of(final World world, final double x, final double y, final double z) {
        return new RespawnPoint(world, new Location(x, y, z, 0f, 0f));
    }

    /**
     * @param entity the entity whose current placement is captured
     * @return a point at that entity's world and location
     */
    public static RespawnPoint of(final Entity entity) {
        return new RespawnPoint(entity.world(), entity.location());
    }
}
