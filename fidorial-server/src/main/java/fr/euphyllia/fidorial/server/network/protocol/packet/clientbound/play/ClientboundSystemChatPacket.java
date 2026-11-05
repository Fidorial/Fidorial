package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.TextCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;

public record ClientboundSystemChatPacket(Component message, boolean overlay)
        implements ClientboundPacket<ClientboundSystemChatPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundSystemChatPacket> CODEC = NetworkEncoder
            .field(TextCodecs.COMPONENT, ClientboundSystemChatPacket::message)
            .then(PrimitiveCodecs.BOOL, ClientboundSystemChatPacket::overlay);

    @Override
    public Key name() {
        return PlayClientboundPackets.SYSTEM_CHAT;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundSystemChatPacket> codec() {
        return CODEC;
    }
}
