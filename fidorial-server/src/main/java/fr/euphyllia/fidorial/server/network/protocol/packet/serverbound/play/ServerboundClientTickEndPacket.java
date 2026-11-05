package fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.NetworkDecoder;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.PlayPacketListener;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
import fr.fidorial.protocol.PacketListener;
import fr.fidorial.protocol.ServerboundPacket;

public record ServerboundClientTickEndPacket() implements ServerboundPacket {

    public static final NetworkDecoder<PacketBuffer, ServerboundClientTickEndPacket> DECODER = _ -> new ServerboundClientTickEndPacket();

    @Override
    public void handle(final PacketListener listener) {
        ((PlayPacketListener) listener).handleClientTickEnd(this);
    }
}
