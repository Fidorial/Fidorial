package fr.euphyllia.fidorial.server.world.block.redstone;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.redstone.RedstoneBehaviour;
import fr.fidorial.world.redstone.RedstoneWorld;
import fr.fidorial.world.redstone.SignalGetter;

/**
 * The trapped chest, a port of the signal of vanilla's {@code TrappedChestBlock}: as many players
 * as look into it, up to {@code 15}, is the signal it sends around it, and into the block below it.
 *
 * <p>The players looking into a block are those the server's {@code BlockViewerTracker} counts,
 * which tells {@link #viewersChanged} when they come and go.</p>
 */
public final class TrappedChestBlock implements RedstoneBehaviour {

    public static final TypedKey<BlockType> KEY = BlockTypeKeys.TRAPPED_CHEST;

    public static final TrappedChestBlock INSTANCE = new TrappedChestBlock();

    private TrappedChestBlock() {
    }

    @Override
    public boolean isSignalSource(final BlockData state) {
        return true;
    }

    @Override
    public int signal(final SignalGetter world, final BlockPosition position, final BlockData state, final BlockFace direction) {
        if (!FidorialServer.isInitialized()) {
            return 0;
        }
        final int viewers = FidorialServer.getInstance().players().blockViewers().count(world.world().key(), position);
        return Math.clamp(viewers, 0, 15);
    }

    /**
     * Only the block below the chest, which reads it upwards, is strongly powered.
     */
    @Override
    public int directSignal(final SignalGetter world, final BlockPosition position, final BlockData state, final BlockFace direction) {
        return direction == BlockFace.UP ? signal(world, position, state, direction) : 0;
    }

    /**
     * Tells the neighbours of the chest, and those of the block below it, that the number of players
     * looking into it changed, what vanilla's {@code TrappedChestBlockEntity.signalOpenCount} does.
     *
     * @param world    the redstone of the world of the chest
     * @param position where the chest is
     * @param previous how many players looked into it
     * @param current  how many players look into it now
     */
    public void viewersChanged(final RedstoneWorld world, final BlockPosition position, final int previous, final int current) {
        if (previous != current) {
            world.updateNeighbors(position, KEY);
            world.updateNeighbors(position.relative(BlockFace.DOWN), KEY);
        }
    }
}
