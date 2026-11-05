package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.configuration;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.StringLikeCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.ConfigurationClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;

/**
 * @see <a href="https://minecraft.wiki/w/Java_Edition_protocol/Packets#Code_of_Conduct">Code of Conduct</a>
 */
public record ClientboundCodeOfConductPacket(String contents) implements ClientboundPacket<ClientboundCodeOfConductPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundCodeOfConductPacket> CODEC =
            NetworkEncoder.field(StringLikeCodecs.STRING, ClientboundCodeOfConductPacket::contents);

    @Override
    public Key name() {
        return ConfigurationClientboundPackets.CODE_OF_CONDUCT;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundCodeOfConductPacket> codec() {
        return CODEC;
    }
}
