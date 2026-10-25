package fr.euphyllia.fidorial.server.events.player;

import com.google.common.base.Preconditions;
import fr.fidorial.entity.player.Player;
import fr.fidorial.event.player.PlayerCraftItemEvent;
import fr.fidorial.item.ItemStack;
import fr.fidorial.recipe.CraftingMatch;

public final class PlayerCraftItemEventImpl implements PlayerCraftItemEvent {

    private final Player player;
    private final CraftingMatch match;
    private final ItemStack result;
    private final boolean shiftClick;
    private boolean cancelled;

    public PlayerCraftItemEventImpl(final Player player, final CraftingMatch match, final ItemStack result,
                                    final boolean shiftClick) {
        Preconditions.checkArgument(player != null, "The player crafting must not be null");
        Preconditions.checkArgument(match != null, "The recipe being crafted must not be null");
        Preconditions.checkArgument(result != null, "The result of a craft must not be null");
        this.player = player;
        this.match = match;
        this.result = result;
        this.shiftClick = shiftClick;
    }

    @Override
    public Player player() {
        return player;
    }

    @Override
    public CraftingMatch match() {
        return match;
    }

    @Override
    public ItemStack result() {
        return result;
    }

    @Override
    public boolean shiftClick() {
        return shiftClick;
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
