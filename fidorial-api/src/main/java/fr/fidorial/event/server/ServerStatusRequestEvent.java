package fr.fidorial.event.server;

import fr.fidorial.event.Event;
import fr.fidorial.status.ServerStatus;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;

/**
 * Called when a client requests the server list status.
 *
 * @since 0.1.0
 */
@ApiStatus.NonExtendable
public interface ServerStatusRequestEvent extends Event {
    // todo: expose client connection that requested the status

    /**
     * Gets the status that will be sent to the requesting client.
     *
     * @return current response status
     * @since 0.1.0
     */
    @Contract(pure = true)
    ServerStatus status();

    /**
     * Replaces the status that will be sent to the requesting client.
     *
     * @param status new response status
     * @since 0.1.0
     */
    @Contract(mutates = "this")
    void status(ServerStatus status);
}
