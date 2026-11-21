package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.util.ItemStackWriter;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import fr.fidorial.inventory.EquipmentSlot;
import fr.fidorial.item.ItemStack;
import net.kyori.adventure.key.Key;

import java.util.Map;

/**
 * Shows what an entity wears or holds, slot by slot.
 *
 * <p>Format:
 *
 * <pre>
 *   Entity ID   VarInt
 *   Equipment   (Slot Byte, Item Slot)... the top bit of the slot set while more follow
 * </pre>
 *
 * <p><a href="https://minecraft.wiki/w/Java_Edition_protocol/Packets#Set_Equipment">Packets Set_Equipment</a>
 */
public record ClientboundSetEquipmentPacket(int entityId, Map<EquipmentSlot, ItemStack> equipment)
        implements ClientboundPacket<ClientboundSetEquipmentPacket> {

    private static final int MORE_FOLLOW = 0x80;

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundSetEquipmentPacket> CODEC =
            NetworkEncoder.member(ClientboundSetEquipmentPacket::encode);

    public ClientboundSetEquipmentPacket {
        if (equipment.isEmpty()) {
            throw new IllegalArgumentException("The equipment shown must hold one slot at least");
        }
        equipment = Map.copyOf(equipment);
    }

    /**
     * {@return a packet showing what a single slot holds}
     */
    public static ClientboundSetEquipmentPacket of(final int entityId, final EquipmentSlot slot, final ItemStack stack) {
        return new ClientboundSetEquipmentPacket(entityId, Map.of(slot, stack));
    }

    @Override
    public Key name() {
        return PlayClientboundPackets.SET_EQUIPMENT;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundSetEquipmentPacket> codec() {
        return CODEC;
    }

    private void encode(final CodecAwarePacketBuffer buf) {
        buf.writeVarInt(entityId);
        int left = equipment.size();
        // the slots in their order, the client reading them as they come
        for (final EquipmentSlot slot : EquipmentSlot.values()) {
            final ItemStack stack = equipment.get(slot);
            if (stack == null) {
                continue;
            }
            left--;
            buf.writeByte(left > 0 ? slot.ordinal() | MORE_FOLLOW : slot.ordinal());
            ItemStackWriter.SLOT.write(buf, stack);
        }
    }
}
