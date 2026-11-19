package fr.fidorial.event.player;

import fr.fidorial.entity.player.Player;
import fr.fidorial.entity.player.profile.PlayerProfile;
import fr.fidorial.event.Cancellable;
import fr.fidorial.event.Event;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Called when a client attempts to log in.
 *
 * <p>Listeners can refuse the connection with {@link #refuse(Component)}. This is preferred over
 * {@link #setCancelled(boolean)}, which does not allow you to say why. To decide from a cookie, such as one
 * stored by the server that transferred the player here, use {@link #retrieveCookie(Key, Consumer)}: the
 * login waits for the client's answer.</p>
 *
 * @since 0.1.0
 */
public interface PlayerLoginAttemptEvent extends Event, Cancellable {

    /**
     * Gets the identity the client is connecting with.
     *
     * @return the profile
     * @since 0.1.0
     */
    PlayerProfile profile();

    /**
     * Gets the address the client is connecting from.
     *
     * <p>When a proxy sits in front of the server, this is the address the proxy forwarded, not the
     * proxy's own.</p>
     *
     * @return the client address
     * @since 0.1.0
     */
    String address();

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
    boolean authenticated();

    /**
     * Checks whether the client was sent here by another server, with a transfer.
     *
     * <p>Only clients the server accepts transfers from get this far: with
     * {@code network.accepts-transfers} off, they are turned away before this event. The flag is
     * whatever the client announced, so it does not prove which server sent it.</p>
     *
     * @return {@code true} if the client announced a transfer
     * @since 0.1.0
     */
    boolean transferred();

    /**
     * Asks the client for a cookie, and holds the login until it answers.
     *
     * <p>{@code action} receives the content of the cookie, or an empty optional if the client has none,
     * and can still {@linkplain #refuse(Component) refuse} the connection: the server decides once every
     * cookie asked for has been answered. Call this while handling the event, or from the action of
     * another cookie; the action runs on the network thread, so it must not block. If it throws, the
     * connection is refused.</p>
     *
     * <pre>{@code
     * event.retrieveCookie(TICKET, ticket -> {
     *     if (ticket.isEmpty() || !tickets.verify(ticket.get())) {
     *         event.refuse(Component.text("Join through the lobby"));
     *     }
     * });
     * }</pre>
     *
     * <p>Clients only keep cookies through a {@linkplain #transferred() transfer}: a client connecting
     * directly has none. The client can send back anything, so sign the cookies you need to trust.</p>
     *
     * @param key    the identifier of the cookie
     * @param action what to do with the cookie, once the client has answered
     * @throws IllegalArgumentException if {@code key} or {@code action} is null
     * @see Player#storeCookie(Key, byte[])
     * @since 0.1.0
     */
    void retrieveCookie(Key key, Consumer<Optional<byte[]>> action);

    /**
     * Refuses the connection, disconnecting the client with the given reason.
     *
     * @param reason the message shown to the client
     * @throws IllegalArgumentException if {@code reason} is null
     * @since 0.1.0
     */
    void refuse(Component reason);

    /**
     * Gets the reason the connection was refused.
     *
     * @return the reason, or empty when none was given - including when the event was cancelled
     * through {@link #setCancelled(boolean)}
     * @since 0.1.0
     */
    Optional<Component> refusal();

    /**
     * Refuses or re-allows the connection.
     *
     * <p>Prefer {@link #refuse(Component)}, which lets you say why. Re-allowing a connection another
     * listener refused also drops the reason it gave.</p>
     *
     * @param cancelled whether the connection should be refused
     * @since 0.1.0
     */
    @Override
    void setCancelled(boolean cancelled);
}
