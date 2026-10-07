package fr.fidorial.dialog;

import fr.fidorial.annotation.SinceMinecraft;

/**
 * What the client does with a dialog screen once a click or submit action has been performed.
 *
 * @since 0.1.0
 * @since 0.1.0
 */
@SinceMinecraft("1.21.6")
public enum DialogAfterAction {

    /**
     * Closes the dialog and returns to the previous non-dialog screen, if any.
     *
     * @since 0.1.0
     */
    CLOSE("close"),

    /**
     * Keeps the current dialog open.
     *
     * @since 0.1.0
     */
    NONE("none"),

    /**
     * Replaces the dialog with the vanilla <em>Waiting for Response</em> screen.
     *
     * @since 0.1.0
     */
    WAIT_FOR_RESPONSE("wait_for_response");

    private final String id;

    DialogAfterAction(final String id) {
        this.id = id;
    }

    /**
     * {@return the identifier used on the wire, for instance {@code wait_for_response}}
     *
     * @since 0.1.0
     */
    public String id() {
        return id;
    }
}
