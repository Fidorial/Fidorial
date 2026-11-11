package fr.euphyllia.fidorial.server.events.farming;

import com.google.common.base.Preconditions;
import fr.fidorial.event.Cancellable;
import fr.fidorial.event.farming.FarmingEvent;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.world.World;
import fr.fidorial.world.block.BlockData;

/**
 * What every farming event of the server holds: the block, where it is, and whether a listener
 * cancelled what the event describes.
 */
abstract class AbstractFarmingEvent implements FarmingEvent, Cancellable {

    private final World world;
    private final BlockPosition position;
    private final BlockData block;
    private boolean cancelled;

    AbstractFarmingEvent(final World world, final BlockPosition position, final BlockData block) {
        Preconditions.checkArgument(world != null, "The world of a farming event must not be null");
        Preconditions.checkArgument(position != null, "The position of a farming event must not be null");
        Preconditions.checkArgument(block != null, "The block of a farming event must not be null");
        this.world = world;
        this.position = position;
        this.block = block;
    }

    @Override
    public final World world() {
        return world;
    }

    @Override
    public final BlockPosition position() {
        return position;
    }

    @Override
    public final BlockData block() {
        return block;
    }

    @Override
    public final boolean isCancelled() {
        return cancelled;
    }

    @Override
    public final void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }
}
