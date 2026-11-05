package fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkDecoder;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.PlayPacketListener;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
import fr.fidorial.protocol.PacketListener;
import fr.fidorial.protocol.ServerboundPacket;

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#Acknowledge_Message
public record ServerboundChatAckPacket(int messageCount) implements ServerboundPacket {

    public static final NetworkDecoder<PacketBuffer, ServerboundChatAckPacket> DECODER = NetworkDecoder.composite(
            PrimitiveCodecs.VAR_INT,
            ServerboundChatAckPacket::new);

    @Override
    public void handle(final PacketListener listener) {
        ((PlayPacketListener) listener).handleChatAck(this);
    }
}
