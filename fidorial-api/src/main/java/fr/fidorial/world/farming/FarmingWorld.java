package fr.fidorial.world.farming;

import fr.fidorial.entity.Entity;
import fr.fidorial.entity.player.Player;
import fr.fidorial.event.farming.HarvestEvent;
import fr.fidorial.event.farming.PlantGrowEvent;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TagKey;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.World;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockGetter;
import fr.fidorial.world.block.BlockType;
import org.jspecify.annotations.Nullable;

import java.util.random.RandomGenerator;

/**
 * A world as the farming engine drives it: what plants look at to grow, the changes they make, and
 * what the server reports to the engine.
 *
 * <p>Reads only look at loaded chunks, and are allowed from any thread. Writes happen on the thread
 * of the region owning the block: a write aimed at a block of another region, or of a chunk that is
 * not loaded, does nothing and returns {@code false}, so that a plant growing sideways simply tries
 * again later.</p>
 *
 * @see FarmingEngine#world(World)
 * @since 0.1.0
 */
public interface FarmingWorld extends BlockGetter {

    /**
     * {@return the world these plants grow in}
     *
     * @since 0.1.0
     */
    World world();

    /**
     * {@return the source of randomness of the calling thread, for growth chances}
     *
     * @since 0.1.0
     */
    RandomGenerator random();

    /**
     * {@return {@code true} if the block at a position belongs to a tag, {@code false} when it does
     * not or is not loaded}
     *
     * <p>Soils are tags, such as {@code minecraft:supports_crops}: a data pack or a plugin adding a
     * block to the tag makes it a soil.</p>
     *
     * @param position the block
     * @param tag      the tag
     * @since 0.1.0
     */
    boolean isTagged(BlockPosition position, TagKey<BlockType> tag);

    /**
     * Gets the light a plant at a position gets: the brighter of the light blocks give and the
     * light the sky gives at this time of day and in this weather.
     *
     * <p>While the light of a chunk is still being worked out, right after it was loaded, its
     * blocks are taken to stand in full light and under the open sky: a plant is not uprooted for
     * a darkness that is not there.</p>
     *
     * @param position the block
     * @return the light, from {@code 0} to {@code 15}
     * @since 0.1.0
     */
    int lightLevel(BlockPosition position);

    /**
     * {@return {@code true} if nothing stands between a position and the sky}
     *
     * @param position the block
     * @since 0.1.0
     */
    boolean canSeeSky(BlockPosition position);

    /**
     * {@return {@code true} if rain is falling on a position}
     *
     * @param position the block
     * @since 0.1.0
     */
    boolean isRainingAt(BlockPosition position);

    /**
     * {@return {@code true} if the block at a position is water, or holds water}
     *
     * @param position the block
     * @since 0.1.0
     */
    boolean hasWater(BlockPosition position);

    /**
     * Gets how well a field feeds the plant at a position: the soil right under it counts most,
     * the soil around it adds to it, wet soil counts more than dry soil, and a plant crowded by its
     * own kind on several sides grows half as fast as one planted in rows.
     *
     * <p>Soil is a block of the {@code minecraft:grows_crops} tag. The built-in crops grow on a
     * random tick with one chance in {@code floor(25 / speed) + 1}.</p>
     *
     * @param position where the plant is
     * @return the speed, {@code 1} for a lone plant on dry soil, more on a well kept field
     * @since 0.1.0
     */
    float growthSpeed(BlockPosition position);

    /**
     * {@return {@code true} if a state could stand at a position}: what its
     * {@link FarmingBehaviour#canSurvive} answers, {@code true} for a block type without a farming
     * behaviour
     *
     * @param position where the block would be
     * @param state    the state
     * @since 0.1.0
     */
    boolean canSurvive(BlockPosition position, BlockData state);

    /**
     * Replaces a block. Players see the change, light and redstone follow, and the plants around
     * are told.
     *
     * @param position where the block is
     * @param state    the new state
     * @return {@code false} if the block already held that state, its chunk is not loaded, or the
     *         calling thread does not own it
     * @since 0.1.0
     */
    boolean setBlock(BlockPosition position, BlockData state);

