package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import fr.fidorial.math.BlockPosition;
import net.kyori.adventure.key.Key;

/**
 * Non-persistent block actions and animations (chest lid, note block, piston, ...).
 *
 * <p>Format:
 *
 * <pre>
 *   Location         Position
 *   Action ID        Unsigned Byte
 *   Action Parameter Unsigned Byte
 *   Block Type       VarInt         (block registry ID, ignored by the vanilla client)
 * </pre>
 *
 * <p>For chests: Action ID 1, parameter = number of players with the chest open (0 closes the lid).
 *
 * <p>https://minecraft.wiki/w/Java_Edition_protocol/Packets#Block_Action
 */
public record ClientboundBlockEventPacket(BlockPosition pos, int actionId, int actionParam, int blockTypeId)
        implements ClientboundPacket<ClientboundBlockEventPacket> {

    public static final int CHEST_VIEWER_COUNT = 1;

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundBlockEventPacket> CODEC = NetworkEncoder
            .field(PrimitiveCodecs.BLOCK_POS, ClientboundBlockEventPacket::pos)
            .then(PrimitiveCodecs.UNSIGNED_BYTE, ClientboundBlockEventPacket::actionId)
            .then(PrimitiveCodecs.UNSIGNED_BYTE, ClientboundBlockEventPacket::actionParam)
            .then(PrimitiveCodecs.VAR_INT, ClientboundBlockEventPacket::blockTypeId);

    public static ClientboundBlockEventPacket chestViewers(final BlockPosition pos, final int viewers) {
        return new ClientboundBlockEventPacket(pos, CHEST_VIEWER_COUNT, viewers, 0);
    }

    @Override
    public Key name() {
        return PlayClientboundPackets.BLOCK_EVENT;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundBlockEventPacket> codec() {
        return CODEC;
    }
}
