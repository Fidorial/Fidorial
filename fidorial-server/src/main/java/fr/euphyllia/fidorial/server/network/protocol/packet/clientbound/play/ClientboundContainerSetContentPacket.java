package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.utils.ItemStackWriter;
import fr.euphyllia.fidorial.server.registry.RegistryHolder;
import fr.fidorial.item.ItemStack;
import net.kyori.adventure.key.Key;

import java.util.List;

/**
 * Fully replaces the contents of a window.
 *
 * <p>Format:
 *
 * <pre>
 *   Window ID     VarInt
 *   State ID      VarInt
 *   Slots         Prefixed Array of Slot
 *   Carried item  Slot
 * </pre>
 *
 * <p>Also resent to resynchronize the client after each click, mirroring what the vanilla server
 * does when a State ID does not match.
 *
 * <p><a href="https://minecraft.wiki/w/Java_Edition_protocol/Packets#Set_Container_Content">Packets#Set_Container_Content</a>
 */
public record ClientboundContainerSetContentPacket(
        int windowId, int stateId, List<ItemStack> slots, ItemStack carried, RegistryHolder frozen)
        implements ClientboundPacket {

    public ClientboundContainerSetContentPacket {
        slots = List.copyOf(slots);
    }

    @Override
    public Key name() {
        return PlayClientboundPackets.CONTAINER_SET_CONTENT;
    }

    @Override
    public void write(final PacketBuffer buf) {
        buf.writeVarInt(windowId); // Window ID
        buf.writeVarInt(stateId); // State ID
        buf.writeVarInt(slots.size()); // slot count
        for (final ItemStack stack : slots) {
            ItemStackWriter.writeSlot(buf, stack, frozen);
        }
        ItemStackWriter.writeSlot(buf, carried, frozen); // Carried item (cursor)
    }
}
