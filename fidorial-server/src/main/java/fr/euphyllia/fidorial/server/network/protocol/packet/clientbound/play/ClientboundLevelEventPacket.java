package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import fr.fidorial.math.Position;
import net.kyori.adventure.key.Key;

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#World_Event
public record ClientboundLevelEventPacket(int event, Position position, int data, boolean global)
        implements ClientboundPacket<ClientboundLevelEventPacket> {

    public static final int BLOCK_BREAK = 2001;
    /**
     * The particles of an enderman that teleports, shown between where it was and where it went:
     * the data packs where it went, as the difference with where it was, each axis in a byte
     * shifted by 127.
     */
    public static final int ENDERMAN_TELEPORT = 2018;

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundLevelEventPacket> CODEC = NetworkEncoder
            .field(PrimitiveCodecs.INT, ClientboundLevelEventPacket::event)
            .then(PrimitiveCodecs.BLOCK_POS, ClientboundLevelEventPacket::position)
            .then(PrimitiveCodecs.INT, ClientboundLevelEventPacket::data)
            .then(PrimitiveCodecs.BOOL, ClientboundLevelEventPacket::global);

    @Override
    public Key name() {
        return PlayClientboundPackets.LEVEL_EVENT;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundLevelEventPacket> codec() {
        return CODEC;
    }
}
