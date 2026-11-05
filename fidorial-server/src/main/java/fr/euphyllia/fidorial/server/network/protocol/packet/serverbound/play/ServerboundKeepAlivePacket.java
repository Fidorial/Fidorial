package fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkDecoder;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.PlayPacketListener;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
import fr.fidorial.protocol.PacketListener;
import fr.fidorial.protocol.ServerboundPacket;

public record ServerboundKeepAlivePacket(long id) implements ServerboundPacket {

    public static final NetworkDecoder<PacketBuffer, ServerboundKeepAlivePacket> DECODER = NetworkDecoder.composite(
            PrimitiveCodecs.LONG,
            ServerboundKeepAlivePacket::new);

    @Override
    public void handle(final PacketListener listener) {
        ((PlayPacketListener) listener).handleKeepAlive(this);
    }
}
