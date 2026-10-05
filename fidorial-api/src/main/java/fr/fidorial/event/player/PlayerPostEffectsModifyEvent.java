package fr.fidorial.event.player;

import fr.fidorial.entity.Player;
import fr.fidorial.event.Cancellable;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * Fired right before a player has their post effects modified.
 * <p>
 * Cancelling the event keeps their current post effects.
 *
 * @apiNote the event is not fired whenever the underlying list of post effects hasn't changed
 * @since 0.1.0
 */
public final class PlayerPostEffectsModifyEvent implements PlayerEvent, Cancellable {

    private final Player player;
    private final List<Key> currentPostEffects;
    private final List<Key> newPostEffects;
    private final Cause cause;
    private boolean cancelled;

    /**
     * Creates an event.
     *
     * @param player the player whose post effects are about to be changed
     * @param cause what triggered the change in active post effects
     * @param currentPostEffects the currently active post effects
     * @param newPostEffects the post effects about to be applied
     * @since 0.1.0
     */
    @ApiStatus.Internal
    public PlayerPostEffectsModifyEvent(final Player player, final Cause cause, final List<Key> currentPostEffects, final List<Key> newPostEffects) {
        this.player = player;
        this.cause = cause;
        this.currentPostEffects = List.copyOf(currentPostEffects);
        this.newPostEffects = List.copyOf(newPostEffects);
    }

    @Override
    public Player player() {
        return this.player;
    }

    /**
     * {@return the new post effects}
     * @since 0.1.0
     */
    @Unmodifiable
    public List<Key> newPostEffects() {
        return this.newPostEffects;
    }

    /**
     * {@return the current active post effects}
     * @since 0.1.0
     */
    @Unmodifiable
    public List<Key> currentPostEffects() {
        return this.currentPostEffects;
    }

    /**
     * {@return what triggered the change in active post effects}
     * @since 0.1.0
     */
    public Cause cause() {
        return cause;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }

    /**
     * What triggered the change in the player's active post effects
     * @since 0.1.0
     */
    public enum Cause {
        /**
         * A plugin called a post-effect mutating method on {@link Player}.
         * @since 0.1.0
         */
        API,
        /**
         * Someone ran the {@literal /posteffect} command.
         * @since 0.1.0
         */
        COMMAND
    }
}
