package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.common;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.StringLikeCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.TextCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#Add_Resource_Pack
public record ClientboundResourcePackPushPacket(
        Key packetName,
        UUID id,
        String url,
        String hash,
        boolean forced,
        @Nullable Component promptMessage
) implements ClientboundPacket<ClientboundResourcePackPushPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundResourcePackPushPacket> CODEC = NetworkEncoder
            .field(PrimitiveCodecs.UUID, ClientboundResourcePackPushPacket::id)
            .then(StringLikeCodecs.STRING, ClientboundResourcePackPushPacket::url)
            .then(StringLikeCodecs.STRING, ClientboundResourcePackPushPacket::hash)
            .then(PrimitiveCodecs.BOOL, ClientboundResourcePackPushPacket::forced)
            .then(PrimitiveCodecs.prefixedNullable(TextCodecs.COMPONENT), ClientboundResourcePackPushPacket::promptMessage);

    @Override
    public Key name() {
        return packetName;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundResourcePackPushPacket> codec() {
        return CODEC;
    }
}
