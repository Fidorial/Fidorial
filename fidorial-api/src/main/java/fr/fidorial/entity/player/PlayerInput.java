package fr.fidorial.entity.player;

/**
 * The movement keys a player is holding, as their client reports them.
 *
 * <p>The client sends this state whether or not the player can actually move, so it also works as
 * a plain keyboard reader while movement is blocked.</p>
 *
 * @param forward  the forward key
 * @param backward the backward key
 * @param left     the strafe left key
 * @param right    the strafe right key
 * @param jump     the jump key
 * @param sneak    the sneak key
 * @param sprint   the sprint key
 * @since 0.1.0
 */
public record PlayerInput(boolean forward, boolean backward, boolean left, boolean right, boolean jump,
                          boolean sneak, boolean sprint) {

    /**
     * No key held.
     */
    public static final PlayerInput NONE = new PlayerInput(false, false, false, false, false, false, false);
}
