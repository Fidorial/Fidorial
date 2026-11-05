package fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkDecoder;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.PlayPacketListener;
import fr.euphyllia.fidorial.server.network.protocol.packet.util.PositionData;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
import fr.fidorial.protocol.PacketListener;
import fr.fidorial.protocol.ServerboundPacket;

public record ServerboundMovePlayerPosPacket(PositionData.Vec3D position, int flags)
        implements ServerboundPacket {

    public static final NetworkDecoder<PacketBuffer, ServerboundMovePlayerPosPacket> DECODER = NetworkDecoder.composite(
            PositionData.Vec3D.CODEC,
            PrimitiveCodecs.UNSIGNED_BYTE,
            ServerboundMovePlayerPosPacket::new);

    @Override
    public void handle(final PacketListener listener) {
        ((PlayPacketListener) listener).handleMovePlayerPos(this);
    }
}
