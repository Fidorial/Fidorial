package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;

public record ClientboundRemoveEntitiesPacket(int... entityIds) implements ClientboundPacket<ClientboundRemoveEntitiesPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundRemoveEntitiesPacket> CODEC =
            NetworkEncoder.field(PrimitiveCodecs.VAR_INT_ARRAY, ClientboundRemoveEntitiesPacket::entityIds);

    @Override
    public Key name() {
        return PlayClientboundPackets.REMOVE_ENTITIES;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundRemoveEntitiesPacket> codec() {
        return CODEC;
    }
}
