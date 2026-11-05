package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.configuration;

import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.ConfigurationClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#Reset_Chat
public record ClientboundResetChatPacket() implements ClientboundPacket<ClientboundResetChatPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundResetChatPacket> CODEC = (_, _) -> {};

    @Override
    public Key name() {
        return ConfigurationClientboundPackets.RESET_CHAT;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundResetChatPacket> codec() {
        return CODEC;
    }
}
