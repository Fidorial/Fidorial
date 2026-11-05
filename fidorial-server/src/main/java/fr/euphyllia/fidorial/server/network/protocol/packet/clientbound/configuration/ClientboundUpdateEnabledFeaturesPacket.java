package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.configuration;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.StringLikeCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.ConfigurationClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;

public record ClientboundUpdateEnabledFeaturesPacket(Key[] features) implements ClientboundPacket<ClientboundUpdateEnabledFeaturesPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundUpdateEnabledFeaturesPacket> CODEC =
            NetworkEncoder.field(StringLikeCodecs.KEY_ARRAY, ClientboundUpdateEnabledFeaturesPacket::features);

    @Override
    public Key name() {
        return ConfigurationClientboundPackets.UPDATE_ENABLED_FEATURES;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundUpdateEnabledFeaturesPacket> codec() {
        return CODEC;
    }
}
