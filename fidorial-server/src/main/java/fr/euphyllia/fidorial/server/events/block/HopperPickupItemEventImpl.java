package fr.euphyllia.fidorial.server.events.block;

import com.google.common.base.Preconditions;
import fr.fidorial.entity.ItemEntity;
import fr.fidorial.event.block.HopperPickupItemEvent;
import fr.fidorial.inventory.HopperInventory;
import fr.fidorial.item.ItemStack;

public final class HopperPickupItemEventImpl extends AbstractHopperEvent implements HopperPickupItemEvent {

    private final ItemEntity item;
    private final ItemStack taken;

    /**
     * @param hopper the hopper picking the item up
     * @param item   the item entity above it
     * @param taken  what goes into the hopper
     */
    public HopperPickupItemEventImpl(final HopperInventory hopper, final ItemEntity item, final ItemStack taken) {
        super(hopper);
        Preconditions.checkArgument(item != null, "The hopper at %s picks up an item entity, not null", hopper.position());
        Preconditions.checkArgument(taken != null && !taken.isEmpty(), "The hopper at %s picks up some items, not none",
                hopper.position());
        this.item = item;
        this.taken = taken;
    }

    @Override
    public ItemEntity item() {
        return item;
    }

    @Override
    public ItemStack taken() {
        return taken;
    }
}
