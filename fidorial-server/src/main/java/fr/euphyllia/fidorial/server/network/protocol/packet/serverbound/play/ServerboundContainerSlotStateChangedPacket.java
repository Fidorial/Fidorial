package fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkDecoder;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.PlayPacketListener;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
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

    public static final NetworkDecoder<PacketBuffer, ServerboundContainerSlotStateChangedPacket> DECODER = NetworkDecoder.composite(
            PrimitiveCodecs.VAR_INT,
            PrimitiveCodecs.VAR_INT,
            PrimitiveCodecs.BOOL,
            ServerboundContainerSlotStateChangedPacket::new);

    @Override
    public void handle(final PacketListener listener) {
        ((PlayPacketListener) listener).handleContainerSlotStateChanged(this);
    }
}
