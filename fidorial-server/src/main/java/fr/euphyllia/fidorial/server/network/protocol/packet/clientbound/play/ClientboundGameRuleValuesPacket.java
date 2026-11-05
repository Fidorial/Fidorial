package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;

import java.util.LinkedHashMap;
import java.util.Map;

public record ClientboundGameRuleValuesPacket(Map<Key, String> values) implements ClientboundPacket<ClientboundGameRuleValuesPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundGameRuleValuesPacket> CODEC = NetworkEncoder.member(ClientboundGameRuleValuesPacket::encode);

    public ClientboundGameRuleValuesPacket {
        values = new LinkedHashMap<>(values);
    }

    @Override
    public Key name() {
        return PlayClientboundPackets.GAME_RULE_VALUES;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundGameRuleValuesPacket> codec() {
        return CODEC;
    }

    private void encode(final CodecAwarePacketBuffer buf) {
        buf.writeVarInt(values.size());
        for (final Map.Entry<Key, String> entry : values.entrySet()) {
            buf.writeKey(entry.getKey());
            buf.writeString(entry.getValue());
        }
    }
}
