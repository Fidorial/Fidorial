package fr.fidorial.event.player;

import fr.fidorial.entity.Player;
import fr.fidorial.math.Location;
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
    private Location location;


    public PlayerRespawnEvent(
            final Player player,
            final Location location,
            final Cause cause,
            final boolean usedRespawnPoint) {
        this.player = player;
        this.location = location;
        this.cause = cause;
        this.usedRespawnPoint = usedRespawnPoint;
    }

    @Contract(pure = true)
    @Override
    public Player player() {
        return player;
    }

    /**
     * @return the position the player is about to respawn at
     */
    @Contract(pure = true)
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
     * @param location the position to respawn at instead, in the same world
     */
    public void setRespawnLocation(final Location location) {
        this.location = location;
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
