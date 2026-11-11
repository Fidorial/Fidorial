package fr.euphyllia.fidorial.server.events.farming;

import com.google.common.base.Preconditions;
import fr.fidorial.event.farming.SoilRevertEvent;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.world.World;
import fr.fidorial.world.block.BlockData;

public sealed class SoilRevertEventImpl extends AbstractFarmingEvent implements SoilRevertEvent permits SoilTrampleEventImpl {

    private BlockData reverted;

    public SoilRevertEventImpl(final World world, final BlockPosition position, final BlockData block,
                               final BlockData reverted) {
        super(world, position, block);
        Preconditions.checkArgument(reverted != null, "The reverted state of a soil revert event must not be null");
        this.reverted = reverted;
    }

    @Override
    public final BlockData reverted() {
        return reverted;
    }

    @Override
    public final void setReverted(final BlockData reverted) {
        Preconditions.checkArgument(reverted != null, "The reverted state of a soil revert event must not be null");
        this.reverted = reverted;
    }
}
