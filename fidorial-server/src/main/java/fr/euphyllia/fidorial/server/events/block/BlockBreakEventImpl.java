package fr.euphyllia.fidorial.server.events.block;

import com.google.common.base.Preconditions;
import fr.fidorial.entity.player.Player;
import fr.fidorial.event.block.BlockBreakEvent;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.world.World;
import fr.fidorial.world.block.BlockData;

import java.util.ArrayList;
import java.util.List;

public final class BlockBreakEventImpl implements BlockBreakEvent {

    private final Player player;
    private final World world;
    private final BlockPosition position;
    private final BlockData block;
    private final ItemStack tool;
    private final List<ItemStack> drops;
    private boolean cancelled;

    /**
     * @param player   the player breaking the block
     * @param world    the world of the block, that of the player
     * @param position where the block is
     * @param block    the state being broken
     * @param tool     what the player breaks it with, {@link ItemStack#EMPTY} for a bare hand
     * @param drops    what the block is about to drop
     */
    public BlockBreakEventImpl(final Player player, final World world, final BlockPosition position,
                               final BlockData block, final ItemStack tool, final List<ItemStack> drops) {
        Preconditions.checkArgument(player != null, "The player of a block break event must not be null");
        Preconditions.checkArgument(world != null, "The world of a block break event must not be null");
        Preconditions.checkArgument(position != null, "The position of a block break event must not be null");
        Preconditions.checkArgument(block != null, "The block of a block break event must not be null");
        Preconditions.checkArgument(tool != null, "The tool of a block break event must not be null");
        Preconditions.checkArgument(drops != null, "The drops of a block break event must not be null");
        this.player = player;
        this.world = world;
        this.position = position;
        this.block = block;
        this.tool = tool;
        this.drops = new ArrayList<>(drops);
    }

    @Override
    public Player player() {
        return player;
    }

    @Override
    public World world() {
        return world;
    }

    @Override
    public BlockPosition position() {
        return position;
    }

    @Override
    public BlockData block() {
        return block;
    }

    @Override
    public ItemStack tool() {
        return tool;
    }

    @Override
    public List<ItemStack> drops() {
        return drops;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }
}
