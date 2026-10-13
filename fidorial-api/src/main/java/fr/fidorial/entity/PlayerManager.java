package fr.fidorial.entity;

import fr.fidorial.Server;
import fr.fidorial.moderation.BanManager;
import fr.fidorial.moderation.WhitelistManager;
import org.jetbrains.annotations.Contract;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * The players of the server: who is online, every known identity, and who may join.
 *
 * @see Server#players()
 * @since 0.1.0
 */
public interface PlayerManager {

    /**
     * Gets the players currently connected.
     *
     * @return the online players
     * @since 0.1.0
     */
    Collection<? extends Player> online();

    /**
     * Gets the current online player count.
     *
     * @return online player count
     * @since 0.1.0
     */
    @Contract(pure = true)
    int count();

    /**
     * Looks up an online player by identity.
     *
     * @param uuid the player identity
     * @return the player, or empty if no player with that identity is connected
     * @since 0.1.0
     */
    default Optional<? extends Player> get(final UUID uuid) {
        return online().stream().filter(player -> player.uuid().equals(uuid)).findFirst();
    }

    /**
     * Looks up an online player by name, ignoring case.
     *
     * @param name the player name
     * @return the player, or empty if no player with that name is connected
     * @since 0.1.0
     */
    default Optional<? extends Player> get(final String name) {
        return online().stream().filter(player -> player.name().equalsIgnoreCase(name)).findFirst();
    }

    /**
     * Gets the directory of every identity the server knows of, connected or not.
     *
     * @return the offline player directory
     * @since 0.1.0
     */
    @Contract(pure = true)
    OfflinePlayerManager offline();

    /**
     * Gets the service holding the players who are not allowed to connect.
     *
     * @return the ban service
     * @since 0.1.0
     */
    @Contract(pure = true)
    BanManager bans();

    /**
     * Gets the service holding the players allowed to connect while the whitelist is enforced.
     *
     * @return the whitelist service
     * @since 0.1.0
     */
    @Contract(pure = true)
    WhitelistManager whitelist();
}
