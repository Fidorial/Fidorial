package fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkDecoder;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.PlayPacketListener;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
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

    public static final NetworkDecoder<PacketBuffer, ServerboundUseItemPacket> DECODER = NetworkDecoder.composite(
            PrimitiveCodecs.VAR_INT,
            PrimitiveCodecs.VAR_INT,
            PrimitiveCodecs.FLOAT,
            PrimitiveCodecs.FLOAT,
            ServerboundUseItemPacket::new);

    public boolean isOffHand() {
        return hand == 1;
    }

    @Override
    public void handle(final PacketListener listener) {
        ((PlayPacketListener) listener).handleUseItem(this);
    }
}
