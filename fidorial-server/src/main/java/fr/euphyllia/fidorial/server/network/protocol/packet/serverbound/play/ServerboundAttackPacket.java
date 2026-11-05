package fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkDecoder;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.PlayPacketListener;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
import fr.fidorial.protocol.PacketListener;
import fr.fidorial.protocol.ServerboundPacket;

/**
 * Sent when the player left-clicks an entity.
 *
 * <p><a href="https://minecraft.wiki/w/Java_Edition_protocol/Packets#Attack">Attack</a></p>
 */
public record ServerboundAttackPacket(int entityId) implements ServerboundPacket {

    public static final NetworkDecoder<PacketBuffer, ServerboundAttackPacket> DECODER = NetworkDecoder.composite(
            PrimitiveCodecs.VAR_INT,
            ServerboundAttackPacket::new);

    @Override
    public void handle(final PacketListener listener) {
        ((PlayPacketListener) listener).handleAttack(this);
    }
}
