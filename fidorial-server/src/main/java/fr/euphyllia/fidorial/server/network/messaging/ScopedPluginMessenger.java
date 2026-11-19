package fr.euphyllia.fidorial.server.network.messaging;

import fr.fidorial.event.Subscription;
import fr.fidorial.messaging.PluginMessageListener;
import fr.fidorial.messaging.PluginMessenger;
import net.kyori.adventure.key.Key;

import java.util.Set;

/**
 * The messenger of one plugin: its listeners belong to the plugin, and stop with it.
 */
final class ScopedPluginMessenger implements PluginMessenger {

    private final PluginChannelRegistry registry;
    private final Object owner;

    ScopedPluginMessenger(final PluginChannelRegistry registry, final Object owner) {
        this.registry = registry;
        this.owner = owner;
    }

    @Override
    public Subscription listen(final Key channel, final PluginMessageListener listener) {
        return registry.listen(owner, channel, listener);
    }

    @Override
    public Set<Key> channels() {
        return registry.channels();
    }
}
