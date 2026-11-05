package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#Set_Ticking_State
public record ClientboundTickingStatePacket(float tickRate, boolean frozen) implements ClientboundPacket<ClientboundTickingStatePacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundTickingStatePacket> CODEC = NetworkEncoder
            .field(PrimitiveCodecs.FLOAT, ClientboundTickingStatePacket::tickRate)
            .then(PrimitiveCodecs.BOOL, ClientboundTickingStatePacket::frozen);

    @Override
    public Key name() {
        return PlayClientboundPackets.TICKING_STATE;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundTickingStatePacket> codec() {
        return CODEC;
    }
}
