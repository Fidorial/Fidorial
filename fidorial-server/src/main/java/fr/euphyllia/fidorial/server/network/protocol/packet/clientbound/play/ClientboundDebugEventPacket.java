package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.debug.DebugChannel;
import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import net.kyori.adventure.key.Key;

import java.util.Objects;

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#Debug_Event
public record ClientboundDebugEventPacket<T>(DebugChannel<T> channel, T value) implements ClientboundPacket {

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
    public void write(final PacketBuffer buf) {
        buf.writeVarInt(channel.networkId());
        channel.valueCodec().write(buf, value);
    }
}
