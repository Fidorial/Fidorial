package fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.PlayPacketListener;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.protocol.PacketListener;
import fr.fidorial.protocol.ServerboundPacket;

public record ServerboundUseItemOnPacket(
        int hand,
        BlockPosition target,
        int face,
        float cursorX,
        float cursorY,
        float cursorZ,
        boolean insideBlock,
        int sequence)
        implements ServerboundPacket {

    public static ServerboundUseItemOnPacket read(final PacketBuffer buf) {
        final int hand = buf.readVarInt();
        final BlockPosition target = buf.readPosition();
        final int face = buf.readVarInt();
        final float cursorX = buf.readFloat();
        final float cursorY = buf.readFloat();
        final float cursorZ = buf.readFloat();
        final boolean insideBlock = buf.readBoolean();

        if (buf.readableBytes() > 0) {
            buf.readBoolean();
        }
        final int sequence = buf.readableBytes() > 0 ? buf.readVarInt() : 0;
        return new ServerboundUseItemOnPacket(hand, target, face, cursorX, cursorY, cursorZ, insideBlock, sequence);
    }

    @Override
    public void handle(final PacketListener listener) {
        ((PlayPacketListener) listener).handleUseItemOn(this);
    }
}
