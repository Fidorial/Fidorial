package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.login;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.LoginClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;


public record ClientboundSetupCompressionPacket(int threshold) implements ClientboundPacket<ClientboundSetupCompressionPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundSetupCompressionPacket> CODEC =
            NetworkEncoder.field(PrimitiveCodecs.VAR_INT, ClientboundSetupCompressionPacket::threshold);

    @Override
    public Key name() {
        return LoginClientboundPackets.SETUP_COMPRESSION;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundSetupCompressionPacket> codec() {
        return CODEC;
    }
}
