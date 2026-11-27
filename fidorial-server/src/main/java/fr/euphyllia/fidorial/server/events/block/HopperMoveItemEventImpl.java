package fr.euphyllia.fidorial.server.events.block;

import com.google.common.base.Preconditions;
import fr.fidorial.event.block.HopperMoveItemEvent;
import fr.fidorial.inventory.Container;
import fr.fidorial.inventory.HopperInventory;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.BlockPosition;

public final class HopperMoveItemEventImpl extends AbstractHopperEvent implements HopperMoveItemEvent {

    private final boolean push;
    private final BlockPosition sourcePosition;
    private final BlockPosition destinationPosition;
    private final Container source;
    private final Container destination;
    private final ItemStack item;

    /**
     * @param hopper              the hopper moving the items
     * @param push                {@code true} when it pushes them out, {@code false} when it pulls them in
     * @param sourcePosition      where the items come from
     * @param source              the container they come from
     * @param destinationPosition where they go
     * @param destination         the container they go to
     * @param item                the items that move
     */
    public HopperMoveItemEventImpl(final HopperInventory hopper, final boolean push,
                                   final BlockPosition sourcePosition, final Container source,
                                   final BlockPosition destinationPosition, final Container destination,
                                   final ItemStack item) {
        super(hopper);
        Preconditions.checkArgument(sourcePosition != null, "The hopper at %s moves items from somewhere, not null",
                hopper.position());
        Preconditions.checkArgument(source != null, "The hopper at %s moves items out of a container, not null",
                hopper.position());
        Preconditions.checkArgument(destinationPosition != null, "The hopper at %s moves items somewhere, not to null",
                hopper.position());
        Preconditions.checkArgument(destination != null, "The hopper at %s moves items into a container, not null",
                hopper.position());
        Preconditions.checkArgument(item != null && !item.isEmpty(), "The hopper at %s moves some items, not none",
                hopper.position());
        this.push = push;
        this.sourcePosition = sourcePosition;
        this.destinationPosition = destinationPosition;
        this.source = source;
        this.destination = destination;
        this.item = item;
    }

    @Override
    public boolean isPush() {
        return push;
    }

    @Override
    public BlockPosition sourcePosition() {
        return sourcePosition;
    }

    @Override
    public BlockPosition destinationPosition() {
        return destinationPosition;
    }

    @Override
    public Container source() {
        return source;
    }

    @Override
    public Container destination() {
        return destination;
    }

    @Override
    public ItemStack item() {
        return item;
    }
}
