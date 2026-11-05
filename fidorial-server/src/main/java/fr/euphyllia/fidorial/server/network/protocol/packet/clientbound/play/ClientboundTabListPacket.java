package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.TextCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#Set_Tab_List_Header_And_Footer
public record ClientboundTabListPacket(Component header, Component footer) implements ClientboundPacket<ClientboundTabListPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundTabListPacket> CODEC = NetworkEncoder
            .field(TextCodecs.COMPONENT, ClientboundTabListPacket::header)
            .then(TextCodecs.COMPONENT, ClientboundTabListPacket::footer);

    @Override
    public Key name() {
        return PlayClientboundPackets.TAB_LIST;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundTabListPacket> codec() {
        return CODEC;
    }
}
