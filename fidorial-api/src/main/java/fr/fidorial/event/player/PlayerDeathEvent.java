package fr.fidorial.event.player;

import fr.fidorial.entity.Entity;
import fr.fidorial.entity.Player;
import fr.fidorial.event.entity.EntityDeathEvent;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

/**
 * Fired when a player dies, before the death message is broadcast and the death screen shown.
 *
 * @since 0.1.0
 */
public final class PlayerDeathEvent extends EntityDeathEvent implements PlayerEvent {

    private @Nullable Component deathMessage;

    /**
     * Creates an event.
     *
     * @param player       the player who died
     * @param killer       the entity credited with the kill, or {@code null}
     * @param deathMessage the message to broadcast, or {@code null} to stay silent
     * @since 0.1.0
     */
    public PlayerDeathEvent(final Player player, final @Nullable Entity killer, final @Nullable Component deathMessage) {
        super(player, killer);
        this.deathMessage = deathMessage;
    }

    /**
     * {@return the player who died}
     *
     * @since 0.1.0
     */
    @Override
    public Player entity() {
        return (Player) super.entity();
    }

    @Override
    public Player player() {
        return entity();
    }

    /**
     * @return the message broadcast to the server, or {@code null} if the death stays silent
     */
    public @Nullable Component deathMessage() {
        return deathMessage;
    }

    /**
     * @param deathMessage the message to broadcast instead, or {@code null} to broadcast nothing
     */
    public void setDeathMessage(final @Nullable Component deathMessage) {
        this.deathMessage = deathMessage;
    }
}
