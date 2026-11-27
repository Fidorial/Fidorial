package fr.fidorial.inventory;

import fr.fidorial.math.BlockPosition;
import fr.fidorial.world.World;
import fr.fidorial.world.block.data.type.Hopper;

/**
 * The {@value #SIZE} slots of a hopper, read from and written to its block entity as
 * {@link BlockInventory} tells.
 *
 * <p>Once its {@linkplain #transferCooldown() cooldown} is over, an {@linkplain Hopper#isEnabled()
 * enabled} hopper pushes items into the container it faces, then pulls items from the container
 * above it, and waits again the cooldown the configuration of its world gives after either one. With
 * no container above it, it also picks up the items lying in its bowl, and those lying above it unless
 * a full block covers it, without waiting for its cooldown nor starting it.</p>
 *
 * @see World#hopperAt(BlockPosition)
 * @since 0.1.0
 */
public interface HopperInventory extends BlockInventory {

    /**
     * The number of slots of a hopper.
     *
     * @since 0.1.0
     */
    int SIZE = 5;

    /**
     * {@return the game ticks left before the hopper moves items again, {@code 0} when it is ready}
     *
     * <p>A hopper only counts its cooldown down while its region ticks.</p>
     *
     * @since 0.1.0
     */
    int transferCooldown();

    /**
     * Makes the hopper wait before moving items again, or lets it move them on its next tick.
     *
     * @param ticks the game ticks to wait, {@code 0} to move items on the next tick
     * @throws IllegalArgumentException if {@code ticks} is negative
     * @since 0.1.0
     */
    void setTransferCooldown(int ticks);
}
