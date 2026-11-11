package fr.euphyllia.fidorial.server.events.farming;

import com.google.common.base.Preconditions;
import fr.fidorial.event.farming.SoilMoistureEvent;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.world.World;
import fr.fidorial.world.block.BlockData;

public final class SoilMoistureEventImpl extends AbstractFarmingEvent implements SoilMoistureEvent {

    private final BlockData moistened;

    public SoilMoistureEventImpl(final World world, final BlockPosition position, final BlockData block,
                                 final BlockData moistened) {
        super(world, position, block);
        Preconditions.checkArgument(moistened != null, "The moistened state of a soil moisture event must not be null");
        this.moistened = moistened;
    }

    @Override
    public BlockData moistened() {
        return moistened;
    }
}
