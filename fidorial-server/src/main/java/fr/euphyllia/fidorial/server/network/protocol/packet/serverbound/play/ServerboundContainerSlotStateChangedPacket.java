package fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.PlayPacketListener;
import fr.fidorial.protocol.PacketListener;
import fr.fidorial.protocol.ServerboundPacket;

/**
 * A player clicking an empty slot of a crafter to disable it, or a disabled one to enable it again.
 *
 * <p>Format:
 *
 * <pre>
 *   Slot ID     VarInt
 *   Window ID   VarInt
 *   State       Boolean   ({@code true}: the slot is enabled)
 * </pre>
 *
 * <p><a href="https://minecraft.wiki/w/Java_Edition_protocol/Packets#Change_Container_Slot_State">Packets#Change_Container_Slot_State</a>
 */
public record ServerboundContainerSlotStateChangedPacket(int slot, int windowId, boolean enabled)
        implements ServerboundPacket {

    public static ServerboundContainerSlotStateChangedPacket read(final PacketBuffer buf) {
        final int slot = buf.readVarInt();
        final int windowId = buf.readVarInt();
        final boolean enabled = buf.readBoolean();
        return new ServerboundContainerSlotStateChangedPacket(slot, windowId, enabled);
    }

    @Override
    public void handle(final PacketListener listener) {
        ((PlayPacketListener) listener).handleContainerSlotStateChanged(this);
    }
}
