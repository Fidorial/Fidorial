package fr.fidorial.event.player;

import com.google.common.base.Preconditions;
import fr.fidorial.entity.PlayerProfile;
import fr.fidorial.event.Cancellable;
import fr.fidorial.event.Event;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * Called when a client attempts to log in.
 *
 * <p>Listeners can refuse the connection with {@link #refuse(Component)}. This is preferred over
 * {@link #setCancelled(boolean)}, which does not allow you to say why.</p>
 *
 * @since 0.1.0
 */
public final class PlayerLoginAttemptEvent implements Event, Cancellable {

    private final PlayerProfile profile;
    private final String address;
    private final boolean authenticated;

    private boolean cancelled;
    private @Nullable Component refusal;

    /**
     * Creates an event.
     *
     * @param profile       the identity the client is connecting with
     * @param address       the client address
     * @param authenticated whether the identity was verified, rather than derived from a name
     * @since 0.1.0
     */
    public PlayerLoginAttemptEvent(final PlayerProfile profile, final String address, final boolean authenticated) {
        Preconditions.checkArgument(profile != null, "The profile of a player login attempt event must not be null");
        Preconditions.checkArgument(address != null, "The address of a player login attempt event must not be null");
        this.profile = profile;
        this.address = address;
        this.authenticated = authenticated;
    }

    /**
     * Gets the identity the client is connecting with.
     *
     * @return the profile
     * @since 0.1.0
     */
    public PlayerProfile profile() {
        return profile;
    }

    /**
     * Gets the address the client is connecting from.
     *
     * <p>When a proxy sits in front of the server, this is the address the proxy forwarded, not the
     * proxy's own.</p>
     *
     * @return the client address
     * @since 0.1.0
     */
    public String address() {
        return address;
    }

    /**
     * Checks whether this identity was actually verified.
     *
     * <p>{@code false} means the server is running in offline mode and the uuid was derived from
     * the name the client asked for, so it proves nothing. Treat anything security-sensitive
     * accordingly.</p>
     *
     * @return {@code true} if the identity was verified
     * @since 0.1.0
     */
    public boolean authenticated() {
        return authenticated;
    }

    /**
     * Refuses the connection, disconnecting the client with the given reason.
     *
     * @param reason the message shown to the client
     * @since 0.1.0
     */
    public void refuse(final Component reason) {
        Preconditions.checkArgument(reason != null, "The reason passed to PlayerLoginAttemptEvent.refuse() must not be null");
        this.refusal = reason;
        this.cancelled = true;
    }

    /**
     * Gets the reason the connection was refused.
     *
     * @return the reason, or empty when none was given - including when the event was cancelled
     * through {@link #setCancelled(boolean)}
     * @since 0.1.0
     */
    public Optional<Component> refusal() {
        return Optional.ofNullable(refusal);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Refuses or re-allows the connection.
     *
     * <p>Prefer {@link #refuse(Component)}, which lets you say why. Re-allowing a connection another
     * listener refused also drops the reason it gave.</p>
     *
     * @param cancelled whether the connection should be refused
     */
    @Override
    public void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
        if (!cancelled) {
            this.refusal = null;
        }
    }
}
