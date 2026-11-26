package fr.fidorial.inventory;

import fr.fidorial.math.BlockPosition;
import fr.fidorial.world.World;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * The slots a block keeps in its block entity: those of a chest or of a barrel.
 *
 * <p>What is read is what the block holds, what is written is saved with its chunk and shows at
 * once in the window of every player looking into it. Once the block is gone, the inventory reads
 * as empty and ignores what is written to it.</p>
 *
 * <p>A block still holding a {@linkplain #lootTable() loot table} rolls it the first time its
 * contents are read: when a player opens it, a comparator reads it, it is broken, or a plugin reads
 * one of its slots.</p>
 *
 * <p>Use it on the thread of the region owning the block.</p>
 *
 * @see ChestInventory
 * @see BarrelInventory
 * @since 0.1.0
 */
public interface BlockInventory extends Container {

    /**
     * {@return the world of the block}
     *
     * @since 0.1.0
     */
    World world();

    /**
     * {@return where the block is}
     *
     * @since 0.1.0
     */
    BlockPosition position();

    /**
     * {@return the name shown at the top of the window, empty for the default one}
     *
     * @since 0.1.0
     */
    Optional<Component> customName();

    /**
     * Names the block, as an item renamed in an anvil names the block it places.
     *
     * @param name the name shown at the top of the window, {@code null} for the default one
     * @since 0.1.0
     */
    void setCustomName(@Nullable Component name);

    /**
     * {@return the loot table the block has yet to roll, empty once rolled or for a block that never
     * had one}
     *
     * <p>Reading this never rolls the table.</p>
     *
     * @since 0.1.0
     */
    Optional<Key> lootTable();

    /**
     * Gives the block a loot table to roll the first time its contents are read, as the chests of
     * dungeons have. What the block holds stays until then, the loot only filling its empty slots.
     *
     * @param table the loot table, such as {@code minecraft:chests/simple_dungeon}; {@code null} to
     *              forget the one it has
     * @param seed  the seed of the roll, {@code 0} for a random one
     * @since 0.1.0
     */
    void setLootTable(@Nullable Key table, long seed);
}
