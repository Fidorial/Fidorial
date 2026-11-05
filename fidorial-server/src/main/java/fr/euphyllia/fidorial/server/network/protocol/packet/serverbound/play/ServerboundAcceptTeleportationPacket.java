package fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkDecoder;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.PlayPacketListener;
import fr.euphyllia.fidorial.server.network.protocol.packet.util.PositionData;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
import fr.fidorial.protocol.PacketListener;
import fr.fidorial.protocol.ServerboundPacket;

public record ServerboundAcceptTeleportationPacket(int teleportId, PositionData.Vec3D position, PositionData.FloatRotation rotation) implements ServerboundPacket {

    public static final NetworkDecoder<PacketBuffer, ServerboundAcceptTeleportationPacket> DECODER = NetworkDecoder.composite(
            PrimitiveCodecs.VAR_INT,
            PositionData.Vec3D.CODEC,
            PositionData.FloatRotation.CODEC,
            ServerboundAcceptTeleportationPacket::new);

    @Override
    public void handle(final PacketListener listener) {
        ((PlayPacketListener) listener).handleAcceptTeleportation(this);
    }
}
