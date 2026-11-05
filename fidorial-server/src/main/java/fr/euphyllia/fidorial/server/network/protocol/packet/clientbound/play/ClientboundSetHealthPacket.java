package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;

public record ClientboundSetHealthPacket(float health, int food, float saturation)
        implements ClientboundPacket<ClientboundSetHealthPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundSetHealthPacket> CODEC = NetworkEncoder
            .field(PrimitiveCodecs.FLOAT, ClientboundSetHealthPacket::health)
            .then(PrimitiveCodecs.VAR_INT, ClientboundSetHealthPacket::food)
            .then(PrimitiveCodecs.FLOAT, ClientboundSetHealthPacket::saturation);

    @Override
    public Key name() {
        return PlayClientboundPackets.SET_HEALTH;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundSetHealthPacket> codec() {
        return CODEC;
    }
}
