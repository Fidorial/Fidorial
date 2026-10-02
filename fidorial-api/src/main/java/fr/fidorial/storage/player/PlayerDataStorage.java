package fr.fidorial.storage.player;

import fr.fidorial.entity.GameMode;
import fr.fidorial.math.Location;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.UUID;

public interface PlayerDataStorage {

    PlayerData load(UUID uuid) throws IOException;

    void save(UUID uuid, PlayerData data) throws IOException;

    /**
     * Checks whether saved data exists for an identity, without loading it.
     *
     * @param uuid the player identity
     * @return {@code true} if data has been saved for that identity
     * @throws IOException if the check fails
     */
    default boolean exists(final UUID uuid) throws IOException {
        return true;
    }

    /**
     * @param gameMode        the mode the player left in
     * @param respawnLocation the position the player respawns at, or {@code null} for the world spawn
     * @param lastLocation    the position the player was last at, or {@code null} if never saved
     */
    record PlayerData(
            GameMode gameMode,
            @Nullable Location respawnLocation,
            @Nullable Location lastLocation
    ) {
    }
}
