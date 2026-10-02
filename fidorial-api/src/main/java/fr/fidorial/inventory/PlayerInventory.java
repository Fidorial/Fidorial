package fr.fidorial.inventory;

/**
 * The inventory a player carries.
 *
 * <p>Slots are laid out as follows: the hotbar from {@value #HOTBAR_START} to {@code 8}, the main
 * inventory from {@value #MAIN_START} to {@code 35}, the armor from {@value #FEET} (feet) to
 * {@value #HEAD} (head), then the off hand at {@value #OFF_HAND}. The remaining slots are
 * reserved.</p>
 *
 * @see fr.fidorial.entity.Player#inventory()
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
     * Creates an empty inventory.
     *
     * @since 0.1.0
     */
    public PlayerInventory() {
        super(SIZE);
    }
}
