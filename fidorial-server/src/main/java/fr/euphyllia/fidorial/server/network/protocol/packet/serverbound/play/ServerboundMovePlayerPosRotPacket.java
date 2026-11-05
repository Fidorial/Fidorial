package fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkDecoder;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.PlayPacketListener;
import fr.euphyllia.fidorial.server.network.protocol.packet.util.PositionData;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
import fr.fidorial.protocol.PacketListener;
import fr.fidorial.protocol.ServerboundPacket;

public record ServerboundMovePlayerPosRotPacket(PositionData.Vec3D position, PositionData.FloatRotation rotation, int flags)
        implements ServerboundPacket {

    public static final NetworkDecoder<PacketBuffer, ServerboundMovePlayerPosRotPacket> DECODER = NetworkDecoder.composite(
            PositionData.Vec3D.CODEC,
            PositionData.FloatRotation.CODEC,
            PrimitiveCodecs.UNSIGNED_BYTE,
            ServerboundMovePlayerPosRotPacket::new);

    @Override
    public void handle(final PacketListener listener) {
        ((PlayPacketListener) listener).handleMovePlayerPosRot(this);
    }
}
