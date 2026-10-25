package fr.euphyllia.fidorial.server.events.player;

import com.google.common.base.Preconditions;
import fr.fidorial.entity.player.Player;
import fr.fidorial.event.player.PlayerPrepareCraftEvent;
import fr.fidorial.item.ItemStack;
import fr.fidorial.recipe.CraftingInput;
import fr.fidorial.recipe.CraftingMatch;
import org.jspecify.annotations.Nullable;

public final class PlayerPrepareCraftEventImpl implements PlayerPrepareCraftEvent {

    private final Player player;
    private final CraftingInput input;
    private final @Nullable CraftingMatch match;
    private ItemStack result;

    public PlayerPrepareCraftEventImpl(final Player player, final CraftingInput input, final @Nullable CraftingMatch match) {
        Preconditions.checkArgument(player != null, "The player crafting must not be null");
        Preconditions.checkArgument(input != null, "The crafting grid must not be null");
        this.player = player;
        this.input = input;
        this.match = match;
        this.result = match == null ? ItemStack.EMPTY : match.result();
    }

    @Override
    public Player player() {
        return player;
    }

    @Override
    public CraftingInput input() {
        return input;
    }

    @Override
    public @Nullable CraftingMatch match() {
        return match;
    }

    @Override
    public ItemStack result() {
        return result;
    }

    @Override
    public void setResult(final ItemStack result) {
        Preconditions.checkArgument(result != null, "The result of a craft must not be null");
        this.result = result;
    }
}
