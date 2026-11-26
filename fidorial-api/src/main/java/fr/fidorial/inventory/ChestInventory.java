package fr.fidorial.inventory;

import fr.fidorial.math.BlockPosition;
import fr.fidorial.world.World;
import fr.fidorial.world.block.data.type.Chest;

import java.util.Optional;

/**
 * The 27 slots of one chest block: a chest, a trapped chest or one of the copper chests, read from
 * and written to its block entity as {@link BlockInventory} tells.
 *
 * <p>The {@linkplain #customName() name} of either half names a large chest, the first half
 * winning.</p>
 *
 * @see World#chestAt(BlockPosition)
 * @see DoubleChestInventory
 * @since 0.1.0
 */
public interface ChestInventory extends BlockInventory {

    /**
     * The number of slots of one chest.
     *
     * @since 0.1.0
     */
    int SIZE = 27;

    /**
     * {@return the large chest this chest is half of, empty while it stands alone}
     *
     * <p>Two chests form a large chest when their block states point at each other, as
     * {@link Chest#partnerDirection()} tells.</p>
     *
     * @since 0.1.0
     */
    Optional<DoubleChestInventory> doubleChest();
}
