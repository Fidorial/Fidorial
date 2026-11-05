package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#Clear_Titles
public record ClientboundClearTitlesPacket(boolean reset) implements ClientboundPacket<ClientboundClearTitlesPacket> {
    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundClearTitlesPacket> CODEC =
            NetworkEncoder.field(PrimitiveCodecs.BOOL, ClientboundClearTitlesPacket::reset);

    @Override
    public Key name() {
        return PlayClientboundPackets.CLEAR_TITLES;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundClearTitlesPacket> codec() {
        return CODEC;
    }
}
