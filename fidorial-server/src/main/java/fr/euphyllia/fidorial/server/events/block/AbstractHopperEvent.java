package fr.euphyllia.fidorial.server.events.block;

import com.google.common.base.Preconditions;
import fr.fidorial.event.Cancellable;
import fr.fidorial.event.block.HopperEvent;
import fr.fidorial.inventory.HopperInventory;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.world.World;

/**
 * What every hopper event of the server holds: the hopper, and whether a listener cancelled what
 * the event describes.
 */
abstract class AbstractHopperEvent implements HopperEvent, Cancellable {

    private final HopperInventory hopper;
    private boolean cancelled;

    AbstractHopperEvent(final HopperInventory hopper) {
        Preconditions.checkArgument(hopper != null, "The hopper of a hopper event must not be null");
        this.hopper = hopper;
    }

    @Override
    public final World world() {
        return hopper.world();
    }

    @Override
    public final BlockPosition position() {
        return hopper.position();
    }

    @Override
    public final HopperInventory hopper() {
        return hopper;
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
