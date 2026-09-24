package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import net.kyori.adventure.key.Key;

import java.util.LinkedHashMap;
import java.util.Map;

public record ClientboundGameRuleValuesPacket(Map<Key, String> values) implements ClientboundPacket {

    public ClientboundGameRuleValuesPacket {
        values = new LinkedHashMap<>(values);
    }

    @Override
    public Key name() {
        return PlayClientboundPackets.GAME_RULE_VALUES;
    }

    @Override
    public void write(final PacketBuffer buf) {
        buf.writeVarInt(values.size());
        for (final Map.Entry<Key, String> entry : values.entrySet()) {
            buf.writeKey(entry.getKey());
            buf.writeString(entry.getValue());
        }
    }
}
