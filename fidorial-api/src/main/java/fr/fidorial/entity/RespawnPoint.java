package fr.fidorial.entity;

import fr.fidorial.math.Position;
import fr.fidorial.world.World;

/**
 * The place a player comes back to after dying.
 *
 * <p>A point is bound to a {@link World}: when that world is no longer loaded at respawn time the
 * server silently falls back to the configured world spawn and clears the point.</p>
 *
 * @param world    the world to respawn in
 * @param position the position and orientation to respawn at
 * @see Player#setRespawnPoint(RespawnPoint)
 * @since 0.1.0
 */
public record RespawnPoint(World world, Position position) {
}
