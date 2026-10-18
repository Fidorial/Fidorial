package fr.fidorial.moderation;

import fr.fidorial.entity.player.profile.PlayerProfile;
import org.jetbrains.annotations.Contract;

import java.util.UUID;
import java.util.stream.Stream;

/**
 * A service for managing the whitelist of allowed players.
 *
 * @since 0.1.0
 */
public interface WhitelistManager {

    /**
     * Loads the whitelist.
     */
    void load();

    /**
     * Checks whether the whitelist is being enforced.
     *
     * @return {@code true} when only listed players may connect
     * @since 0.1.0
     */
    @Contract(pure = true)
    boolean enabled();

    /**
     * Starts or stops enforcing the whitelist.
     *
     * <p>This does not disconnect anyone. Fidorial kicks the players who are no longer allowed
     * separately, so that a change made by other means does not carry that side effect.</p>
     *
     * @param enabled whether the whitelist should be enforced
     * @return {@code true} when the setting actually changed
     * @since 0.1.0
     */
    boolean enabled(boolean enabled);

    /**
     * Checks whether an identity is listed, regardless of whether the whitelist is enforced.
     *
     * @param uuid the player identity
     * @return {@code true} when the identity is on the list
     * @since 0.1.0
     */
    @Contract(pure = true)
    boolean contains(UUID uuid);

    /**
     * Checks whether an identity may connect as far as the whitelist is concerned.
     *
     * <p>This is the question the login check asks, and the only one it asks. The whitelist is
     * enforced for everyone once enabled: operators are not exempt, so an operator who is not
     * listed is refused like anyone else.</p>
     *
     * @param uuid the player identity
     * @return {@code true} when the whitelist is not enforced, or the identity is listed
     * @since 0.1.0
     */
    @Contract(pure = true)
    default boolean allows(final UUID uuid) {
        return !enabled() || contains(uuid);
    }

    /**
     * Adds a profile to the list.
     *
     * @param profile the profile to allow
     * @return {@code true} when the identity was not already listed
     * @since 0.1.0
     */
    boolean add(PlayerProfile profile);

    /**
     * Adds an identity to the list.
     *
     * @param uuid the identity to allow
     * @param name the name to record for display
     * @return {@code true} when the identity was not already listed
     * @since 0.1.0
     */
    default boolean add(final UUID uuid, final String name) {
        return add(new PlayerProfile(uuid, name));
    }

    /**
     * Removes an identity from the list.
     *
     * @param uuid the identity to remove
     * @return {@code true} when the identity was actually listed
     * @since 0.1.0
     */
    boolean remove(UUID uuid);

    /**
     * Gets the listed identities.
     *
     * @return the entries, ordered by name
     * @since 0.1.0
     */
    @Contract(pure = true)
    Stream<PlayerProfile> entries();

    /**
     * Gets how many identities are listed.
     *
     * @return the number of entries
     * @since 0.1.0
     */
    @Contract(pure = true)
    int totalEntries();
}
