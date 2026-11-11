package fr.euphyllia.fidorial.server.events.farming;

import com.google.common.base.Preconditions;
import fr.fidorial.event.farming.PlantGrowEvent;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.world.World;
import fr.fidorial.world.block.BlockData;

public final class PlantGrowEventImpl extends AbstractFarmingEvent implements PlantGrowEvent {

    private final BlockPosition target;
    private final boolean fertilized;
    private BlockData grown;

    public PlantGrowEventImpl(final World world, final BlockPosition position, final BlockData block,
                              final BlockPosition target, final BlockData grown, final boolean fertilized) {
        super(world, position, block);
        Preconditions.checkArgument(target != null, "The target of a plant grow event must not be null");
        Preconditions.checkArgument(grown != null, "The grown state of a plant grow event must not be null");
        this.target = target;
        this.grown = grown;
        this.fertilized = fertilized;
    }

    @Override
    public BlockPosition target() {
        return target;
    }

    @Override
    public BlockData grown() {
        return grown;
    }

    @Override
    public void setGrown(final BlockData grown) {
        Preconditions.checkArgument(grown != null, "The grown state of a plant grow event must not be null");
        this.grown = grown;
    }

    @Override
    public boolean fertilized() {
        return fertilized;
    }
}
