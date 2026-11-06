package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.login;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.TextCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.LoginClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;

public record ClientboundLoginDisconnectPacket(Component reason) implements ClientboundPacket<ClientboundLoginDisconnectPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundLoginDisconnectPacket> CODEC =
            NetworkEncoder.field(TextCodecs.COMPONENT_JSON, ClientboundLoginDisconnectPacket::reason);

    public static ClientboundLoginDisconnectPacket ofComponent(final Component reason) {
        return new ClientboundLoginDisconnectPacket(reason);
    }

    @Override
    public Key name() {
        return LoginClientboundPackets.LOGIN_DISCONNECT;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundLoginDisconnectPacket> codec() {
        return CODEC;
    }
}
