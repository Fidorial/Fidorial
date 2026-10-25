package fr.fidorial.event.player;

import fr.fidorial.event.Cancellable;
import fr.fidorial.item.ItemStack;
import fr.fidorial.recipe.CraftingMatch;

/**
 * Fired when a player takes the result of a craft, before the ingredients are taken from the grid.
 *
 * <p>A shift-click crafts as many times as it can: the event fires once per craft. Cancelling it
 * leaves the grid untouched and gives nothing.</p>
 *
 * @since 0.1.0
 */
public interface PlayerCraftItemEvent extends PlayerEvent, Cancellable {

    /**
     * {@return the recipe being crafted, and what it takes from the grid}
     *
     * @since 0.1.0
     */
    CraftingMatch match();

    /**
     * {@return what the player gets}
     *
     * @since 0.1.0
     */
    ItemStack result();

    /**
     * {@return whether the player shift-clicked to craft as many as possible}
     *
     * @since 0.1.0
     */
    boolean shiftClick();
}
