package fr.fidorial.inventory;

import fr.fidorial.entity.player.Player;

/**
 * A player's ender chest, shared by every ender chest block they open.
 *
 * <p>It always holds {@value #SIZE} slots, those of {@value #MAX_ROWS} rows, but a player only sees
 * the first {@link Player#enderChestRows()} rows of it. What happens to the items of the rows a
 * player no longer sees, kept hidden until they see them again or cleared when they open their
 * ender chest, is up to the server configuration.</p>
 *
 * @see Player#enderChest()
 * @since 0.1.0
 */
public interface EnderChestInventory extends Container {

    /**
     * The number of slots of one row.
     *
     * @since 0.1.0
     */
    int ROW_SIZE = 9;

    /**
     * The most rows a player can see in their ender chest.
     *
     * @since 0.1.0
     */
    int MAX_ROWS = 6;

    /**
     * The rows of a vanilla ender chest.
     *
     * @since 0.1.0
     */
    int DEFAULT_ROWS = 3;

    /**
     * The number of slots of an ender chest, those of {@value #MAX_ROWS} rows, seen or not.
     *
     * @since 0.1.0
     */
    int SIZE = ROW_SIZE * MAX_ROWS;
}
