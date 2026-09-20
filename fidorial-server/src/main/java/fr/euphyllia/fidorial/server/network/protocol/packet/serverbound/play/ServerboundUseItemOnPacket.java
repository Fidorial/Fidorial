package fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.PlayPacketListener;
import fr.fidorial.protocol.PacketListener;
import fr.fidorial.protocol.ServerboundPacket;
import fr.fidorial.world.BlockPos;
import fr.fidorial.world.Vec3f;

public record ServerboundUseItemOnPacket(
        int hand,
        BlockPos target,
        int face,
        Vec3f cursor,
        boolean insideBlock,
        int sequence)
        implements ServerboundPacket {

    public static ServerboundUseItemOnPacket read(final PacketBuffer buf) {
        final int hand = buf.readVarInt();
        final BlockPos target = buf.readPosition();
        final int face = buf.readVarInt();
        final float cursorX = buf.readFloat();
        final float cursorY = buf.readFloat();
        final float cursorZ = buf.readFloat();
        final boolean insideBlock = buf.readBoolean();

        if (buf.readableBytes() > 0) {
            buf.readBoolean();
        }
        final int sequence = buf.readableBytes() > 0 ? buf.readVarInt() : 0;
        final Vec3f cursor3f = new Vec3f(cursorX, cursorY, cursorZ);
        return new ServerboundUseItemOnPacket(hand, target, face, cursor3f, insideBlock, sequence);
    }

    @Override
    public void handle(final PacketListener listener) {
        ((PlayPacketListener) listener).handleUseItemOn(this);
    }
}
