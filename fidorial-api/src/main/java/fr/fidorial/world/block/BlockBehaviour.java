package fr.fidorial.world.block;

import fr.fidorial.entity.player.Player;
import fr.fidorial.map.MapColor;
import fr.fidorial.map.MapManager;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.world.World;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

/**
 * The server-side logic of a block type: how it is placed, used and removed, and how it interacts
 * with light.
 *
 * @since 0.1.0
 */
public interface BlockBehaviour {

    /**
     * {@return the block type this behaviour drives}
     *
     * @since 0.1.0
     */
    BlockType type();

    /**
     * {@return the key of the block type this behaviour drives}
     *
     * @since 0.1.0
     */
    default Key key() {
        return type().key();
    }

    /**
     * Chooses the state a player places, for instance facing the player.
     *
     * @param context where and how the block is being placed
     * @return the state to place, or {@code null} to refuse the placement
     * @since 0.1.0
     */
    default @Nullable BlockData placementState(final BlockPlaceContext context) {
        return type().defaultData();
    }

    /**
     * Called when a player uses the block, a right click, unless they sneak with an item in hand.
     * Opening a crafting table or an ender chest happens here.
     *
     * <p>Runs on the thread of the region owning the block.</p>
     *
     * @param player   the player using the block
     * @param position where the block is, in the player's world
     * @param data     the state of the block
     * @return {@code true} when the use is handled: the held item is then neither used nor placed
     * @since 0.1.0
     */
    default boolean use(final Player player, final BlockPosition position, final BlockData data) {
        return false;
    }

    /**
     * Called when the block is about to be replaced by another block type: broken, blown up, or
     * overwritten by a plugin. The block and its block entity are still in place, so that what it
     * holds can be read.
     *
     * <p>Runs on the thread of the region owning the block.</p>
     *
     * @param world    the world of the block
     * @param position where the block is
     * @param data     the state being removed
     * @since 0.1.0
     */
    default void onRemove(final World world, final BlockPosition position, final BlockData data) {
    }

    /**
     * {@return the light level the state emits, from {@code 0} to {@code 15}}
     *
     * @param data the state
     * @since 0.1.0
     */
    default int lightEmission(final BlockData data) {
        return 0;
    }

    /**
     * {@return how much light the state absorbs, from {@code 0} (transparent) to {@code 15} (opaque)}
     *
     * @param data the state
     * @since 0.1.0
     */
    default int lightOpacity(final BlockData data) {
        return 15;
    }

    /**
     * Returns the color terrain maps draw the state with.
     *
     * @param data the state
     * @return the color, {@link MapColor#NONE} for a block maps look through, or {@code null} (the
     *         default) to keep the vanilla color of the block
     * @see MapManager#colorOf(BlockData)
     * @since 0.1.0
     */
    default @Nullable MapColor mapColor(final BlockData data) {
        return null;
    }
}
