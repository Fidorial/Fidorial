package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#Debug_Sample
public record ClientboundDebugSamplePacket(long fullTick, long tick, long tasks, long idle) implements ClientboundPacket<ClientboundDebugSamplePacket> {

    private static final int TICK_TIME = 0;

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundDebugSamplePacket> CODEC = NetworkEncoder.member(ClientboundDebugSamplePacket::encode);

    @Override
    public Key name() {
        return PlayClientboundPackets.DEBUG_SAMPLE;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundDebugSamplePacket> codec() {
        return CODEC;
    }

    private void encode(final CodecAwarePacketBuffer buf) {
        buf.writeLongArray(new long[]{fullTick, tick, tasks, idle});
        buf.writeVarInt(TICK_TIME);
    }
}
