package fr.fidorial.event.player;

import fr.fidorial.entity.Player;

/**
 * Fired when a player leaves the world.
 *
 * <p>Also fired when a player enters the configuration phase through
 * {@link Player#enterConfigurationPhase()}.</p>
 *
 * @param player the player who left
 * @since 0.1.0
 */
public record PlayerQuitEvent(Player player) implements PlayerEvent {
}
