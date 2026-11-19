package fr.euphyllia.fidorial.server.network.messaging;

import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.fidorial.messaging.PluginMessage;
import net.kyori.adventure.key.Key;

/**
 * A message received on a channel; every listener of the channel gets the same one.
 */
record ReceivedPluginMessage(ServerPlayer player, Key channel, byte[] data) implements PluginMessage {

    @Override
    public byte[] data() {
        // Every listener gets the same message: none can change what the next one reads.
        return data.clone();
    }

    @Override
    public void reply(final byte[] data) {
        player.sendPluginMessage(channel, data);
    }
}
