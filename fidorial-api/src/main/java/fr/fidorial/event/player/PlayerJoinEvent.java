package fr.fidorial.event.player;

import fr.fidorial.entity.Player;

/**
 * Fired once a player has entered the world.
 *
 * <p>Also fired when a player comes back from {@link Player#enterConfigurationPhase()}.</p>
 *
 * @param player the player who joined
 * @since 0.1.0
 */
public record PlayerJoinEvent(Player player) implements PlayerEvent {
}