    /**
     * Makes a plant grow: fires a {@link PlantGrowEvent}, then writes what the listeners left of
     * the grown state.
     *
     * @param plant      where the plant growing is
     * @param target     where the grown state goes: {@code plant} for a crop ripening, another
     *                   position for a cane growing taller or a stem bearing a fruit
     * @param grown      the state to write at {@code target}
     * @param fertilized {@code true} if a fertilizer causes the growth
     * @return {@code false} if a listener cancelled the growth, or the block could not be
     *         {@linkplain #setBlock written}
     * @since 0.1.0
     */
    boolean grow(BlockPosition plant, BlockPosition target, BlockData grown, boolean fertilized);

    /**
     * Makes a plant grow in place, on its own.
     *
     * @param plant where the plant is
     * @param grown the state it takes
     * @return {@code false} if a listener cancelled the growth, or the block could not be written
     * @see #grow(BlockPosition, BlockPosition, BlockData, boolean)
     * @since 0.1.0
     */
    default boolean grow(final BlockPosition plant, final BlockData grown) {
        return grow(plant, plant, grown, false);
    }

    /**
     * Breaks a block that can no longer stand: it {@linkplain #harvest drops its harvest}, players
     * see and hear it break, and it leaves air behind, or its water when it held some.
     *
     * @param position where the block is
     * @return {@code false} if there was nothing to break, its chunk is not loaded, or the calling
     *         thread does not own it
     * @since 0.1.0
     */
    boolean destroy(BlockPosition position);

    /**
     * Drops what a block gives: fires a {@link HarvestEvent} with the
     * {@linkplain FarmingBehaviour#drops drops} of its behaviour, then drops what the listeners
     * left of them. The block itself is left as it is.
     *
     * <p>The server calls it when a player who is not in creative mode breaks a block, right before
     * the block is removed.</p>
     *
     * @param position where the block is
     * @param player   the player breaking the block, {@code null} when nobody does
     * @param tool     what the block is broken with, {@link ItemStack#EMPTY} for none
     * @return {@code true} if at least one stack was dropped
     * @since 0.1.0
     */
    boolean harvest(BlockPosition position, @Nullable Player player, ItemStack tool);

    /**
     * Drops a stack from the middle of a block, as a harvest falls.
     *
     * @param position the block to drop from
     * @param stack    the stack, nothing is dropped when it is empty
     * @since 0.1.0
     */
    void dropItem(BlockPosition position, ItemStack stack);

    /**
     * Schedules a call to {@link FarmingBehaviour#tick}, once {@code delay} game ticks have
     * elapsed. The tick only runs if the block is still of the same type by then, and is lost if
     * the server stops before.
     *
     * @param position the block to tick
     * @param delay    the number of game ticks to wait, {@code 1} for the next tick
     * @since 0.1.0
     */
    void scheduleTick(BlockPosition position, int delay);

    /**
     * Tells the engine a block was replaced, so that the plants and the soil around it react:
     * those that can no longer stand break, the others follow the change.
     *
     * <p>The server calls it for every block that changes, whatever changed it; a plugin only needs
     * it when it writes blocks by means the server does not see.</p>
     *
     * @param position where the block is
     * @param previous the state that was replaced
     * @param current  the state now in place
     * @since 0.1.0
     */
    void blockChanged(BlockPosition position, BlockData previous, BlockData current);

    /**
     * Uses the item a player holds on a block, when the item is one the
     * {@link FarmingRegistry} knows: a hoe tills, a fertilizer feeds a plant. The item is worn or
     * used up in the inventory of the player, unless they are in creative mode.
     *
     * <p>The server calls it when a player uses an item on a block, before trying to place a block
     * out of the item.</p>
     *
     * @param player   the player
     * @param slot     the slot of the {@linkplain Player#inventory() inventory} of the player
     *                 holding the item
     * @param position the block the item is used on
     * @param face     the face of that block the player clicked
     * @return {@code true} if the item did something, and nothing else should be done with it
     * @since 0.1.0
     */
    boolean useItem(Player player, int slot, BlockPosition position, BlockFace face);

    /**
     * Tells the engine an entity landed on a block after a fall, so that the
     * {@linkplain FarmingBehaviour#entityLanded behaviour} of the block reacts.
     *
     * @param entity       the entity that landed
     * @param position     the block it landed on
     * @param fallDistance how far it fell, in blocks
     * @since 0.1.0
     */
    void entityLanded(Entity entity, BlockPosition position, double fallDistance);
}
