package fr.fidorial.world.farming;

import fr.fidorial.plugin.Plugin;
import fr.fidorial.registry.GameRegistries;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.Item;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockType;

import java.util.Optional;
import java.util.Set;

/**
 * What takes part in farming, and how: the {@link FarmingBehaviour} attached to each block type,
 * and what items do to the land.
 *
 * <p>Whatever a plugin declares here is in effect until it is disabled, or until it takes it back
 * with {@link #detachAll(Plugin)}. The latest declaration wins, and the previous one is in effect
 * again once the latest is taken back, so that a plugin can override a built-in plant and give it
 * back.</p>
 *
 * @see GameRegistries#farming()
 * @since 0.1.0
 */
public interface FarmingRegistry {

    /**
     * Attaches a behaviour to a block type, in place of the one in effect.
     *
     * @param block     the block type
     * @param behaviour its farming behaviour
     * @param owner     the owner of the attachment, usually the {@link Plugin} instance; it is
     *                  released by {@link #detachAll(Plugin)}
     * @since 0.1.0
     */
    void attach(TypedKey<BlockType> block, FarmingBehaviour behaviour, Plugin owner);

    /**
     * Detaches the behaviours an owner attached to a block type.
     *
     * @param block the block type
     * @param owner the owner given when attaching
     * @return {@code true} if at least one behaviour was detached
     * @since 0.1.0
     */
    boolean detach(TypedKey<BlockType> block, Plugin owner);

    /**
     * Takes back everything an owner declared: the behaviours it attached, and its seeds,
     * fertilizers, hoes and tillable soils.
     *
     * @param owner the owner given when declaring
     * @since 0.1.0
     */
    void detachAll(Plugin owner);

    /**
     * {@return the behaviour in effect for a block type, empty for a block taking no part in farming}
     *
     * @param block the block type
     * @since 0.1.0
     */
    Optional<FarmingBehaviour> behaviour(TypedKey<BlockType> block);

    /**
     * {@return the behaviour in effect for the type of a state, empty for a block taking no part in farming}
     *
     * @param data the state
     * @since 0.1.0
     */
    default Optional<FarmingBehaviour> behaviour(final BlockData data) {
        return behaviour(data.key());
    }

    /**
     * {@return an immutable snapshot of the block types that have a behaviour}
     *
     * @since 0.1.0
     */
    Set<TypedKey<BlockType>> blocks();

    /**
     * Declares an item as the seed of a plant: using it on a block plants the default state of
     * the plant against the clicked face, where the plant
     * {@linkplain FarmingBehaviour#canSurvive can stand}, and uses one item up.
     *
     * @param item  the item
     * @param plant the block type it plants
     * @param owner the owner of the declaration
     * @since 0.1.0
     */
    void seed(TypedKey<Item> item, TypedKey<BlockType> plant, Plugin owner);

    /**
     * {@return the block type an item plants, empty for an item that is not a seed}
     *
     * @param item the item
     * @since 0.1.0
     */
    Optional<TypedKey<BlockType>> plant(TypedKey<Item> item);

    /**
     * Declares an item as a fertilizer: using it on a block that
     * {@linkplain FarmingBehaviour#canFertilize takes fertilizer}
     * {@linkplain FarmingBehaviour#fertilize feeds} the block and uses one item up.
     *
     * @param item  the item
     * @param owner the owner of the declaration
     * @since 0.1.0
     */
    void fertilizer(TypedKey<Item> item, Plugin owner);

    /**
     * {@return {@code true} if an item is a fertilizer}
     *
     * @param item the item
     * @since 0.1.0
     */
    boolean isFertilizer(TypedKey<Item> item);

    /**
     * Declares an item as a hoe: using it on {@linkplain #tillable tillable} soil tills it and
     * wears the item. The items of the {@code minecraft:hoes} tag are hoes without being declared.
     *
     * @param item  the item
     * @param owner the owner of the declaration
     * @since 0.1.0
     */
    void hoe(TypedKey<Item> item, Plugin owner);

    /**
     * {@return {@code true} if an item is a hoe}
     *
     * @param item the item
     * @since 0.1.0
     */
    boolean isHoe(TypedKey<Item> item);

    /**
     * Declares what a hoe does to a block type.
     *
     * @param soil    the block type a hoe is used on
     * @param tillage what it turns into, and what comes out of it
     * @param owner   the owner of the declaration
     * @since 0.1.0
     */
    void tillable(TypedKey<BlockType> soil, Tillage tillage, Plugin owner);

    /**
     * {@return what a hoe does to a block type, empty for a block a hoe does nothing to}
     *
     * @param soil the block type a hoe is used on
     * @since 0.1.0
     */
    Optional<Tillage> tillage(TypedKey<BlockType> soil);
}
