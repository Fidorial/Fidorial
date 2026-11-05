package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;

public record ClientboundEntityEventPacket(int entityId, byte eventId) implements ClientboundPacket<ClientboundEntityEventPacket> {

    /**
     * Sent to a player about itself: hides coordinates and other details from the debug screen.
     */
    public static final byte ENABLE_REDUCED_DEBUG_INFO = 22;

    /**
     * Sent to a player about itself: shows the full debug screen again.
     */
    public static final byte DISABLE_REDUCED_DEBUG_INFO = 23;

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundEntityEventPacket> CODEC = NetworkEncoder
            .field(PrimitiveCodecs.INT, ClientboundEntityEventPacket::entityId)
            .then(PrimitiveCodecs.BYTE, ClientboundEntityEventPacket::eventId);

    /**
     * @param player       the entity id of the player receiving the packet
     * @param reducedDebug the value of the {@code minecraft:reduced_debug_info} game rule
     * @return the packet toggling the debug screen of that player
     */
    public static ClientboundEntityEventPacket reducedDebugInfo(final int player, final boolean reducedDebug) {
        return new ClientboundEntityEventPacket(
                player, reducedDebug ? ENABLE_REDUCED_DEBUG_INFO : DISABLE_REDUCED_DEBUG_INFO);
    }

    @Override
    public Key name() {
        return PlayClientboundPackets.ENTITY_EVENT;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundEntityEventPacket> codec() {
        return CODEC;
    }
}
