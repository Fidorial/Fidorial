package fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.PlayPacketListener;
import fr.fidorial.protocol.PacketListener;
import fr.fidorial.protocol.ServerboundPacket;

/**
 * The player used the item in one hand without aiming at a block.
 *
 * <p><a href="https://minecraft.wiki/w/Java_Edition_protocol/Packets#Use_Item">Use Item</a></p>
 *
 * @param hand     {@code 0} for the main hand, {@code 1} for the off hand
 * @param sequence the block change sequence to acknowledge
 * @param yaw      the yaw of the player
 * @param pitch    the pitch of the player
 */
public record ServerboundUseItemPacket(int hand, int sequence, float yaw, float pitch) implements ServerboundPacket {

    public static ServerboundUseItemPacket read(final PacketBuffer buf) {
        final int hand = buf.readVarInt();
        final int sequence = buf.readVarInt();
        final float yaw = buf.readFloat();
        final float pitch = buf.readFloat();
        return new ServerboundUseItemPacket(hand, sequence, yaw, pitch);
    }

    public boolean isOffHand() {
        return hand == 1;
    }

    @Override
    public void handle(final PacketListener listener) {
        ((PlayPacketListener) listener).handleUseItem(this);
    }
}
