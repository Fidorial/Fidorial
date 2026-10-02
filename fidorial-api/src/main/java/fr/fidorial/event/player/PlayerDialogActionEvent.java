package fr.fidorial.event.player;

import com.google.common.base.Preconditions;
import fr.fidorial.dialog.DialogAction;
import fr.fidorial.dialog.DialogResponse;
import fr.fidorial.entity.Player;
import net.kyori.adventure.key.Key;


/**
 * Fired when a player presses a dialog button carrying a
 * {@link DialogAction#custom(Key) custom} or
 * {@link DialogAction#dynamicCustom(Key) dynamic custom} action.
 *
 * @sinceMinecraft 1.21.6
 * @since 0.1.0
 */
public record PlayerDialogActionEvent(Player player, Key id, DialogResponse response) implements PlayerEvent {

    /**
     * @param player   the player who pressed the button
     * @param id       the identifier declared on the action
     * @param response the values submitted alongside it
     * @since 0.1.0
     */
    public PlayerDialogActionEvent {
        Preconditions.checkArgument(player != null, "The player of a player dialog action event must not be null");
        Preconditions.checkArgument(id != null, "The ID of a player dialog action event must not be null");
        Preconditions.checkArgument(response != null, "The response of a player dialog action event must not be null");
    }

    /**
     * {@return the identifier the action was declared with}
     *
     * @since 0.1.0
     */
    @Override
    public Key id() {
        return id;
    }

    /**
     * {@return the input values the player submitted, empty for a static custom action}
     *
     * @since 0.1.0
     */
    @Override
    public DialogResponse response() {
        return response;
    }
}
