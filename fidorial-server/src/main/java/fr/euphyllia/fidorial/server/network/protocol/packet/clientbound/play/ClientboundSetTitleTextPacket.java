package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.TextCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#Set_Title_Text
public record ClientboundSetTitleTextPacket(Component message) implements ClientboundPacket<ClientboundSetTitleTextPacket> {
    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundSetTitleTextPacket> CODEC =
            NetworkEncoder.field(TextCodecs.COMPONENT, ClientboundSetTitleTextPacket::message);

    @Override
    public Key name() {
        return PlayClientboundPackets.SET_TITLE_TEXT;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundSetTitleTextPacket> codec() {
        return CODEC;
    }
}
