package fr.fidorial.storage.player;

import fr.fidorial.entity.GameMode;
import fr.fidorial.math.Location;
import fr.fidorial.service.ServiceRegistry;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.UUID;

/**
 * Persistence backend for the game mode, respawn point and last position of players.
 *
 * <p>The server registers a default implementation as a {@linkplain ServiceRegistry
 * service}; plugins may replace it, for instance to store players in a database.</p>
 *
 * @since 0.1.0
 */
public interface PlayerDataStorage {

    /**
     * Loads the data of a player.
     *
     * @param uuid     the player identity
     * @param defaults the values to return when nothing was saved
     * @return the saved data, or {@code defaults}
     * @throws IOException if the data cannot be read
     * @since 0.1.0
     */
    PlayerData load(UUID uuid) throws IOException;

    /**
     * Saves the data of a player.
     *
     * @param uuid the player identity
     * @param data the data to save
     * @throws IOException if the data cannot be written
     * @since 0.1.0
     */
    void save(UUID uuid, PlayerData data) throws IOException;

    /**
     * Checks whether saved data exists for an identity, without loading it.
     *
     * @param uuid the player identity
     * @return {@code true} if data has been saved for that identity
     * @throws IOException if the check fails
     * @since 0.1.0
     */
    default boolean exists(final UUID uuid) throws IOException {
        return true;
    }

    /**
     * The persisted state of a player.
     *
     * @param gameMode        the mode the player left in
     * @param respawnLocation the position the player respawns at, or {@code null} for the world spawn
     * @param lastLocation    the position the player was last at, or {@code null} if never saved
     * @since 0.1.0
     */
    record PlayerData(
            GameMode gameMode,
            @Nullable Location respawnLocation,
            @Nullable Location lastLocation
    ) {
    }
}
