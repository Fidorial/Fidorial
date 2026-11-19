package fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.common;

import fr.euphyllia.fidorial.server.codecs.networking.NetworkDecoder;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.ConfigurationPacketListener;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.PlayPacketListener;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
import fr.fidorial.messaging.PluginMessenger;
import fr.fidorial.protocol.PacketListener;
import fr.fidorial.protocol.ServerboundPacket;
import io.netty.handler.codec.DecoderException;
import net.kyori.adventure.key.Key;

/**
 * A plugin message from the client, or from the proxy writing on its connection, in the configuration or
 * play phase.
 *
 * @param channel the channel of the message
 * @param data    the content of the message, up to the end of the packet
 * @see <a href="https://minecraft.wiki/w/Java_Edition_protocol/Packets#Serverbound_Plugin_Message_(configuration)">Serverbound Plugin Message</a>
 */
public record ServerboundCustomPayloadPacket(Key channel, byte[] data) implements ServerboundPacket {

    public static final NetworkDecoder<PacketBuffer, ServerboundCustomPayloadPacket> DECODER =
            ServerboundCustomPayloadPacket::decode;

    private static ServerboundCustomPayloadPacket decode(final PacketBuffer buf) {
        final Key channel = buf.readKey();
        if (buf.readableBytes() > PluginMessenger.MAX_RECEIVED_SIZE) {
            throw new DecoderException("A plugin message holds at most " + PluginMessenger.MAX_RECEIVED_SIZE
                    + " bytes, got " + buf.readableBytes() + " on " + channel.asString());
        }
        return new ServerboundCustomPayloadPacket(channel, buf.readRemainingBytes());
    }

    @Override
    public void handle(final PacketListener listener) {
        switch (listener) {
            case final PlayPacketListener play -> play.handleCustomPayload(this);
            case final ConfigurationPacketListener config -> config.handleCustomPayload(this);
            default -> {
            }
        }
    }
}
