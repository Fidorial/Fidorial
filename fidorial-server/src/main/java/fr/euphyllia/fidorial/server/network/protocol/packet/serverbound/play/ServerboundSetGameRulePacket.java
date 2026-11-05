package fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.StringLikeCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkDecoder;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.PlayPacketListener;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
import fr.fidorial.protocol.PacketListener;
import fr.fidorial.protocol.ServerboundPacket;

import java.util.List;

public record ServerboundSetGameRulePacket(List<Entry> entries) implements ServerboundPacket {

    private static final int MAX_ENTRIES = 1024;
    private static final int MAX_ID_LENGTH = 32767;
    private static final int MAX_VALUE_LENGTH = 32767;

    public static final NetworkDecoder<PacketBuffer, ServerboundSetGameRulePacket> DECODER = NetworkDecoder.composite(
            Entry.DECODER.listOf(PacketBuffer::readVarInt, MAX_ENTRIES),
            ServerboundSetGameRulePacket::new);

    public ServerboundSetGameRulePacket {
        entries = List.copyOf(entries);
    }

    @Override
    public void handle(final PacketListener listener) {
        ((PlayPacketListener) listener).handleSetGameRule(this);
    }

    public record Entry(String rule, String value) {

        public static final NetworkDecoder<PacketBuffer, Entry> DECODER = NetworkDecoder.composite(
                StringLikeCodecs.string(MAX_ID_LENGTH),
                StringLikeCodecs.string(MAX_VALUE_LENGTH),
                Entry::new);
    }
}
