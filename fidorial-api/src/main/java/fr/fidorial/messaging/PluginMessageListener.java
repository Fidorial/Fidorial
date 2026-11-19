package fr.fidorial.messaging;

/**
 * Receives the messages of a channel, given to {@link PluginMessenger#listen}.
 *
 * @since 0.1.0
 */
@FunctionalInterface
public interface PluginMessageListener {

    /**
     * Handles a message, on the network thread: do not block it.
     *
     * @param message the message
     * @since 0.1.0
     */
    void onMessage(PluginMessage message);
}
