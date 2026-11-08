package fr.euphyllia.fidorial.server.events.redstone;

import fr.fidorial.event.redstone.RedstoneOutputEvent;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.world.World;
import fr.fidorial.world.block.BlockData;

public final class RedstoneOutputEventImpl extends AbstractRedstoneEvent implements RedstoneOutputEvent {

    private final int oldSignal;
    private final int newSignal;

    public RedstoneOutputEventImpl(final World world, final BlockPosition position, final BlockData block,
                                   final int oldSignal, final int newSignal) {
        super(world, position, block);
        this.oldSignal = checkSignal(oldSignal);
        this.newSignal = checkSignal(newSignal);
    }

    @Override
    public int oldSignal() {
        return oldSignal;
    }

    @Override
    public int newSignal() {
        return newSignal;
    }
}
