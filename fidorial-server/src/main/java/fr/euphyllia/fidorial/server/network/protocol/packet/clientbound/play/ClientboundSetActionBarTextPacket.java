package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.TextCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#Set_Action_Bar_Text
public record ClientboundSetActionBarTextPacket(Component message) implements ClientboundPacket<ClientboundSetActionBarTextPacket> {
    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundSetActionBarTextPacket> CODEC =
            NetworkEncoder.field(TextCodecs.COMPONENT, ClientboundSetActionBarTextPacket::message);

    @Override
    public Key name() {
        return PlayClientboundPackets.SET_ACTION_BAR_TEXT;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundSetActionBarTextPacket> codec() {
        return CODEC;
    }
}
