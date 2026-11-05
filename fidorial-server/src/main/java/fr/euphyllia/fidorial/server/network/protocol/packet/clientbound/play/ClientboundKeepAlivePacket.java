package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#Keep_Alive_(clientbound)
public record ClientboundKeepAlivePacket(long id) implements ClientboundPacket<ClientboundKeepAlivePacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundKeepAlivePacket> CODEC =
            NetworkEncoder.field(PrimitiveCodecs.LONG, ClientboundKeepAlivePacket::id);

    @Override
    public Key name() {
        return PlayClientboundPackets.KEEP_ALIVE;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundKeepAlivePacket> codec() {
        return CODEC;
    }
}
