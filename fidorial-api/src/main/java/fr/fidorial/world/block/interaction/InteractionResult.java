package fr.fidorial.world.block.interaction;

/**
 * What a {@link BlockInteractionHandler} did with a right-click.
 *
 * @since 0.1.0
 */
public enum InteractionResult {

    /**
     * The handler did not apply. The next handler runs, and if none applies the
     * server falls back to placing the held block.
     */
    PASS,

    /**
     * The interaction was handled. No block is placed, and no arm swing is shown.
     */
    CONSUME,

    /**
     * The interaction was handled and the player's arm should swing.
     */
    SUCCESS;

    /**
     * @return {@code true} when the interaction was handled, so block placement must be skipped
     * @since 0.1.0
     */
    public boolean handled() {
        return this != PASS;
    }

    /**
     * @return {@code true} when the acting player's arm should swing
     * @since 0.1.0
     */
    public boolean shouldSwing() {
        return this == SUCCESS;
    }
}
