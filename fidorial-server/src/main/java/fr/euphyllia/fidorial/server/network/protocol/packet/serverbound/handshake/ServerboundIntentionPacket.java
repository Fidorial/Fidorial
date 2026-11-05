package fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.handshake;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.StringLikeCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkDecoder;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.HandshakePacketListener;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
import fr.fidorial.protocol.PacketListener;
import fr.fidorial.protocol.ServerboundPacket;

public record ServerboundIntentionPacket(int protocolVersion, String hostname, int port, int nextState)
        implements ServerboundPacket {

    public static final NetworkDecoder<PacketBuffer, ServerboundIntentionPacket> DECODER = NetworkDecoder.composite(
            PrimitiveCodecs.VAR_INT,
            StringLikeCodecs.string(1024),
            PrimitiveCodecs.UNSIGNED_SHORT,
            PrimitiveCodecs.VAR_INT,
            ServerboundIntentionPacket::new);

    @Override
    public void handle(final PacketListener listener) {
        ((HandshakePacketListener) listener).handleIntention(this);
    }
}
