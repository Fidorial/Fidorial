package fr.fidorial.event.player;

import fr.fidorial.entity.player.Player;
import fr.fidorial.event.Cancellable;
import fr.fidorial.inventory.EquipmentSlotGroup;
import fr.fidorial.item.ItemStack;

/**
 * Fired when a player uses what they hold without aiming at a block.
 *
 * @since 0.1.0
 */
public final class PlayerUseItemEvent implements PlayerEvent, Cancellable {

    private final Player player;
    private final EquipmentSlotGroup hand;
    private final ItemStack item;
    private boolean cancelled;

    /**
     * Creates an event.
     *
     * @param player the player
     * @param hand   the hand used, {@link EquipmentSlotGroup#MAIN_HAND} or {@link EquipmentSlotGroup#OFF_HAND}
     * @param item   what that hand holds, {@link ItemStack#EMPTY} when nothing
     * @since 0.1.0
     */
    public PlayerUseItemEvent(final Player player, final EquipmentSlotGroup hand, final ItemStack item) {
        this.player = player;
        this.hand = hand;
        this.item = item;
    }

    @Override
    public Player player() {
        return player;
    }

    /**
     * {@return the hand used, {@link EquipmentSlotGroup#MAIN_HAND} or {@link EquipmentSlotGroup#OFF_HAND}}
     *
     * @since 0.1.0
     */
    public EquipmentSlotGroup hand() {
        return hand;
    }

    /**
     * {@return what the hand holds, {@link ItemStack#EMPTY} when nothing}
     *
     * @since 0.1.0
     */
    public ItemStack item() {
        return item;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }
}
