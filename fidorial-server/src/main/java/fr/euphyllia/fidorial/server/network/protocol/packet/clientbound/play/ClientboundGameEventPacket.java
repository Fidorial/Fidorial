package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#Game_Event
public record ClientboundGameEventPacket(int event, float value) implements ClientboundPacket<ClientboundGameEventPacket> {

    public static final int NO_RESPAWN_BLOCK_AVAILABLE = 0;
    public static final int END_RAINING = 1;
    public static final int BEGIN_RAINING = 2;
    public static final int CHANGE_GAME_MODE = 3;
    public static final int RAIN_LEVEL_CHANGE = 7;
    public static final int THUNDER_LEVEL_CHANGE = 8;
    public static final int IMMEDIATE_RESPAWN = 11;
    public static final int LIMITED_CRAFTING = 12;
    public static final int START_WAITING_FOR_CHUNKS = 13;

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundGameEventPacket> CODEC = NetworkEncoder
            .field(PrimitiveCodecs.UNSIGNED_BYTE, ClientboundGameEventPacket::event)
            .then(PrimitiveCodecs.FLOAT, ClientboundGameEventPacket::value);

    @Override
    public Key name() {
        return PlayClientboundPackets.GAME_EVENT;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundGameEventPacket> codec() {
        return CODEC;
    }
}
