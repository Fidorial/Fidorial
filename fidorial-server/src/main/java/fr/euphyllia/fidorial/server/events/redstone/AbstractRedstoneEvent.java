package fr.euphyllia.fidorial.server.events.redstone;

import com.google.common.base.Preconditions;
import fr.fidorial.event.Cancellable;
import fr.fidorial.event.redstone.RedstoneEvent;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.world.World;
import fr.fidorial.world.block.BlockData;

/**
 * What every redstone event of the server holds: the block, where it is, and whether a listener
 * cancelled what the event describes.
 */
abstract class AbstractRedstoneEvent implements RedstoneEvent, Cancellable {

    private final World world;
    private final BlockPosition position;
    private final BlockData block;
    private boolean cancelled;

    AbstractRedstoneEvent(final World world, final BlockPosition position, final BlockData block) {
        Preconditions.checkArgument(world != null, "The world of a redstone event must not be null");
        Preconditions.checkArgument(position != null, "The position of a redstone event must not be null");
        Preconditions.checkArgument(block != null, "The block of a redstone event must not be null");
        this.world = world;
        this.position = position;
        this.block = block;
    }

    /**
     * Checks a signal, or the power of dust.
     *
     * @throws IllegalArgumentException if it is not between {@code 0} and {@code 15}
     */
    static int checkSignal(final int signal) {
        Preconditions.checkArgument(signal >= 0 && signal <= 15, "A redstone signal is between 0 and 15, not %s", signal);
        return signal;
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
