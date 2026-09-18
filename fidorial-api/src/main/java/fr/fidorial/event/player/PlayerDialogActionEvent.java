package fr.fidorial.event.player;

import fr.fidorial.annotation.ThreadContract;
import fr.fidorial.dialog.DialogAction;
import fr.fidorial.dialog.DialogResponse;
import fr.fidorial.entity.Player;
import net.kyori.adventure.key.Key;

import java.util.Objects;

/**
 * Fired when a player presses a dialog button carrying a
 * {@link DialogAction#custom(Key) custom} or
 * {@link DialogAction#dynamicCustom(Key) dynamic custom} action.
 *
 * @since 0.1.0
 * @sinceMinecraft 1.21.6
 */
public final class PlayerDialogActionEvent implements PlayerEvent {

    private final Player player;
    private final Key id;
    private final DialogResponse response;

    /**
     * @param player   the player who pressed the button
     * @param id       the identifier declared on the action
     * @param response the values submitted alongside it
     * @since 0.1.0
     */
    public PlayerDialogActionEvent(final Player player, final Key id, final DialogResponse response) {
        this.player = Objects.requireNonNull(player, "player");
        this.id = Objects.requireNonNull(id, "id");
        this.response = Objects.requireNonNull(response, "response");
    }

    @Override
    @ThreadContract("get -> any; modify -> owner")
    public Player player() {
        return player;
    }

    /**
     * {@return the identifier the action was declared with}
     *
     * @since 0.1.0
     */
    public Key id() {
        return id;
    }

    /**
     * {@return the input values the player submitted, empty for a static custom action}
     *
     * @since 0.1.0
     */
    public DialogResponse response() {
        return response;
    }
}
