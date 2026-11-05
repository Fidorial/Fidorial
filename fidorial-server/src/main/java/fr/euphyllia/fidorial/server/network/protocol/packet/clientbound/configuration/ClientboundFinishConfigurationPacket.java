package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.configuration;

import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.ConfigurationClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;

public record ClientboundFinishConfigurationPacket() implements ClientboundPacket<ClientboundFinishConfigurationPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundFinishConfigurationPacket> CODEC = (_, _) -> {};

    @Override
    public Key name() {
        return ConfigurationClientboundPackets.FINISH_CONFIGURATION;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundFinishConfigurationPacket> codec() {
        return CODEC;
    }
}
