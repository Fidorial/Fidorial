package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.configuration;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.StringLikeCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.ConfigurationClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;

public record ClientboundBrandPacket(String brand) implements ClientboundPacket<ClientboundBrandPacket> {

    private static final Key BRAND_CHANNEL = Key.key("brand");

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundBrandPacket> CODEC = NetworkEncoder
            .constant(StringLikeCodecs.KEY, BRAND_CHANNEL)
            .then(StringLikeCodecs.STRING, ClientboundBrandPacket::brand);

    @Override
    public Key name() {
        return ConfigurationClientboundPackets.CUSTOM_PAYLOAD;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundBrandPacket> codec() {
        return CODEC;
    }
}
