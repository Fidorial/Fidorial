package fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.PlayPacketListener;
import fr.fidorial.protocol.PacketListener;
import fr.fidorial.protocol.ServerboundPacket;

import java.util.ArrayList;
import java.util.List;

public record ServerboundSetGameRulePacket(List<Entry> entries) implements ServerboundPacket {

    private static final int MAX_ENTRIES = 1024;
    private static final int MAX_ID_LENGTH = 32767;
    private static final int MAX_VALUE_LENGTH = 32767;

    public ServerboundSetGameRulePacket {
        entries = List.copyOf(entries);
    }

    public static ServerboundSetGameRulePacket read(final PacketBuffer buf) {
        final int size = buf.readVarInt();
        if (size < 0 || size > MAX_ENTRIES) {
            throw new IllegalArgumentException("Too many game rule entries: " + size);
        }
        final List<Entry> entries = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            entries.add(new Entry(buf.readString(MAX_ID_LENGTH), buf.readString(MAX_VALUE_LENGTH)));
        }
        return new ServerboundSetGameRulePacket(entries);
    }

    @Override
    public void handle(final PacketListener listener) {
        ((PlayPacketListener) listener).handleSetGameRule(this);
    }

    public record Entry(String rule, String value) {
    }
}
