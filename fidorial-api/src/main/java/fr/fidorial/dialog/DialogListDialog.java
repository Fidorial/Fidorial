package fr.fidorial.dialog;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * A dialog whose buttons lead to other dialogs.
 *
 * @param base        the shared title, contents and behaviour
 * @param dialogs     the dialogs to list, never empty
 * @param exitAction  the footer button, or {@code null} for no footer
 * @param columns     how many buttons sit side by side
 * @param buttonWidth the width of each button, between 1 and
 *                    {@value DialogActionButton#MAX_WIDTH}
 * @sinceMinecraft 1.21.6
 * @since 0.1.0
 */
public record DialogListDialog(
        DialogBase base,
        List<Dialog> dialogs,
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
     * @param dialogs     the dialogs to list, never empty
     * @param exitAction  the footer button, or {@code null} for no footer
     * @param columns     how many buttons sit side by side
     * @param buttonWidth the width of each button
     * @since 0.1.0
     */
    public DialogListDialog {
        Preconditions.checkNotNull(base, "The base of a dialog list dialog must not be null");
        dialogs = List.copyOf(dialogs);
        if (dialogs.isEmpty()) {
            throw new IllegalArgumentException("A dialog_list needs at least one dialog");
        }
        final boolean references = dialogs.getFirst() instanceof DialogReference;
        for (final Dialog dialog : dialogs) {
            if (dialog instanceof DialogReference != references) {
                throw new IllegalArgumentException(
                        "A dialog_list must hold either only references or only definitions, not both");
            }
        }
        DialogValidation.positive(columns, "columns");
        DialogValidation.width(buttonWidth, DialogActionButton.MAX_WIDTH, "buttonWidth");
    }

    /**
     * Creates a list of two columns and no footer.
     *
     * @param base    the shared title, contents and behaviour
     * @param dialogs the dialogs to list
     * @return the dialog
     * @since 0.1.0
     */
    @Contract("_, _ -> new")
    public static DialogListDialog of(final DialogBase base, final List<Dialog> dialogs) {
        return new DialogListDialog(base, dialogs, null, DEFAULT_COLUMNS, DialogActionButton.DEFAULT_WIDTH);
    }

    /**
     * @param exitAction the footer button, or {@code null} for no footer
     * @return a copy of this dialog carrying the given footer
     * @since 0.1.0
     */
    @Contract("_ -> new")
    public DialogListDialog exitAction(final @Nullable DialogActionButton exitAction) {
        return new DialogListDialog(base, dialogs, exitAction, columns, buttonWidth);
    }

    /**
     * {@return whether every entry of this list is a reference rather than a definition}
     *
     * @since 0.1.0
     */
    public boolean holdsReferences() {
        return dialogs.getFirst() instanceof DialogReference;
    }
}
