package fr.fidorial.event.player;

import fr.fidorial.annotation.ThreadContract;
import fr.fidorial.entity.Player;
import fr.fidorial.event.Cancellable;
import net.kyori.adventure.text.Component;

public class PlayerChatEvent implements PlayerEvent, Cancellable {

    private final Player player;
    private Component message;
    private boolean cancelled;

    public PlayerChatEvent(Player player, Component message) {
        this.player = player;
        this.message = message;
    }

    @Override
    @ThreadContract("get -> any")
    public Player player() {
        return player;
    }

    public Component message() {
        return message;
    }

    public void setMessage(Component message) {
        this.message = message;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
