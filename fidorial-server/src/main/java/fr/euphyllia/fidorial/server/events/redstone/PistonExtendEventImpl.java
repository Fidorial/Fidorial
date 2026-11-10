package fr.euphyllia.fidorial.server.events.redstone;

import fr.fidorial.event.redstone.PistonExtendEvent;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.World;
import fr.fidorial.world.block.BlockData;

import java.util.List;

public final class PistonExtendEventImpl extends AbstractPistonEvent implements PistonExtendEvent {

    public PistonExtendEventImpl(final World world, final BlockPosition position, final BlockData block, final BlockFace direction,
                                 final boolean sticky, final List<BlockPosition> movedBlocks, final List<BlockPosition> brokenBlocks) {
        super(world, position, block, direction, sticky, movedBlocks, brokenBlocks);
    }

    @Override
    public BlockFace movement() {
        return direction();
    }
}
