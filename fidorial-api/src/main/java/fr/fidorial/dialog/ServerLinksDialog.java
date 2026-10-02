package fr.fidorial.dialog;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;


/**
 * A dialog listing the links the server advertised, laid out in columns.
 *
 * @param base        the shared title, contents and behaviour
 * @param exitAction  the footer button, or {@code null} for no footer
 * @param columns     how many links sit side by side
 * @param buttonWidth the width of each link button, between 1 and
 *                    {@value DialogActionButton#MAX_WIDTH}
 * @sinceMinecraft 1.21.6
 * @since 0.1.0
 */
public record ServerLinksDialog(
        DialogBase base,
        @Nullable DialogActionButton exitAction,
        int columns,
        int buttonWidth
) implements DialogDefinition {

    /**
     * Default number of columns.
     *
     * @since 0.1.0
     */
    public static final int DEFAULT_COLUMNS = 2;

    /**
     * @param base        the shared title, contents and behaviour
     * @param exitAction  the footer button, or {@code null} for no footer
     * @param columns     how many links sit side by side
     * @param buttonWidth the width of each link button
     * @since 0.1.0
     */
    public ServerLinksDialog {
        Preconditions.checkArgument(base != null, "The base of a server links dialog must not be null");
        DialogValidation.positive(columns, "columns");
        DialogValidation.width(buttonWidth, DialogActionButton.MAX_WIDTH, "button width");
    }

    /**
     * Creates a link list of two columns and no footer.
     *
     * @param base the shared title, contents and behaviour
     * @return the dialog
     * @since 0.1.0
     */
    @Contract("_ -> new")
    public static ServerLinksDialog of(final DialogBase base) {
        return new ServerLinksDialog(base, null, DEFAULT_COLUMNS, DialogActionButton.DEFAULT_WIDTH);
    }

    /**
     * @param exitAction the footer button, or {@code null} for no footer
     * @return a copy of this dialog carrying the given footer
     * @since 0.1.0
     */
    @Contract("_ -> new")
    public ServerLinksDialog exitAction(final @Nullable DialogActionButton exitAction) {
        return new ServerLinksDialog(base, exitAction, columns, buttonWidth);
    }
}
