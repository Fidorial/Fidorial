package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.TextCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;

public record ClientboundDisconnectPacket(Component reason) implements ClientboundPacket<ClientboundDisconnectPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundDisconnectPacket> CODEC =
            NetworkEncoder.field(TextCodecs.COMPONENT, ClientboundDisconnectPacket::reason);

    @Override
    public Key name() {
        return PlayClientboundPackets.DISCONNECT;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundDisconnectPacket> codec() {
        return CODEC;
    }
}
