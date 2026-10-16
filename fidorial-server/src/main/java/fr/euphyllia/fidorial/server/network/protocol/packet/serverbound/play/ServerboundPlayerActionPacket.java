package fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.PlayPacketListener;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.protocol.PacketListener;
import fr.fidorial.protocol.ServerboundPacket;

public record ServerboundPlayerActionPacket(int status, BlockPosition position, int face, int sequence)
        implements ServerboundPacket {

    public static final int START_DESTROY_BLOCK = 0;
    public static final int ABORT_DESTROY_BLOCK = 1;
    public static final int FINISH_DESTROY_BLOCK = 2;

    public static ServerboundPlayerActionPacket read(final PacketBuffer buf) {
        final int status = buf.readVarInt();
        final BlockPosition position = buf.readPosition();
        final int face = buf.readUByte();
        final int sequence = buf.readVarInt();
        return new ServerboundPlayerActionPacket(status, position, face, sequence);
    }

    @Override
    public void handle(final PacketListener listener) {
        ((PlayPacketListener) listener).handlePlayerAction(this);
    }
}
