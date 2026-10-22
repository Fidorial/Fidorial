package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import net.kyori.adventure.key.Key;

/**
 * Selects a hotbar slot on the client.
 *
 * <p><a href="https://minecraft.wiki/w/Java_Edition_protocol/Packets#Set_Held_Item_(clientbound)">Set Held Item</a></p>
 *
 * @param slot the hotbar slot, {@code 0}-{@code 8}
 */
public record ClientboundSetHeldSlotPacket(int slot) implements ClientboundPacket {

    @Override
    public Key name() {
        return PlayClientboundPackets.SET_HELD_SLOT;
    }

    @Override
    public void write(final PacketBuffer buf) {
        buf.writeVarInt(slot);
    }
}
