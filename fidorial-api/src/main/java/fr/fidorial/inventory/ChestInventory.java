package fr.fidorial.inventory;

import fr.fidorial.math.BlockPosition;
import fr.fidorial.world.World;
import fr.fidorial.world.block.data.type.Chest;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * The 27 slots of one chest block: a chest, a trapped chest or one of the copper chests.
 *
 * <p>The slots live in the block entity of the chest: what is read is what the chest holds, what is
 * written is saved with its chunk and shows at once in the window of every player looking into it.
 * Once the chest is gone, the inventory reads as empty and ignores what is written to it.</p>
 *
 * <p>A chest still holding a {@linkplain #lootTable() loot table} rolls it the first time its
 * contents are read: when a player opens it, a comparator reads it, it is broken, or a plugin reads
 * one of its slots.</p>
 *
 * <p>Use it on the thread of the region owning the chest.</p>
 *
 * @see World#chestAt(BlockPosition)
 * @see DoubleChestInventory
 * @since 0.1.0
 */
public interface ChestInventory extends Container {

    /**
     * The number of slots of one chest.
     *
     * @since 0.1.0
     */
    int SIZE = 27;

    /**
     * {@return the world of the chest}
     *
     * @since 0.1.0
     */
    World world();

    /**
     * {@return where the chest is}
     *
     * @since 0.1.0
     */
    BlockPosition position();

    /**
     * {@return the large chest this chest is half of, empty while it stands alone}
     *
     * <p>Two chests form a large chest when their block states point at each other, as
     * {@link Chest#partnerDirection()} tells.</p>
     *
     * @since 0.1.0
     */
    Optional<DoubleChestInventory> doubleChest();

    /**
     * {@return the name shown at the top of the window, empty for the default one}
     *
     * <p>The name of either half names a large chest, the first half winning.</p>
     *
     * @since 0.1.0
     */
    Optional<Component> customName();

    /**
     * Names the chest, as an item renamed in an anvil names the chest it places.
     *
     * @param name the name shown at the top of the window, {@code null} for the default one
     * @since 0.1.0
     */
    void setCustomName(@Nullable Component name);

    /**
     * {@return the loot table the chest has yet to roll, empty once rolled or for a chest that never
     * had one}
     *
     * <p>Reading this never rolls the table.</p>
     *
     * @since 0.1.0
     */
    Optional<Key> lootTable();

    /**
     * Gives the chest a loot table to roll the first time its contents are read, as the chests of
     * dungeons have. What the chest holds stays until then, the loot only filling its empty slots.
     *
     * @param table the loot table, such as {@code minecraft:chests/simple_dungeon}; {@code null} to
     *              forget the one it has
     * @param seed  the seed of the roll, {@code 0} for a random one
     * @since 0.1.0
     */
    void setLootTable(@Nullable Key table, long seed);
}
