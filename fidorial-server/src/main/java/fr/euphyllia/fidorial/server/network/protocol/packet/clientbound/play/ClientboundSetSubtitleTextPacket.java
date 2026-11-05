package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.TextCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#Set_Subtitle_Text
public record ClientboundSetSubtitleTextPacket(Component message) implements ClientboundPacket<ClientboundSetSubtitleTextPacket> {
    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundSetSubtitleTextPacket> CODEC =
            NetworkEncoder.field(TextCodecs.COMPONENT, ClientboundSetSubtitleTextPacket::message);

    @Override
    public Key name() {
        return PlayClientboundPackets.SET_SUBTITLE_TEXT;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundSetSubtitleTextPacket> codec() {
        return CODEC;
    }
}
