package fr.fidorial.event.server;

import fr.fidorial.Server;
import fr.fidorial.event.Event;

/**
 * Fired once the server has finished starting: worlds are loaded, plugins are enabled and the
 * network is listening.
 *
 * @param server the server that started
 * @since 0.1.0
 */
public record ServerStartedEvent(Server server) implements Event {
}
