package fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.status;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkDecoder;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.StatusPacketListener;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
import fr.fidorial.protocol.PacketListener;
import fr.fidorial.protocol.ServerboundPacket;

public record ServerboundPingRequestPacket(long payload) implements ServerboundPacket {

    public static final NetworkDecoder<PacketBuffer, ServerboundPingRequestPacket> DECODER = NetworkDecoder.composite(
            PrimitiveCodecs.LONG,
            ServerboundPingRequestPacket::new);

    @Override
    public void handle(final PacketListener listener) {
        ((StatusPacketListener) listener).handlePingRequest(this);
    }
}
