package fr.euphyllia.fidorial.server.events.redstone;

import fr.fidorial.event.redstone.RedstoneInputEvent;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.world.World;
import fr.fidorial.world.block.BlockData;

public final class RedstoneInputEventImpl extends AbstractRedstoneEvent implements RedstoneInputEvent {

    private final boolean powered;

    public RedstoneInputEventImpl(final World world, final BlockPosition position, final BlockData block, final boolean powered) {
        super(world, position, block);
        this.powered = powered;
    }

    @Override
    public boolean powered() {
        return powered;
    }
}
