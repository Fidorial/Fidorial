package fr.fidorial.entity;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * A handle on a player identity, whether or not it is connected.
 *
 * <p>Obtained from {@link OfflinePlayers}. Two handles are equal when they share the same uuid.</p>
 *
 * @since 0.1.0
 */
public final class OfflinePlayer {

    private final OfflinePlayers registry;
    private final UUID uuid;
    private final @Nullable String name;
    private final long firstSeen;
    private final long lastSeen;

    private OfflinePlayer(
            final OfflinePlayers registry,
            final UUID uuid,
            final @Nullable String name,
            final long firstSeen,
            final long lastSeen
    ) {
        Preconditions.checkArgument(registry != null, "The registry of an offline player must not be null");
        Preconditions.checkArgument(uuid != null, "The UUID of an offline player must not be null");
        this.registry = registry;
        this.uuid = uuid;
        this.name = name;
        this.firstSeen = firstSeen;
        this.lastSeen = lastSeen;
    }

    /**
     * Creates a handle bound to the given registry.
     *
     * @param registry  the registry serving the state-dependent operations
     * @param uuid      the player identity
     * @param name      the last known name, or {@code null} if none was ever observed
     * @param firstSeen the epoch milliseconds of the first observation, or {@code 0} if unknown
     * @param lastSeen  the epoch milliseconds of the most recent observation, or {@code 0} if
     *                  unknown
     * @return a new handle
     * @since 0.1.0
     */
    @ApiStatus.Internal
    @Contract(value = "_, _, _, _, _ -> new", pure = true)
    public static OfflinePlayer of(
            final OfflinePlayers registry,
            final UUID uuid,
            final @Nullable String name,
            final long firstSeen,
            final long lastSeen
    ) {
        return new OfflinePlayer(registry, uuid, name, firstSeen, lastSeen);
    }

    /**
     * Gets the unique identifier of this player.
     *
     * @return the player identity
     * @since 0.1.0
     */
    @Contract(pure = true)
    public UUID uuid() {
        return uuid;
    }

    /**
     * Gets the most recent name observed for {@link #uuid()}.
     *
     * @return the last known name, or empty if none is known
     * @since 0.1.0
     */
    @Contract(pure = true)
    public Optional<String> name() {
        return Optional.ofNullable(name);
    }

    /**
     * Gets a label suitable for display, falling back to the identity when no name is known.
     *
     * @return the last known name, or the string form of {@link #uuid()}
     * @since 0.1.0
     */
    @Contract(pure = true)
    public String label() {
        return name != null ? name : uuid.toString();
    }

    /**
     * Checks whether the server holds any record of this identity.
     *
     * @return {@code true} if this identity has been observed at least once
     * @since 0.1.0
     */
    @Contract(pure = true)
    public boolean hasPlayedBefore() {
        return firstSeen != 0L;
    }

    /**
     * Gets the time at which this identity was first observed.
     *
     * @return the first observation, or empty if this identity has never been observed
     * @since 0.1.0
     */
    @Contract(pure = true)
    public Optional<Instant> firstSeen() {
        return firstSeen == 0L ? Optional.empty() : Optional.of(Instant.ofEpochMilli(firstSeen));
    }

    /**
     * Gets the time at which this identity was most recently observed.
     *
     * @return the most recent observation, or empty if this identity has never been observed
     * @since 0.1.0
     */
    @Contract(pure = true)
    public Optional<Instant> lastSeen() {
        return lastSeen == 0L ? Optional.empty() : Optional.of(Instant.ofEpochMilli(lastSeen));
    }

    /**
     * Gets the connected player behind this identity.
     *
     * @return the connected player, or empty if this identity is not connected
     * @since 0.1.0
     */
    public Optional<? extends Player> online() {
        return registry.online(uuid);
    }

    /**
     * Checks whether this identity is currently connected.
     *
     * @return {@code true} if a player with this identity is connected
     * @since 0.1.0
     */
    public boolean isOnline() {
        return online().isPresent();
    }

    /**
     * Performs the given action on the connected player behind this identity, if there is one.
     *
     * @param action the action to perform
     * @since 0.1.0
     */
    public void ifOnline(final Consumer<? super Player> action) {
        online().ifPresent(action);
    }

    /**
     * Checks whether this identity holds operator status.
     *
     * @return {@code true} if this identity is an operator
     * @since 0.1.0
     */
    public boolean isOperator() {
        return registry.isOperator(uuid);
    }

    /**
     * Sets the operator status of this identity.
     *
     * @param operator the new operator status
     * @since 0.1.0
     */
    public void setOperator(final boolean operator) {
        registry.setOperator(uuid, operator);
    }

    /**
     * Checks whether this identity is banned.
     *
     * <p>Only the identity is considered: a player connecting from a banned address is not banned
     * as far as this is concerned. Ask the ban service directly when the address matters.</p>
     *
     * @return {@code true} if this identity is banned
     * @since 0.1.0
     */
    public boolean isBanned() {
        return registry.isBanned(uuid);
    }

    /**
     * Checks whether this identity is on the whitelist, regardless of whether the whitelist is
     * being enforced.
     *
     * @return {@code true} if this identity is listed
     * @since 0.1.0
     */
    public boolean isWhitelisted() {
        return registry.isWhitelisted(uuid);
    }

    /**
     * Reads the saved state of this identity.
     *
     * @return a future completing with the saved state, or with an empty result if none exists
     * @since 0.1.0
     */
    public CompletableFuture<Optional<OfflinePlayerSnapshot>> snapshot() {
        return registry.snapshot(uuid);
    }

    /**
     * Produces an updated handle for this identity.
     *
     * @return a future completing with an updated handle for the same identity
     * @since 0.1.0
     */
    public CompletableFuture<OfflinePlayer> refresh() {
        return registry.refresh(uuid);
    }

    /**
     * Compares handles by identity. Names and observation times are not considered.
     *
     * @param o the object to compare against
     * @return {@code true} if the argument is a handle for the same identity
     */
    @Override
    public boolean equals(final @Nullable Object o) {
        return o instanceof final OfflinePlayer other && uuid.equals(other.uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public String toString() {
        return "OfflinePlayer[" + uuid + (name == null ? "" : ", " + name) + ']';
    }
}
