package fr.fidorial.event.player;

import fr.fidorial.entity.player.Player;
import fr.fidorial.event.Cancellable;
import fr.fidorial.item.ItemStack;

/**
 * Fired when a player swaps the items of their two hands.
 *
 * @since 0.1.0
 */
public final class PlayerSwapHandItemsEvent implements PlayerEvent, Cancellable {

    private final Player player;
    private final ItemStack mainHand;
    private final ItemStack offHand;
    private boolean cancelled;

    /**
     * Creates an event.
     *
     * @param player   the player
     * @param mainHand what the main hand holds before the swap, {@link ItemStack#EMPTY} when nothing
     * @param offHand  what the off hand holds before the swap, {@link ItemStack#EMPTY} when nothing
     * @since 0.1.0
     */
    public PlayerSwapHandItemsEvent(final Player player, final ItemStack mainHand, final ItemStack offHand) {
        this.player = player;
        this.mainHand = mainHand;
        this.offHand = offHand;
    }

    @Override
    public Player player() {
        return player;
    }

    /**
     * {@return what the main hand holds before the swap, and the off hand after it}
     *
     * @since 0.1.0
     */
    public ItemStack mainHand() {
        return mainHand;
    }

    /**
     * {@return what the off hand holds before the swap, and the main hand after it}
     *
     * @since 0.1.0
     */
    public ItemStack offHand() {
        return offHand;
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
