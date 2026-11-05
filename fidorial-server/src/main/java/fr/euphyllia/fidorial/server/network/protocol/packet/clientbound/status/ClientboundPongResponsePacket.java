package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.status;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.StatusClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;

public record ClientboundPongResponsePacket(long payload) implements ClientboundPacket<ClientboundPongResponsePacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundPongResponsePacket> CODEC =
            NetworkEncoder.field(PrimitiveCodecs.LONG, ClientboundPongResponsePacket::payload);

    @Override
    public Key name() {
        return StatusClientboundPackets.PONG_RESPONSE;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundPongResponsePacket> codec() {
        return CODEC;
    }
}
