package fr.fidorial.inventory;

import fr.fidorial.entity.player.Player;
import org.jspecify.annotations.Nullable;

/**
 * The inventory a player carries.
 *
 * <p>Slots are laid out as follows: the hotbar from {@value #HOTBAR_START} to {@code 8}, the main
 * inventory from {@value #MAIN_START} to {@code 35}, the armor from {@value #FEET} (feet) to
 * {@value #HEAD} (head), then the off hand at {@value #OFF_HAND}. The remaining slots are
 * reserved.</p>
 *
 * @see Player#inventory()
 * @since 0.1.0
 */
public class PlayerInventory extends SimpleContainer {

    /**
     * The number of slots of a player inventory, reserved ones included.
     *
     * @since 0.1.0
     */
    public static final int SIZE = 46;

    /**
     * The first hotbar slot.
     *
     * @since 0.1.0
     */
    public static final int HOTBAR_START = 0;

    /**
     * The number of hotbar slots.
     *
     * @since 0.1.0
     */
    public static final int HOTBAR_SIZE = 9;

    /**
     * The first slot of the main inventory, right after the hotbar.
     *
     * @since 0.1.0
     */
    public static final int MAIN_START = 9;

    /**
     * The number of slots of the hotbar and the main inventory together, from {@value #HOTBAR_START}.
     *
     * @since 0.1.0
     */
    public static final int STORAGE_SIZE = 36;

    /**
     * The boots slot.
     *
     * @since 0.1.0
     */
    public static final int FEET = 36;

    /**
     * The leggings slot.
     *
     * @since 0.1.0
     */
    public static final int LEGS = 37;

    /**
     * The chestplate slot.
     *
     * @since 0.1.0
     */
    public static final int CHEST = 38;

    /**
     * The helmet slot.
     *
     * @since 0.1.0
     */
    public static final int HEAD = 39;

    /**
     * The off hand slot.
     *
     * @since 0.1.0
     */
    public static final int OFF_HAND = 40;

    /**
     * Where a piece of equipment sits in this inventory.
     *
     * @param slot the equipment slot
     * @return the inventory slot, {@code -1} for the main hand (it follows the selected hotbar slot)
     *         and for the slots a player does not have
     * @since 0.1.0
     */
    public static int slotOf(final EquipmentSlot slot) {
        return switch (slot) {
            case FEET -> FEET;
            case LEGS -> LEGS;
            case CHEST -> CHEST;
            case HEAD -> HEAD;
            case OFF_HAND -> OFF_HAND;
            case MAIN_HAND, BODY, SADDLE -> -1;
        };
    }

    /**
     * The other way round from {@link #slotOf(EquipmentSlot)}.
     *
     * @param slot an inventory slot
     * @return the equipment slot it holds, {@code null} for the hotbar, the main inventory and
     *         the reserved slots
     * @since 0.1.0
     */
    public static @Nullable EquipmentSlot equipmentSlotAt(final int slot) {
        return switch (slot) {
            case FEET -> EquipmentSlot.FEET;
            case LEGS -> EquipmentSlot.LEGS;
            case CHEST -> EquipmentSlot.CHEST;
            case HEAD -> EquipmentSlot.HEAD;
            case OFF_HAND -> EquipmentSlot.OFF_HAND;
            default -> null;
        };
    }

    /**
     * Creates an empty inventory.
     *
     * @since 0.1.0
     */
    public PlayerInventory() {
        super(SIZE);
    }
}
