package fr.fidorial.world.block.interaction;

import fr.fidorial.entity.Player;
import fr.fidorial.item.ItemStack;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.BlockPos;
import fr.fidorial.world.World;
import fr.fidorial.world.block.BlockData;
import net.kyori.adventure.sound.Sound;

/**
 * A right-click on a block, handed to a {@link BlockInteractionHandler}.
 *
 * @since 0.1.0
 */
public interface BlockInteractionContext {

    /**
     * @return the world the clicked block lives in
     * @since 0.1.0
     */
    World world();

    /**
     * @return the player who clicked
     * @since 0.1.0
     */
    Player player();

    /**
     * @return the clicked position
     * @since 0.1.0
     */
    BlockPos pos();

    /**
     * @return the state of the clicked block, as read when the interaction started
     * @since 0.1.0
     */
    BlockData block();

    /**
     * @return the face that was clicked
     * @since 0.1.0
     */
    BlockFace face();

    /**
     * @return the hand the player used
     * @since 0.1.0
     */
    InteractionHand hand();

    /**
     * @return the stack held in {@link #hand()}, possibly {@link ItemStack#EMPTY}
     * @since 0.1.0
     */
    ItemStack heldItem();

    /**
     * @return where on the clicked face the cursor landed, in block space
     * @since 0.1.0
     */
    float cursorX();

    /**
     * @return where on the clicked face the cursor landed, in block space
     * @since 0.1.0
     */
    float cursorY();

    /**
     * @return where on the clicked face the cursor landed, in block space
     * @since 0.1.0
     */
    float cursorZ();

    /**
     * @return {@code true} when the player's head is inside the clicked block
     * @since 0.1.0
     */
    boolean insideBlock();

    /**
     * Reads any block in this world. Positions in chunks that are not loaded read
     * as air rather than forcing a load.
     *
     * @param position the position to read
     * @return the state there, or air
     * @since 0.1.0
     */
    BlockData blockAt(BlockPos position);

    /**
     * @param direction the direction to look in
     * @return the state of the neighbour of the clicked block, or air
     * @since 0.1.0
     */
    default BlockData relative(final BlockFace direction) {
        return blockAt(pos().relative(direction));
    }

    /**
     * Replaces the clicked block and tells nearby clients about it.
     *
     * @param data the state to write
     * @return {@code true} when the write went through
     * @since 0.1.0
     */
    boolean setBlock(BlockData data);

    /**
     * Plays a sound at the centre of the clicked block, for everyone nearby.
     *
     * @param sound the sound to play
     * @since 0.1.0
     */
    void playSound(Sound sound);
}
