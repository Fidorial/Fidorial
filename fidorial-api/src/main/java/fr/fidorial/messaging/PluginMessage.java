package fr.fidorial.messaging;

import fr.fidorial.entity.player.Player;
import net.kyori.adventure.key.Key;

/**
 * A message received on a channel, from the client of a player or from the proxy on its connection.
 *
 * @since 0.1.0
 */
public interface PluginMessage {

    /**
     * Gets the player whose connection carried the message.
     *
     * @return the player
     * @since 0.1.0
     */
    Player player();

    /**
     * Gets the channel the message was sent on.
     *
     * @return the channel
     * @since 0.1.0
     */
    Key channel();

    /**
     * Gets the content of the message.
     *
     * @return a copy of the content, at most {@link PluginMessenger#MAX_RECEIVED_SIZE} bytes
     * @since 0.1.0
     */
    byte[] data();

    /**
     * Answers on the same channel, through the same connection.
     *
     * @param data the content of the answer, at most {@link PluginMessenger#MAX_SENT_SIZE} bytes
     * @throws IllegalArgumentException if {@code data} is null or too large
     * @see Player#sendPluginMessage(Key, byte[])
     * @since 0.1.0
     */
    void reply(byte[] data);
}
