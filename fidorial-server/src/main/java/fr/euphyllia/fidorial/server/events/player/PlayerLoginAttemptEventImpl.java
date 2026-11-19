package fr.euphyllia.fidorial.server.events.player;

import com.google.common.base.Preconditions;
import fr.fidorial.entity.player.profile.PlayerProfile;
import fr.fidorial.event.player.PlayerLoginAttemptEvent;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

public final class PlayerLoginAttemptEventImpl implements PlayerLoginAttemptEvent {

    private final PlayerProfile profile;
    private final String address;
    private final boolean authenticated;
    private final boolean transferred;
    private final CookieRequester cookies;

    private boolean cancelled;
    private @Nullable Component refusal;

    /**
     * Creates an event.
     *
     * @param profile       the identity the client is connecting with
     * @param address       the client address
     * @param authenticated whether the identity was verified, rather than derived from a name
     * @param transferred   whether another server sent the client here with a transfer
     * @param cookies       asks the client for the cookies requested with {@link #retrieveCookie(Key, Consumer)}
     */
    public PlayerLoginAttemptEventImpl(
            final PlayerProfile profile,
            final String address,
            final boolean authenticated,
            final boolean transferred,
            final CookieRequester cookies) {
        Preconditions.checkArgument(profile != null, "The profile of a player login attempt event must not be null");
        Preconditions.checkArgument(address != null, "The address of a player login attempt event must not be null");
        Preconditions.checkArgument(cookies != null, "The cookie requester of a player login attempt event must not be null");
        this.profile = profile;
        this.address = address;
        this.authenticated = authenticated;
        this.transferred = transferred;
        this.cookies = cookies;
    }

    @Override
    public PlayerProfile profile() {
        return profile;
    }

    @Override
    public String address() {
        return address;
    }

    @Override
    public boolean authenticated() {
        return authenticated;
    }

    @Override
    public boolean transferred() {
        return transferred;
    }

    @Override
    public void retrieveCookie(final Key key, final Consumer<Optional<byte[]>> action) {
        Preconditions.checkArgument(key != null, "The key of a cookie must not be null");
        Preconditions.checkArgument(action != null, "The action on a cookie must not be null");
        cookies.request(key, action);
    }

    @Override
    public void refuse(final Component reason) {
        Preconditions.checkArgument(reason != null, "The reason passed to PlayerLoginAttemptEvent.refuse() must not be null");
        this.refusal = reason;
        this.cancelled = true;
    }

    @Override
    public Optional<Component> refusal() {
        return Optional.ofNullable(refusal);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
        if (!cancelled) {
            this.refusal = null;
        }
    }

    /**
     * Asks the client for a cookie during its login, on behalf of {@link #retrieveCookie(Key, Consumer)}.
     */
    @FunctionalInterface
    public interface CookieRequester {

        /**
         * Asks the client for a cookie, and runs {@code action} with its answer.
         *
         * @param key    the identifier of the cookie
         * @param action what to do with the content of the cookie, or with an empty optional
         */
        void request(Key key, Consumer<Optional<byte[]>> action);
    }
}
