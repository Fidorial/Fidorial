package fr.euphyllia.fidorial.server.events.player;

import com.google.common.base.Preconditions;
import fr.fidorial.entity.Entity;
import fr.fidorial.entity.player.Player;
import fr.fidorial.event.player.PlayerDeathEvent;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

public final class PlayerDeathEventImpl implements PlayerDeathEvent {

    private final Player player;
    private final @Nullable Entity killer;
    private @Nullable Component deathMessage;
    private boolean keepInventory;

    public PlayerDeathEventImpl(final Player player, final @Nullable Entity killer,
                                final @Nullable Component deathMessage, final boolean keepInventory) {
        Preconditions.checkArgument(player != null, "The player who died must not be null");
        this.player = player;
        this.killer = killer;
        this.deathMessage = deathMessage;
        this.keepInventory = keepInventory;
    }

    @Override
    public Player entity() {
        return player;
    }

    @Override
    public Player player() {
        return player;
    }

    @Override
    public @Nullable Entity killer() {
        return killer;
    }

    @Override
    public @Nullable Component deathMessage() {
        return deathMessage;
    }

    @Override
    public void setDeathMessage(final @Nullable Component deathMessage) {
        this.deathMessage = deathMessage;
    }

    @Override
    public boolean keepInventory() {
        return keepInventory;
    }

    @Override
    public void setKeepInventory(final boolean keepInventory) {
        this.keepInventory = keepInventory;
    }
}
