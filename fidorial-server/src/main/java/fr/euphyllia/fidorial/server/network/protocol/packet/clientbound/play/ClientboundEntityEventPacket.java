package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import net.kyori.adventure.key.Key;

public record ClientboundEntityEventPacket(int entityId, byte eventId) implements ClientboundPacket {

    /**
     * Sent to a player about itself: hides coordinates and other details from the debug screen.
     */
    public static final byte ENABLE_REDUCED_DEBUG_INFO = 22;

    /**
     * Sent to a player about itself: shows the full debug screen again.
     */
    public static final byte DISABLE_REDUCED_DEBUG_INFO = 23;

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
    public void write(final PacketBuffer buf) {
        buf.writeInt(entityId);
        buf.writeByte(eventId);
    }
}
