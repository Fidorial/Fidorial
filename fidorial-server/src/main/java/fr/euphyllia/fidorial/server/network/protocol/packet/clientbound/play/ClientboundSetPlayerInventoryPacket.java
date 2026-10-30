package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.utils.ItemStackWriter;
import fr.euphyllia.fidorial.server.registry.ServerRegistries;
import fr.fidorial.item.ItemStack;
import net.kyori.adventure.key.Key;

/**
 * Replaces a slot of the player's own inventory on the client, whatever window is open.
 *
 * <p><a href="https://minecraft.wiki/w/Java_Edition_protocol/Packets#Set_Player_Inventory_Slot">Set Player Inventory Slot</a></p>
 *
 * @param slot   the inventory slot: {@code 0}-{@code 8} for the hotbar, {@code 9}-{@code 35} for the
 *               main inventory, then armor and off hand
 * @param stack  the stack to show in it
 * @param registries the registries the item and its components are numbered from
 */
public record ClientboundSetPlayerInventoryPacket(int slot, ItemStack stack, ServerRegistries registries)
        implements ClientboundPacket {

    @Override
    public Key name() {
        return PlayClientboundPackets.SET_PLAYER_INVENTORY;
    }

    @Override
    public void write(final PacketBuffer buf) {
        buf.writeVarInt(slot);
        ItemStackWriter.writeSlot(buf, stack, registries);
    }
}
