package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.login;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.StringLikeCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.LoginClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

public record ClientboundLoginDisconnectPacket(Component reason) implements ClientboundPacket<ClientboundLoginDisconnectPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundLoginDisconnectPacket> CODEC =
            NetworkEncoder.field(StringLikeCodecs.STRING, packet -> GsonComponentSerializer.gson().serialize(packet.reason()));

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
