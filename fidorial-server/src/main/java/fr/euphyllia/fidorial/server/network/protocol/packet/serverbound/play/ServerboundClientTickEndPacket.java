package fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.PlayPacketListener;
import fr.fidorial.protocol.PacketListener;
import fr.fidorial.protocol.ServerboundPacket;

public record ServerboundClientTickEndPacket() implements ServerboundPacket {

    public static ServerboundClientTickEndPacket read(final PacketBuffer buf) {
        return new ServerboundClientTickEndPacket();
    }

    @Override
    public void handle(final PacketListener listener) {
        ((PlayPacketListener) listener).handleClientTickEnd(this);
    }
}
