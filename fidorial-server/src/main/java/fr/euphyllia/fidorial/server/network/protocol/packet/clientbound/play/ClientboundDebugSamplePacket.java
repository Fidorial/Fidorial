package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import net.kyori.adventure.key.Key;

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#Debug_Sample
public record ClientboundDebugSamplePacket(long fullTick, long tick, long tasks, long idle) implements ClientboundPacket {

    private static final int TICK_TIME = 0;

    @Override
    public Key name() {
        return PlayClientboundPackets.DEBUG_SAMPLE;
    }

    @Override
    public void write(final PacketBuffer buf) {
        buf.writeLongArray(new long[]{fullTick, tick, tasks, idle});
        buf.writeVarInt(TICK_TIME);
    }
}
