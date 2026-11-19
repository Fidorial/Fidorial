package fr.fidorial.messaging;

import fr.fidorial.entity.player.Player;
import fr.fidorial.event.Subscription;
import net.kyori.adventure.key.Key;

import java.util.Set;

/**
 * Listens to plugin message channels, for one plugin: what it listens to is released when the plugin is
 * disabled.
 *
 * <p>The server announces every channel it listens to, so that clients and proxies know they can send on
 * it, and keeps announcing new channels to the players already in the game. Messages are sent with
 * {@link Player#sendPluginMessage(Key, byte[])}, on any channel, without declaring it first.</p>
 *
 * @since 0.1.0
 */
public interface PluginMessenger {

    /**
     * The most bytes a message sent to a player can hold.
     *
     * @since 0.1.0
     */
    int MAX_SENT_SIZE = 1_048_576;

    /**
     * The most bytes a message received from a player can hold.
     *
     * @since 0.1.0
     */
    int MAX_RECEIVED_SIZE = 32_767;

    /**
     * The channel on which clients and proxies announce the channels they listen to, handled by the
     * server: nothing can listen or send on it.
     *
     * @since 0.1.0
     */
    Key REGISTER = Key.key("register");

    /**
     * The channel on which clients and proxies withdraw the channels they listened to, handled by the
     * server: nothing can listen or send on it.
     *
     * @since 0.1.0
     */
    Key UNREGISTER = Key.key("unregister");

    /**
     * Listens to a channel.
     *
     * <p>{@code listener} receives the messages players send on the channel while they are in the game, on
     * the network thread: it must not block, and hands any work on the world over to the right thread.
     * Several listeners, of one plugin or several, can listen to the same channel.</p>
     *
     * <p>A message can come from the player's client as well as from the proxy: never trust it blindly.</p>
     *
     * @param channel  the channel
     * @param listener what to do with each message
     * @return the subscription, to stop listening before the plugin is disabled
     * @throws IllegalArgumentException if {@code channel} or {@code listener} is null, or if the channel is
     *                                  {@link #REGISTER} or {@link #UNREGISTER}
     * @since 0.1.0
     */
    Subscription listen(Key channel, PluginMessageListener listener);

    /**
     * Gets the channels the server listens to, those of every plugin.
     *
     * @return the channels, as they are now
     * @since 0.1.0
     */
    Set<Key> channels();
}
