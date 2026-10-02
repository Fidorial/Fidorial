package fr.fidorial.inventory;

/**
 * The 27 slots of a player's ender chest, shared by every ender chest block they open.
 *
 * @see fr.fidorial.entity.Player#enderChest()
 * @since 0.1.0
 */
public class EnderChestInventory extends SimpleContainer {

    /**
     * The number of slots of an ender chest.
     *
     * @since 0.1.0
     */
    public static final int SIZE = 27;

    /**
     * Creates an empty ender chest.
     *
     * @since 0.1.0
     */
    public EnderChestInventory() {
        super(SIZE);
    }
}
