package fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkDecoder;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.PlayPacketListener;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
import fr.fidorial.protocol.PacketListener;
import fr.fidorial.protocol.ServerboundPacket;

public record ServerboundSetCarriedItemPacket(int slot) implements ServerboundPacket {

    public static final NetworkDecoder<PacketBuffer, ServerboundSetCarriedItemPacket> DECODER = NetworkDecoder.composite(
            PrimitiveCodecs.SHORT,
            slot -> new ServerboundSetCarriedItemPacket(slot));

    @Override
    public void handle(final PacketListener listener) {
        ((PlayPacketListener) listener).handleSetCarriedItem(this);
    }
}
