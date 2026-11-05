package fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkDecoder;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.PlayPacketListener;
import fr.euphyllia.fidorial.server.network.protocol.packet.util.PositionData;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
import fr.fidorial.protocol.PacketListener;
import fr.fidorial.protocol.ServerboundPacket;

/**
 * Sent when the player right-clicks an entity.
 *
 * <p><a href="https://minecraft.wiki/w/Java_Edition_protocol/Packets#Interact">Interact</a></p>
 *
 * @param entityId the entity being used
 * @param hand     {@link #HAND_MAIN} or {@link #HAND_OFF}
 * @param relativePosition   the hit position relative to the target
 * @param sneaking whether the player was holding the sneak key, which suppresses most interactions
 */
public record ServerboundInteractPacket(
        int entityId, int hand, PositionData.VelocityVec3D relativePosition, boolean sneaking)
        implements ServerboundPacket {

    public static final int HAND_MAIN = 0;
    public static final int HAND_OFF = 1;

    public static final NetworkDecoder<PacketBuffer, ServerboundInteractPacket> DECODER = NetworkDecoder.composite(
            PrimitiveCodecs.VAR_INT,
            PrimitiveCodecs.VAR_INT,
            PositionData.VelocityVec3D.CODEC,
            PrimitiveCodecs.BOOL,
            ServerboundInteractPacket::new);

    public boolean isOffHand() {
        return hand == HAND_OFF;
    }

    @Override
    public void handle(final PacketListener listener) {
        ((PlayPacketListener) listener).handleInteract(this);
    }
}
