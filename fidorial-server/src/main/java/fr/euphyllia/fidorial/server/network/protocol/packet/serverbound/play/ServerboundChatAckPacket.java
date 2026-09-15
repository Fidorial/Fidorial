package fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.PlayPacketListener;
import fr.fidorial.protocol.PacketListener;
import fr.fidorial.protocol.ServerboundPacket;

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#Acknowledge_Message
public record ServerboundChatAckPacket(int messageCount) implements ServerboundPacket {

    public static ServerboundChatAckPacket read(final PacketBuffer buf) {
        return new ServerboundChatAckPacket(buf.readVarInt());
    }

    @Override
    public void handle(final PacketListener listener) {
        ((PlayPacketListener) listener).handleChatAck(this);
    }
}
