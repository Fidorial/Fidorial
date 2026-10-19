package fr.fidorial.event.player;

import fr.fidorial.entity.player.Player;
import fr.fidorial.event.Cancellable;
import fr.fidorial.item.ItemStack;

/**
 * Fired when a player presses the drop key with an item in hand.
 *
 * @since 0.1.0
 */
public final class PlayerDropItemEvent implements PlayerEvent, Cancellable {

    private final Player player;
    private final ItemStack item;
    private final boolean wholeStack;
    private boolean cancelled;

    /**
     * Creates an event.
     *
     * @param player     the player
     * @param item       what the selected hotbar slot holds, {@link ItemStack#EMPTY} when nothing
     * @param wholeStack whether the whole stack is dropped ({@code Ctrl+Q}) rather than one item
     * @since 0.1.0
     */
    public PlayerDropItemEvent(final Player player, final ItemStack item, final boolean wholeStack) {
        this.player = player;
        this.item = item;
        this.wholeStack = wholeStack;
    }

    @Override
    public Player player() {
        return player;
    }

    /**
     * {@return what the selected hotbar slot holds, {@link ItemStack#EMPTY} when nothing}
     *
     * @since 0.1.0
     */
    public ItemStack item() {
        return item;
    }

    /**
     * {@return whether the whole stack is dropped rather than one item}
     *
     * @since 0.1.0
     */
    public boolean wholeStack() {
        return wholeStack;
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
