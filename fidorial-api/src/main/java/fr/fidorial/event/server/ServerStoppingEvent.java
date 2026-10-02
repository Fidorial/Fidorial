package fr.fidorial.event.server;

import fr.fidorial.Server;
import fr.fidorial.event.Event;

/**
 * Fired when the server starts shutting down, before players are kicked and plugins disabled.
 *
 * @param server the server stopping
 * @since 0.1.0
 */
public record ServerStoppingEvent(Server server) implements Event {
}
