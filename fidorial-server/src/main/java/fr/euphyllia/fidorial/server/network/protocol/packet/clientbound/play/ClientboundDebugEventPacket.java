package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.debug.DebugChannel;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;

import java.util.Objects;

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#Debug_Event
public record ClientboundDebugEventPacket<T>(DebugChannel<T> channel, T value) implements ClientboundPacket<ClientboundDebugEventPacket<?>> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundDebugEventPacket<?>> CODEC = ClientboundDebugEventPacket::encode;

    public ClientboundDebugEventPacket {
        if (channel.scope() != DebugChannel.Scope.EVENT) {
            throw new IllegalArgumentException(channel.key().key().asString() + " is not an event channel");
        }
        Objects.requireNonNull(value, "value");
    }

    @Override
    public Key name() {
        return PlayClientboundPackets.DEBUG_EVENT;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundDebugEventPacket<?>> codec() {
        return CODEC;
    }

    private static <T> void encode(final CodecAwarePacketBuffer buf, final ClientboundDebugEventPacket<T> packet) {
        buf.writeVarInt(packet.channel().networkId());
        packet.channel().valueCodec().write(buf, packet.value());
    }
}
