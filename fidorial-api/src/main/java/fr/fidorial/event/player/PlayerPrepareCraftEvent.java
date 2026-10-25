package fr.fidorial.event.player;

import fr.fidorial.item.ItemStack;
import fr.fidorial.recipe.CraftingInput;
import fr.fidorial.recipe.CraftingMatch;
import org.jspecify.annotations.Nullable;

/**
 * Fired each time the crafting grid of a player changes, once the server has looked for a matching
 * recipe and before the result slot is shown.
 *
 * <p>Setting the result to {@link ItemStack#EMPTY} hides it, so the craft cannot happen. Setting
 * another item shows that one instead; the ingredients taken stay those of the
 * {@linkplain #match() match}.</p>
 *
 * @since 0.1.0
 */
public interface PlayerPrepareCraftEvent extends PlayerEvent {

    /**
     * {@return the crafting grid}
     *
     * @since 0.1.0
     */
    CraftingInput input();

    /**
     * {@return the recipe found, {@code null} when none matches}
     *
     * @since 0.1.0
     */
    @Nullable CraftingMatch match();

    /**
     * {@return what the result slot shows, {@link ItemStack#EMPTY} for nothing}
     *
     * @since 0.1.0
     */
    ItemStack result();

    /**
     * Changes what the result slot shows. Ignored when no recipe {@linkplain #match() matches}:
     * there would be nothing to take from the grid.
     *
     * @param result the item to show, {@link ItemStack#EMPTY} to block the craft
     * @since 0.1.0
     */
    void setResult(ItemStack result);
}
