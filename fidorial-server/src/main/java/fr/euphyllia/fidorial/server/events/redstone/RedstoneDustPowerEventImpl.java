package fr.euphyllia.fidorial.server.events.redstone;

import fr.fidorial.event.redstone.RedstoneDustPowerEvent;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.world.World;
import fr.fidorial.world.block.BlockData;

public final class RedstoneDustPowerEventImpl extends AbstractRedstoneEvent implements RedstoneDustPowerEvent {

    private final int oldPower;
    private int newPower;

    public RedstoneDustPowerEventImpl(final World world, final BlockPosition position, final BlockData block,
                                      final int oldPower, final int newPower) {
        super(world, position, block);
        this.oldPower = checkSignal(oldPower);
        this.newPower = checkSignal(newPower);
    }

    @Override
    public int oldPower() {
        return oldPower;
    }

    @Override
    public int newPower() {
        return newPower;
    }

    @Override
    public void setNewPower(final int newPower) {
        this.newPower = checkSignal(newPower);
    }
}
