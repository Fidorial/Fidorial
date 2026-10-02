package fr.fidorial.entity;

import fr.fidorial.Server;
import org.jetbrains.annotations.Contract;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * The directory of every player identity the server knows of, connected or not.
 *
 * @see Server#offlinePlayers()
 * @since 0.1.0
 */
public interface OfflinePlayers {

    /**
     * Gets the handle for an identity, whether or not the server has ever observed it.
     *
     * @param uuid the player identity
     * @return the handle, whose {@link OfflinePlayer#hasPlayedBefore()} reports whether anything is
     * known about it
     * @since 0.1.0
     */
    @Contract(pure = true)
    OfflinePlayer of(UUID uuid);

    /**
     * Gets the handle for a connected player.
     *
     * @param player the connected player
     * @return the handle for that player's identity
     * @since 0.1.0
     */
    @Contract(pure = true)
    OfflinePlayer of(Player player);

    /**
     * Looks up an identity by name without contacting storage or the network.
     *
     * @param name the player name
     * @return the handle, or empty if the name is not currently known
     * @since 0.1.0
     */
    @Contract(pure = true)
    Optional<OfflinePlayer> cached(String name);

    /**
     * Looks up an identity without contacting storage or the network.
     *
     * <p>Unlike {@link #of(UUID)}, this reports whether the identity is known.</p>
     *
     * @param uuid the player identity
     * @return the handle, or empty if the identity is not currently known
     * @since 0.1.0
     */
    @Contract(pure = true)
    Optional<OfflinePlayer> cached(UUID uuid);

    /**
     * Resolves an identity by name, consulting the profile source when the name is not already
     * known.
     *
     * @param name the player name
     * @return a future completing with the handle, or with an empty result if no such player exists
     * @since 0.1.0
     */
    CompletableFuture<Optional<OfflinePlayer>> lookup(String name);

    /**
     * Resolves several identities by name.
     *
     * @param names the player names; duplicates and case differences are collapsed
     * @return a future completing with the resolved handles, keyed by the requested name; names
     * that do not resolve are absent from the result
     * @since 0.1.0
     */
    CompletableFuture<Map<String, OfflinePlayer>> lookup(Collection<String> names);

    /**
     * Produces an updated handle for an identity, consulting the profile source when the locally
     * held record has expired.
     *
     * @param uuid the player identity
     * @return a future completing with an updated handle, carrying the previously known name if the
     * lookup does not succeed
     * @since 0.1.0
     */
    CompletableFuture<OfflinePlayer> refresh(UUID uuid);

    /**
     * Gets the connected player behind an identity.
     *
     * @param uuid the player identity
     * @return the connected player, or empty if that identity is not connected
     * @since 0.1.0
     */
    Optional<? extends Player> online(UUID uuid);

    /**
     * Checks whether an identity holds operator status.
     *
     * @param uuid the player identity
     * @return {@code true} if that identity is an operator
     * @since 0.1.0
     */
    boolean isOperator(UUID uuid);

    /**
     * Sets the operator status of an identity, whether or not it is connected.
     *
     * @param uuid     the player identity
     * @param operator the new operator status
     * @since 0.1.0
     */
    void setOperator(UUID uuid, boolean operator);

    /**
     * Checks whether an identity is banned.
     *
     * <p>Only the identity is considered: an identity connecting from a banned address is not
     * banned as far as this is concerned.</p>
     *
     * @param uuid the player identity
     * @return {@code true} if that identity is banned
     * @since 0.1.0
     */
    @Contract(pure = true)
    boolean isBanned(UUID uuid);

    /**
     * Checks whether an identity is on the whitelist, regardless of whether the whitelist is being
     * enforced.
     *
     * @param uuid the player identity
     * @return {@code true} if that identity is listed
     * @since 0.1.0
     */
    @Contract(pure = true)
    boolean isWhitelisted(UUID uuid);

    /**
     * Reads the saved state of an identity, or its current state when it is connected.
     *
     * @param uuid the player identity
     * @return a future completing with the state, or with an empty result if nothing has ever been
     * saved for that identity
     * @since 0.1.0
     */
    CompletableFuture<Optional<OfflinePlayerSnapshot>> snapshot(UUID uuid);

    /**
     * Gets every identity currently held in the cache.
     *
     * @return an immutable snapshot of the known identities
     * @since 0.1.0
     */
    @Contract(pure = true)
    Collection<OfflinePlayer> known();
}
