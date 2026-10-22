package fr.fidorial.event.player;

import fr.fidorial.entity.player.Player;
import fr.fidorial.entity.player.PlayerInput;

/**
 * Fired when a player presses or releases a movement key.
 *
 * <p>Listeners run on the network thread of the player, keep them short.</p>
 *
 * @since 0.1.0
 */
public record PlayerInputEvent(Player player, PlayerInput previous, PlayerInput input) implements PlayerEvent {

    /**
     * Creates an event.
     *
     * @param player   the player
     * @param previous the keys held before
     * @param input    the keys held now
     * @since 0.1.0
     */
    public PlayerInputEvent {
    }

    /**
     * {@return the keys held before this change}
     *
     * @since 0.1.0
     */
    @Override
    public PlayerInput previous() {
        return previous;
    }

    /**
     * {@return the keys held now}
     *
     * @since 0.1.0
     */
    @Override
    public PlayerInput input() {
        return input;
    }
}
