package fr.fidorial.event.player;

import fr.fidorial.entity.Player;
import fr.fidorial.event.Cancellable;
import fr.fidorial.item.ItemStack;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.BlockPos;
import fr.fidorial.world.World;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.interaction.InteractionHand;

public final class PlayerInteractBlockEvent implements PlayerEvent, Cancellable {

    private final Player player;
    private final World world;
    private final BlockPos position;
    private final BlockData block;
    private final BlockFace face;
    private final InteractionHand hand;
    private final ItemStack item;
    private final float cursorX;
    private final float cursorY;
    private final float cursorZ;
    private final boolean insideBlock;

    private boolean useInteractedBlock = true;
    private boolean useItemInHand = true;


    public PlayerInteractBlockEvent(final Player player, final World world, final BlockPos position, final BlockData block,
                                    final BlockFace face, final InteractionHand hand, final ItemStack item, final float cursorX,
                                    final float cursorY, final float cursorZ, final boolean insideBlock) {
        this.player = player;
        this.world = world;
        this.position = position;
        this.block = block;
        this.face = face;
        this.hand = hand;
        this.item = item;
        this.cursorX = cursorX;
        this.cursorY = cursorY;
        this.cursorZ = cursorZ;
        this.insideBlock = insideBlock;
    }

    @Override
    public Player player() {
        return player;
    }

    /**
     * @return the world the clicked block lives in
     * @since 0.1.0
     */
    public World world() {
        return world;
    }

    /**
     * @return the clicked position
     * @since 0.1.0
     */
    public BlockPos position() {
        return position;
    }

    /**
     * @return the state of the clicked block
     * @since 0.1.0
     */
    public BlockData block() {
        return block;
    }

    /**
     * @return the face that was clicked
     * @since 0.1.0
     */
    public BlockFace face() {
        return face;
    }

    /**
     * @return the hand the player used
     * @since 0.1.0
     */
    public InteractionHand hand() {
        return hand;
    }

    /**
     * @return the stack held in {@link #hand()}, possibly {@link ItemStack#EMPTY}
     * @since 0.1.0
     */
    public ItemStack item() {
        return item;
    }

    /**
     * @return where on the clicked face the cursor landed, in block space
     * @since 0.1.0
     */
    public float cursorX() {
        return cursorX;
    }

    /**
     * @return where on the clicked face the cursor landed, in block space
     * @since 0.1.0
     */
    public float cursorY() {
        return cursorY;
    }

    /**
     * @return where on the clicked face the cursor landed, in block space
     * @since 0.1.0
     */
    public float cursorZ() {
        return cursorZ;
    }

    /**
     * @return {@code true} when the player's head is inside the clicked block
     * @since 0.1.0
     */
    public boolean insideBlock() {
        return insideBlock;
    }

    /**
     * @return {@code true} when the clicked block is allowed to react
     * @since 0.1.0
     */
    public boolean useInteractedBlock() {
        return useInteractedBlock;
    }

    /**
     * @param use {@code false} to skip the block's own reaction
     * @since 0.1.0
     */
    public void setUseInteractedBlock(final boolean use) {
        this.useInteractedBlock = use;
    }

    /**
     * @return {@code true} when the held item is allowed to be placed
     * @since 0.1.0
     */
    public boolean useItemInHand() {
        return useItemInHand;
    }

    /**
     * @param use {@code false} to stop the held item from being placed
     * @since 0.1.0
     */
    public void setUseItemInHand(final boolean use) {
        this.useItemInHand = use;
    }

    /**
     * @return {@code true} when both the block reaction and the item use are denied
     * @since 0.1.0
     */
    @Override
    public boolean isCancelled() {
        return !useInteractedBlock && !useItemInHand;
    }

    /**
     * Denies or allows both the block reaction and the item use at once.
     *
     * @param cancelled {@code true} to deny both
     * @since 0.1.0
     */
    @Override
    public void setCancelled(final boolean cancelled) {
        this.useInteractedBlock = !cancelled;
        this.useItemInHand = !cancelled;
    }
}
