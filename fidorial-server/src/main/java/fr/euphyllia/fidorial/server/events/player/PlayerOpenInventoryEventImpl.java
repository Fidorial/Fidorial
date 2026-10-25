package fr.euphyllia.fidorial.server.events.player;

import com.google.common.base.Preconditions;
import fr.fidorial.entity.player.Player;
import fr.fidorial.event.player.PlayerOpenInventoryEvent;
import fr.fidorial.inventory.Container;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.MenuType;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public final class PlayerOpenInventoryEventImpl implements PlayerOpenInventoryEvent {

    private final Player player;
    private final TypedKey<MenuType> menuType;
    private final Component title;
    private final @Nullable BlockPosition blockPosition;
    private final Container inventory;
    private boolean cancelled;

    public PlayerOpenInventoryEventImpl(final Player player, final TypedKey<MenuType> menuType, final Component title,
                                        final @Nullable BlockPosition blockPosition, final Container inventory) {
        Preconditions.checkArgument(player != null, "The player opening the window must not be null");
        Preconditions.checkArgument(menuType != null, "The menu type must not be null");
        Preconditions.checkArgument(title != null, "The title must not be null");
        Preconditions.checkArgument(inventory != null, "The inventory shown must not be null");
        this.player = player;
        this.menuType = menuType;
        this.title = title;
        this.blockPosition = blockPosition;
        this.inventory = inventory;
    }

    @Override
    public Player player() {
        return player;
    }

    @Override
    public TypedKey<MenuType> menuType() {
        return menuType;
    }

    @Override
    public Component title() {
        return title;
    }

    @Override
    public Optional<BlockPosition> blockPosition() {
        return Optional.ofNullable(blockPosition);
    }

    @Override
    public Container inventory() {
        return inventory;
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
