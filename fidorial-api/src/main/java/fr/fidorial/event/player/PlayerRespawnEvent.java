package fr.fidorial.event.player;

import fr.fidorial.annotation.ThreadContract;
import fr.fidorial.entity.Player;
import fr.fidorial.entity.RespawnPoint;
import fr.fidorial.world.Location;
import fr.fidorial.world.World;
import org.jetbrains.annotations.Contract;

/**
 * Fired when a dead player clicks respawn, before they are placed back in the world.
 *
 * @since 0.1.0
 */
public final class PlayerRespawnEvent implements PlayerEvent {

    private final Player player;
    private final Cause cause;
    private final boolean usedRespawnPoint;
    private World world;
    private Location location;

    public PlayerRespawnEvent(
            final Player player,
            final World world,
            final Location location,
            final Cause cause,
            final boolean usedRespawnPoint) {
        this.player = player;
        this.world = world;
        this.location = location;
        this.cause = cause;
        this.usedRespawnPoint = usedRespawnPoint;
    }

    @Contract(pure = true)
    @ThreadContract("get -> any")
    @Override
    public Player player() {
        return player;
    }

    /**
     * @return the world the player is about to respawn in
     */
    @Contract(pure = true)
    @ThreadContract("get -> any; modify -> owner")
    public World world() {
        return world;
    }

    /**
     * @return the position the player is about to respawn at
     */
    @Contract(pure = true)
    @ThreadContract("get -> any")
    public Location location() {
        return location;
    }

    /**
     * @return what triggered this respawn
     * @since 0.1.0
     */
    public Cause cause() {
        return cause;
    }

    /**
     * @return {@code true} when the position comes from the player's
     * {@linkplain Player#respawnPoint() respawn point}, {@code false} when it is the world spawn
     * @since 0.1.0
     */
    public boolean usedRespawnPoint() {
        return usedRespawnPoint;
    }

    /**
     * @param world    the world to respawn in instead
     * @param location the position to respawn at instead
     */
    @ThreadContract("set -> owner")
    public void setRespawnLocation(final World world, final Location location) {
        this.world = world;
        this.location = location;
    }

    /**
     * @param location the position to respawn at instead, in the same world
     */
    @ThreadContract("set -> owner")
    public void setRespawnLocation(final Location location) {
        this.location = location;
    }

    /**
     * @param point the point to respawn at instead
     * @since 0.1.0
     */
    @ThreadContract("set -> owner")
    public void setRespawnLocation(final RespawnPoint point) {
        this.world = point.world();
        this.location = point.location();
    }

    /**
     * What made the player respawn.
     *
     * @since 0.1.0
     */
    public enum Cause {
        /**
         * The player clicked the button on the death screen.
         */
        DEATH_SCREEN,
        /**
         * A plugin called {@link Player#respawn()}, or an operator ran {@code /respawn}.
         */
        API
    }
}
