package fr.fidorial.world.block;

import fr.fidorial.item.ItemStack;
import fr.fidorial.plugin.Plugin;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.BlockPos;
import fr.fidorial.world.block.interaction.BlockInteractionContext;
import fr.fidorial.world.block.interaction.InteractionResult;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.random.RandomGenerator;

/**
 * How one block type behaves in the world. Every hook has a neutral default, so a
 * behaviour only overrides what makes its block special.
 *
 * <p>Register a behaviour with {@link BlockRegistry#register(BlockBehaviour, Plugin)}.</p>
 *
 * @since 0.1.0
 */
public interface BlockBehaviour {

    BlockType type();

    default Key key() {
        return type().key();
    }

    default @Nullable BlockData placementState(final BlockPlaceContext context) {
        return type().defaultData();
    }

    default int lightEmission(final BlockData data) {
        return 0;
    }

    default int lightOpacity(final BlockData data) {
        return 15;
    }

    /**
     * Whether the block may exist at a position. Checked before the block is
     * placed, and again whenever a neighbour changes; a block that can no longer
     * survive is broken and drops its items.
     *
     * @param data  the state of the block
     * @param world where the block is, or would be
     * @param pos   the position of the block
     * @return {@code false} when the block cannot stay there
     * @since 0.1.0
     */
    default boolean canSurvive(final BlockData data, final BlockAccess world, final BlockPos pos) {
        return true;
    }

    /**
     * Lets the block adjust itself after one of its neighbours changed. Runs before
     * {@link #canSurvive}.
     *
     * @param data      the current state of the block
     * @param direction where the neighbour that changed is, seen from this block
     * @param neighbour the new state of that neighbour
     * @param world     where the block is
     * @param pos       the position of the block
     * @return the state the block should take; {@code data} itself to keep it as is
     * @since 0.1.0
     */
    default BlockData updateShape(final BlockData data,
                                  final BlockFace direction,
                                  final BlockData neighbour,
                                  final BlockAccess world,
                                  final BlockPos pos) {
        return data;
    }

    /**
     * @param data the state of the block
     * @return {@code true} when this state should receive {@link #randomTick random ticks}
     * @since 0.1.0
     */
    default boolean isRandomlyTicking(final BlockData data) {
        return false;
    }

    /**
     * Called now and then on blocks that {@linkplain #isRandomlyTicking tick
     * randomly}: about three blocks per 16×16×16 section each game tick, so any
     * given block about once every 68 seconds. Crops grow here, farmland dries
     * here, stems push their fruit here.
     *
     * @param data   the state of the block
     * @param world  where the block is
     * @param pos    the position of the block
     * @param random the source of randomness
     * @since 0.1.0
     */
    default void randomTick(final BlockData data, final BlockAccess world, final BlockPos pos, final RandomGenerator random) {
    }

    /**
     * Reacts to a right-click on the block. Runs after the handlers plugins
     * attached through the {@link fr.fidorial.world.block.interaction.BlockInteractionRegistry}
     * all {@linkplain InteractionResult#PASS passed}.
     *
     * @param data    the state of the clicked block
     * @param context the interaction
     * @return what the server should do next
     * @since 0.1.0
     */
    default InteractionResult use(final BlockData data, final BlockInteractionContext context) {
        return InteractionResult.PASS;
    }

    /**
     * @param data   the state the block was broken in
     * @param random the source of randomness
     * @return what breaking it gives back, empty for nothing
     * @since 0.1.0
     */
    default List<ItemStack> drops(final BlockData data, final RandomGenerator random) {
        return List.of();
    }

    /**
     * @param data the state of the block
     * @return {@code true} when survival players break it in one hit
     * @since 0.1.0
     */
    default boolean breaksInstantly(final BlockData data) {
        return false;
    }

    /**
     * @param data the state that was just placed
     * @return the sound nearby players hear when it is placed, or {@code null} for none
     * @since 0.1.0
     */
    default Sound.@Nullable Type placeSound(final BlockData data) {
        return null;
    }
}
