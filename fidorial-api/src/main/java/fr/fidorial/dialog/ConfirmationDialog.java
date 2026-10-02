package fr.fidorial.dialog;

import com.google.common.base.Preconditions;

/**
 * A dialog with two buttons in its footer.
 *
 * @param base the shared title, contents and behaviour
 * @param yes  the button for the positive outcome
 * @param no   the button for the negative outcome
 * @sinceMinecraft 1.21.6
 * @since 0.1.0
 */
public record ConfirmationDialog(DialogBase base, DialogActionButton yes, DialogActionButton no)
        implements DialogDefinition {

    /**
     * @param base the shared title, contents and behaviour
     * @param yes  the button for the positive outcome
     * @param no   the button for the negative outcome
     * @since 0.1.0
     */
    public ConfirmationDialog {
        Preconditions.checkArgument(base != null, "The base of a confirmation dialog must not be null");
        Preconditions.checkArgument(yes != null, "The positive button of a confirmation dialog must not be null");
        Preconditions.checkArgument(no != null, "The negative button of a confirmation dialog must not be null");
    }
}
