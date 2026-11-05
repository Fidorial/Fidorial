package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.util.ItemStackWriter;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
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
 */
public record ClientboundSetPlayerInventoryPacket(int slot, ItemStack stack)
        implements ClientboundPacket<ClientboundSetPlayerInventoryPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundSetPlayerInventoryPacket> CODEC = NetworkEncoder
            .field(PrimitiveCodecs.VAR_INT, ClientboundSetPlayerInventoryPacket::slot)
            .then(ItemStackWriter.SLOT, ClientboundSetPlayerInventoryPacket::stack);

    @Override
    public Key name() {
        return PlayClientboundPackets.SET_PLAYER_INVENTORY;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundSetPlayerInventoryPacket> codec() {
        return CODEC;
    }
}
