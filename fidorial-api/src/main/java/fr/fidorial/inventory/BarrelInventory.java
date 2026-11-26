package fr.fidorial.inventory;

import fr.fidorial.math.BlockPosition;
import fr.fidorial.world.World;

/**
 * The slots of a barrel, read from and written to its block entity as {@link BlockInventory} tells.
 *
 * <p>A barrel shows the {@linkplain #rows() rows} the configuration of its world gives, from one to
 * {@value #MAX_ROWS}: its {@linkplain #size() size} is that of those rows. Its block entity holds up
 * to {@value #MAX_ROWS} rows: when the configuration gives fewer rows than a barrel held, the items
 * of the rows it no longer shows are either kept out of reach or cleared when a player opens it, as
 * the configuration says. Breaking a barrel spills every item it holds, those of the rows it no
 * longer shows included.</p>
 *
 * <p>Choosing the rows of the barrels of a world is inspired by Purpur.</p>
 *
 * @see World#barrelAt(BlockPosition)
 * @since 0.1.0
 */
public interface BarrelInventory extends BlockInventory {

    /**
     * The number of slots of one row.
     *
     * @since 0.1.0
     */
    int ROW_SIZE = 9;

    /**
     * The most rows a barrel shows.
     *
     * @since 0.1.0
     */
    int MAX_ROWS = 6;

    /**
     * The rows of a vanilla barrel.
     *
     * @since 0.1.0
     */
    int DEFAULT_ROWS = 3;

    /**
     * {@return the rows the barrel shows, from {@code 1} to {@value #MAX_ROWS}, as the configuration of
     * its world gives them}
     *
     * @since 0.1.0
     */
    int rows();
}
