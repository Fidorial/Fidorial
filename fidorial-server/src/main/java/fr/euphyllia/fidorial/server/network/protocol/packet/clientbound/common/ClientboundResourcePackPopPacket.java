package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.common;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#Remove_Resource_Pack
public record ClientboundResourcePackPopPacket(Key packetName, @Nullable UUID id) implements ClientboundPacket<ClientboundResourcePackPopPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundResourcePackPopPacket> CODEC =
            NetworkEncoder.field(PrimitiveCodecs.prefixedNullable(PrimitiveCodecs.UUID), ClientboundResourcePackPopPacket::id);

    @Override
    public Key name() {
        return packetName;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundResourcePackPopPacket> codec() {
        return CODEC;
    }
}
