package fr.fidorial.inventory;

import fr.fidorial.entity.player.Player;

/**
 * The 27 slots of a player's ender chest, shared by every ender chest block they open.
 *
 * @see Player#enderChest()
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
