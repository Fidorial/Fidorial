package fr.euphyllia.fidorial.server.events.redstone;

import com.google.common.base.Preconditions;
import fr.fidorial.event.redstone.PistonEvent;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.World;
import fr.fidorial.world.block.BlockData;

import java.util.List;

/**
 * What both piston events of the server hold: the piston, the side it faces and what it is about
 * to move and to break.
 */
abstract class AbstractPistonEvent extends AbstractRedstoneEvent implements PistonEvent {

    private final BlockFace direction;
    private final boolean sticky;
    private final List<BlockPosition> movedBlocks;
    private final List<BlockPosition> brokenBlocks;

    AbstractPistonEvent(final World world, final BlockPosition position, final BlockData block, final BlockFace direction,
                        final boolean sticky, final List<BlockPosition> movedBlocks, final List<BlockPosition> brokenBlocks) {
        super(world, position, block);
        Preconditions.checkArgument(direction != null, "The direction of a piston event must not be null");
        Preconditions.checkArgument(movedBlocks != null, "The moved blocks of a piston event must not be null");
        Preconditions.checkArgument(brokenBlocks != null, "The broken blocks of a piston event must not be null");
        this.direction = direction;
        this.sticky = sticky;
        this.movedBlocks = List.copyOf(movedBlocks);
        this.brokenBlocks = List.copyOf(brokenBlocks);
    }

    @Override
    public final BlockFace direction() {
        return direction;
    }

    @Override
    public final boolean sticky() {
        return sticky;
    }

    @Override
    public final List<BlockPosition> movedBlocks() {
        return movedBlocks;
    }

    @Override
    public final List<BlockPosition> brokenBlocks() {
        return brokenBlocks;
    }
}
