package fr.euphyllia.fidorial.server.events.farming;

import com.google.common.base.Preconditions;
import fr.fidorial.entity.Entity;
import fr.fidorial.event.farming.SoilTrampleEvent;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.world.World;
import fr.fidorial.world.block.BlockData;

public final class SoilTrampleEventImpl extends SoilRevertEventImpl implements SoilTrampleEvent {

    private final Entity entity;
    private final double fallDistance;

    public SoilTrampleEventImpl(final World world, final BlockPosition position, final BlockData block,
                                final BlockData reverted, final Entity entity, final double fallDistance) {
        super(world, position, block, reverted);
        Preconditions.checkArgument(entity != null, "The entity of a soil trample event must not be null");
        this.entity = entity;
        this.fallDistance = fallDistance;
    }

    @Override
    public Entity entity() {
        return entity;
    }

    @Override
    public double fallDistance() {
        return fallDistance;
    }
}
