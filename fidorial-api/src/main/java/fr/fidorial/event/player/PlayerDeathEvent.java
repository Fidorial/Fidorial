package fr.fidorial.event.player;

import fr.fidorial.entity.player.Player;
import fr.fidorial.event.entity.EntityDeathEvent;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

/**
 * Fired when a player dies, before the death message is broadcast and the death screen shown.
 *
 * @since 0.1.0
 */
public interface PlayerDeathEvent extends EntityDeathEvent, PlayerEvent {

    /**
     * {@return the player who died}
     *
     * @since 0.1.0
     */
    @Override
    Player entity();

    /**
     * {@return the message broadcast to the server, or {@code null} if the death stays silent}
     *
     * @since 0.1.0
     */
    @Nullable Component deathMessage();

    /**
     * Replaces the message broadcast to the server.
     *
     * @param deathMessage the message to broadcast instead, or {@code null} to broadcast nothing
     * @since 0.1.0
     */
    void setDeathMessage(@Nullable Component deathMessage);

    /**
     * {@return whether the player keeps their inventory; when not, it is dropped on the ground
     * where they died}
     *
     * <p>Starts from the {@code minecraft:keep_inventory} game rule of the world.</p>
     *
     * @since 0.1.0
     */
    boolean keepInventory();

    /**
     * Sets whether the player keeps their inventory instead of dropping it where they died.
     *
     * @param keepInventory {@code true} to keep it
     * @since 0.1.0
     */
    void setKeepInventory(boolean keepInventory);
}
