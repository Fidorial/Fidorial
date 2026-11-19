package fr.euphyllia.fidorial.server.network.messaging;

import com.google.common.base.Preconditions;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.fidorial.event.Subscription;
import fr.fidorial.messaging.PluginMessageListener;
import fr.fidorial.messaging.PluginMessenger;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The plugin message channels the server listens to, those of every plugin, and the delivery of the
 * messages players send on them.
 *
 * <p>A channel is announced to the players in the game when it gets its first listener, and withdrawn when
 * it loses its last one; players joining later get every channel when they enter the game.</p>
 */
public final class PluginChannelRegistry {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(PluginChannelRegistry.class);

    private final FidorialServer server;
    // Changed under the lock of the registry, so that announcing and withdrawing a channel cannot cross.
    private final Map<Key, List<Registration>> listeners = new ConcurrentHashMap<>();

    public PluginChannelRegistry(final FidorialServer server) {
        this.server = server;
    }

    /**
     * Checks whether the server handles a channel itself, so that nothing else can listen or send on it.
     *
     * @param channel the channel
     * @return {@code true} for the channels announcing and withdrawing channels
     */
    public static boolean isReserved(final Key channel) {
        return channel.equals(PluginMessenger.REGISTER) || channel.equals(PluginMessenger.UNREGISTER);
    }

    /**
     * Creates the messenger of a plugin, whose channels are released by {@link #unregisterAll(Object)}.
     *
     * @param owner the plugin
     * @return the messenger
     */
    public PluginMessenger scopedTo(final Object owner) {
        return new ScopedPluginMessenger(this, owner);
    }

    /**
     * Gets the channels listened to.
     *
     * @return the channels, as they are now
     */
    public Set<Key> channels() {
        return Set.copyOf(listeners.keySet());
    }

    Subscription listen(final Object owner, final Key channel, final PluginMessageListener listener) {
        Preconditions.checkArgument(channel != null, "The channel to listen to must not be null");
        Preconditions.checkArgument(listener != null, "The listener of a plugin message channel must not be null");
        Preconditions.checkArgument(!isReserved(channel), "The channel %s is handled by the server", channel.asString());
        final Registration registration = new Registration(owner, channel, listener);
        synchronized (this) {
            final List<Registration> registrations = listeners.computeIfAbsent(channel, _ -> new CopyOnWriteArrayList<>());
            registrations.add(registration);
            if (registrations.size() == 1) {
                announce(PluginMessenger.REGISTER, List.of(channel));
            }
        }
        return registration;
    }

    /**
     * Stops every listener of a plugin.
     *
     * @param owner the plugin
     */
    public void unregisterAll(final Object owner) {
        final List<Key> withdrawn = new ArrayList<>();
        synchronized (this) {
            for (final List<Registration> registrations : listeners.values()) {
                for (final Registration registration : registrations) {
                    if (registration.owner == owner && remove(registration)) {
                        withdrawn.add(registration.channel);
                    }
                }
            }
            announce(PluginMessenger.UNREGISTER, withdrawn);
        }
    }

    /**
     * Hands a message to the listeners of its channel, on the network thread.
     *
     * @param player  the player whose connection carried the message
     * @param channel the channel of the message
     * @param data    the content of the message
     */
    public void dispatch(final ServerPlayer player, final Key channel, final byte[] data) {
        final List<Registration> registrations = listeners.get(channel);
        if (registrations == null) {
            LOGGER.debug("{} sent a plugin message on {}, which nothing listens to", player.name(), channel.asString());
            return;
        }
        final ReceivedPluginMessage message = new ReceivedPluginMessage(player, channel, data);
        for (final Registration registration : registrations) {
            try {
                registration.listener.onMessage(message);
            } catch (final Throwable t) {
                LOGGER.error("A listener of the plugin message channel {} failed on a message from {}",
                        channel.asString(), player.name(), t);
            }
        }
    }

    /**
     * Removes a registration, under the lock of the registry.
     *
     * @return {@code true} if it was the last listener of its channel, which must then be withdrawn
     */
    private boolean remove(final Registration registration) {
        final List<Registration> registrations = listeners.get(registration.channel);
        if (!registration.active || registrations == null || !registrations.remove(registration)) {
            return false;
        }
        registration.active = false;
        if (!registrations.isEmpty()) {
            return false;
        }
        listeners.remove(registration.channel);
        return true;
    }

    private void announce(final Key type, final Collection<Key> channels) {
        if (channels.isEmpty()) {
            return;
        }
        for (final ServerPlayer player : server.players().online()) {
            player.connection().sendChannelList(type, channels);
        }
    }

    private final class Registration implements Subscription {

        private final Object owner;
        private final Key channel;
        private final PluginMessageListener listener;
        private volatile boolean active = true;

        private Registration(final Object owner, final Key channel, final PluginMessageListener listener) {
            this.owner = owner;
            this.channel = channel;
            this.listener = listener;
        }

        @Override
        public boolean isActive() {
            return active;
        }

        @Override
        public void unsubscribe() {
            synchronized (PluginChannelRegistry.this) {
                if (remove(this)) {
                    announce(PluginMessenger.UNREGISTER, List.of(channel));
                }
            }
        }
    }
}
