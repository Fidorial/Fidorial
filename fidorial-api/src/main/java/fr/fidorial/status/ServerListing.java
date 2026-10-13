package fr.fidorial.status;

import fr.fidorial.Server;
import fr.fidorial.event.server.ServerStatusRequestEvent;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Contract;

import java.util.Optional;

/**
 * How the server shows up in the multiplayer server list: the values used to answer status pings.
 *
 * <p>Changes apply to the next ping. To change a single answer, listen to
 * {@link ServerStatusRequestEvent} instead.</p>
 *
 * @see Server#listing()
 * @since 0.1.0
 */
public interface ServerListing {

    /**
     * Gets the server's favicon shown in the status ping.
     *
     * @return the server favicon
     * @since 0.1.0
     */
    @Contract(pure = true)
    Optional<Favicon> favicon();

    /**
     * Sets the server's favicon shown in the status ping.
     *
     * @param favicon the server favicon
     * @since 0.1.0
     */
    @Contract(mutates = "this")
    void favicon(Favicon favicon);

    /**
     * Gets the server description shown in the status ping.
     *
     * @return server description
     * @since 0.1.0
     */
    @Contract(pure = true)
    Component description();

    /**
     * Sets the server description shown in the status ping.
     *
     * @param description server description
     * @since 0.1.0
     */
    @Contract(mutates = "this")
    void description(Component description);

    /**
     * Gets the maximum player count shown in the status ping.
     *
     * @return maximum player count
     * @since 0.1.0
     */
    @Contract(pure = true)
    int maxPlayers();

    /**
     * Sets the maximum player count shown in the status ping.
     *
     * @param maxPlayers maximum player count
     * @since 0.1.0
     */
    @Contract(mutates = "this")
    void maxPlayers(int maxPlayers);
}
