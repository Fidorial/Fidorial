package fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkDecoder;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.PlayPacketListener;
import fr.euphyllia.fidorial.server.network.protocol.packet.util.PositionData;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
import fr.fidorial.protocol.PacketListener;
import fr.fidorial.protocol.ServerboundPacket;

public record ServerboundMovePlayerRotPacket(PositionData.FloatRotation rotation, int flags)
        implements ServerboundPacket {

    public static final NetworkDecoder<PacketBuffer, ServerboundMovePlayerRotPacket> DECODER = NetworkDecoder.composite(
            PositionData.FloatRotation.CODEC,
            PrimitiveCodecs.UNSIGNED_BYTE,
            ServerboundMovePlayerRotPacket::new);

    @Override
    public void handle(final PacketListener listener) {
        ((PlayPacketListener) listener).handleMovePlayerRot(this);
    }
}
