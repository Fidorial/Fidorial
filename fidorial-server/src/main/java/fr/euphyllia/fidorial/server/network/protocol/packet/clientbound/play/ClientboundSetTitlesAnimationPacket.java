package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#Set_Title_Animation_Times
public record ClientboundSetTitlesAnimationPacket(int fadeIn, int stay, int fadeOut) implements ClientboundPacket<ClientboundSetTitlesAnimationPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundSetTitlesAnimationPacket> CODEC = NetworkEncoder
            .field(PrimitiveCodecs.INT, ClientboundSetTitlesAnimationPacket::fadeIn)
            .then(PrimitiveCodecs.INT, ClientboundSetTitlesAnimationPacket::stay)
            .then(PrimitiveCodecs.INT, ClientboundSetTitlesAnimationPacket::fadeOut);

    @Override
    public Key name() {
        return PlayClientboundPackets.SET_TITLES_ANIMATION;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundSetTitlesAnimationPacket> codec() {
        return CODEC;
    }
}
