package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.common;

import com.google.common.base.Preconditions;
import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.StringLikeCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
import fr.fidorial.messaging.PluginMessenger;
import net.kyori.adventure.key.Key;

/**
 * A plugin message for the client, or for the proxy reading the connection, in the configuration or play
 * phase.
 *
 * @param name    the packet identifier for the current connection phase
 * @param channel the channel of the message
 * @param data    the content of the message, up to the end of the packet
 * @see <a href="https://minecraft.wiki/w/Java_Edition_protocol/Packets#Clientbound_Plugin_Message_(configuration)">Clientbound Plugin Message</a>
 */
public record ClientboundCustomPayloadPacket(Key name, Key channel, byte[] data)
        implements ClientboundPacket<ClientboundCustomPayloadPacket> {

    /**
     * The content is not prefixed by its length: it runs to the end of the packet.
     */
    private static final NetworkEncoder<PacketBuffer, byte[]> REST_OF_PACKET = (buf, data) -> buf.nettyBuf().writeBytes(data);

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundCustomPayloadPacket> CODEC = NetworkEncoder
            .field(StringLikeCodecs.KEY, ClientboundCustomPayloadPacket::channel)
            .then(REST_OF_PACKET, ClientboundCustomPayloadPacket::data);

    public ClientboundCustomPayloadPacket {
        Preconditions.checkArgument(name != null, "The packet identifier of a plugin message must not be null");
        Preconditions.checkArgument(channel != null, "The channel of a plugin message must not be null");
        Preconditions.checkArgument(data != null, "The data of a plugin message must not be null");
        Preconditions.checkArgument(data.length <= PluginMessenger.MAX_SENT_SIZE,
                "A plugin message holds at most %s bytes, got %s", PluginMessenger.MAX_SENT_SIZE, data.length);
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundCustomPayloadPacket> codec() {
        return CODEC;
    }
}
