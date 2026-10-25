package fr.euphyllia.fidorial.server.codecs.container;

import com.mojang.serialization.Codec;
import fr.euphyllia.fidorial.server.VersionConstants;
import fr.euphyllia.fidorial.server.world.storage.datafixers.DataFixerType;
import fr.fidorial.inventory.EquipmentSlot;
import fr.fidorial.inventory.PlayerInventory;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public final class PlayerInventoryCodec {

    private static final String ROOT_NAME = "";
    private static final String LIST_NAME = "Inventory";
    private static final String EQUIPMENT = "equipment";

    private static final Map<Integer, String> EQUIPMENT_SLOTS = equipmentSlots();
    private static final Codec<PlayerInventory> CODEC =
            ContainerCodecs.containerCodec(PlayerInventory::new, PlayerInventory.STORAGE_SIZE);

    private PlayerInventoryCodec() {
    }

    private static Map<Integer, String> equipmentSlots() {
        final Map<Integer, String> slots = new LinkedHashMap<>();
        for (final EquipmentSlot slot : EquipmentSlot.values()) {
            final int index = PlayerInventory.slotOf(slot);
            if (index >= 0) {
                slots.put(index, slot.serializedName());
            }
        }
        return Map.copyOf(slots);
    }

    public static byte[] encode(final byte[] existing, final PlayerInventory inventory) throws IOException {
        final CompoundBinaryTag root = ContainerCodecs.readRoot(existing, DataFixerType.PLAYER);
        CompoundBinaryTag equipment = root.getCompound(EQUIPMENT);
        for (final Map.Entry<Integer, String> entry : EQUIPMENT_SLOTS.entrySet()) {
            final int slot = entry.getKey();
            equipment = slot < inventory.size() && !inventory.get(slot).isEmpty()
                    ? equipment.put(entry.getValue(), ContainerCodecs.writeItem(inventory.get(slot)))
                    : equipment.remove(entry.getValue());
        }
        final CompoundBinaryTag withItems = ContainerCodecs.writeList(root, LIST_NAME, CODEC, inventory);
        final CompoundBinaryTag result = equipment.isEmpty() ? withItems.remove(EQUIPMENT) : withItems.put(EQUIPMENT, equipment);
        return ContainerCodecs.writeRoot(ROOT_NAME, result, VersionConstants.DATA_VERSION);
    }

    public static PlayerInventory decode(final byte[] payload) throws IOException {
        final CompoundBinaryTag root = ContainerCodecs.readRoot(payload, DataFixerType.PLAYER);
        final PlayerInventory inventory = ContainerCodecs.readList(root, LIST_NAME, CODEC);
        final CompoundBinaryTag equipment = root.getCompound(EQUIPMENT);
        for (final Map.Entry<Integer, String> entry : EQUIPMENT_SLOTS.entrySet()) {
            final int slot = entry.getKey();
            final BinaryTag tag = equipment.get(entry.getValue());
            if (tag != null && slot < inventory.size()) {
                ContainerCodecs.readItem(tag).ifPresent(stack -> inventory.set(slot, stack));
            }
        }
        return inventory;
    }
}
