package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.common;

import com.google.common.base.Preconditions;
import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.StringLikeCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
import net.kyori.adventure.key.Key;

import java.util.Map;

/**
 * Tells the client to leave this server and connect to {@code host}:{@code port}, announcing a transfer
 * (intention {@code 3}) in its handshake. The target server must accept transfers.
 *
 * @param name       the packet identifier for the current connection phase
 * @param host       the hostname or IP address of the target server
 * @param port       the port of the target server
 * @param properties string pairs handed to the client along with the target, which this server has no
 *                   use for: it sends none
 * @see <a href="https://minecraft.wiki/w/Java_Edition_protocol/Packets#Transfer_(configuration)">Transfer</a>
 */
public record ClientboundTransferPacket(Key name, String host, int port, Map<String, String> properties)
        implements ClientboundPacket<ClientboundTransferPacket> {

    /**
     * A VarInt count, then each name followed by its value.
     */
    private static final NetworkEncoder<PacketBuffer, Map<String, String>> PROPERTIES = (buf, properties) -> {
        buf.writeVarInt(properties.size());
        properties.forEach((property, value) -> {
            StringLikeCodecs.STRING.write(buf, property);
            StringLikeCodecs.STRING.write(buf, value);
        });
    };

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundTransferPacket> CODEC = NetworkEncoder
            .field(StringLikeCodecs.STRING, ClientboundTransferPacket::host)
            .then(PrimitiveCodecs.VAR_INT, ClientboundTransferPacket::port)
            .then(PROPERTIES, ClientboundTransferPacket::properties);

    public ClientboundTransferPacket {
        Preconditions.checkArgument(name != null, "The packet identifier of a transfer must not be null");
        Preconditions.checkArgument(host != null, "The host of a transfer must not be null");
        Preconditions.checkArgument(properties != null, "The properties of a transfer must not be null");
        properties = Map.copyOf(properties);
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundTransferPacket> codec() {
        return CODEC;
    }
}
