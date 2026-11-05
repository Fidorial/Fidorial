package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
import net.kyori.adventure.key.Key;

import java.util.List;

public record ClientboundSetTimePacket(long worldAge, List<Clock> clocks) implements ClientboundPacket<ClientboundSetTimePacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundSetTimePacket> CODEC = NetworkEncoder
            .field(PrimitiveCodecs.LONG, ClientboundSetTimePacket::worldAge)
            .then(Clock.CODEC.listOf(PacketBuffer::writeVarInt), ClientboundSetTimePacket::clocks);

    public ClientboundSetTimePacket {
        clocks = List.copyOf(clocks);
    }

    public static ClientboundSetTimePacket single(
            final long worldAge,
            final int clockId,
            final long time,
            final float fractionalTime,
            final float rate
    ) {
        return new ClientboundSetTimePacket(worldAge, List.of(new Clock(clockId, time, fractionalTime, rate)));
    }

    @Override
    public Key name() {
        return PlayClientboundPackets.SET_TIME;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundSetTimePacket> codec() {
        return CODEC;
    }

    public record Clock(int clockId, long time, float fractionalTime, float rate) {

        public static final NetworkEncoder<PacketBuffer, Clock> CODEC = NetworkEncoder
                .field(PrimitiveCodecs.VAR_INT, Clock::clockId)
                .then(PrimitiveCodecs.VAR_LONG, Clock::time)
                .then(PrimitiveCodecs.FLOAT, Clock::fractionalTime)
                .then(PrimitiveCodecs.FLOAT, Clock::rate);
    }
}
