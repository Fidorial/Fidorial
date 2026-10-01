package fr.fidorial.dialog;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

import java.util.List;

public record MultiActionDialog(
        DialogBase base,
        List<DialogActionButton> actions,
        int columns,
        @Nullable DialogActionButton exitAction
) implements DialogDefinition {

    /**
     * Default number of columns.
     *
     * @since 0.1.0
     */
    public static final int DEFAULT_COLUMNS = 2;

    /**
     * @param base       the shared title, contents and behaviour
     * @param actions    the buttons of the grid, never empty
     * @param columns    how many buttons sit side by side
     * @param exitAction the footer button, or {@code null} for no footer
     * @since 0.1.0
     */
    public MultiActionDialog {
        Preconditions.checkNotNull(base, "The base of a multi action dialog must not be null");
        actions = List.copyOf(actions);
        if (actions.isEmpty()) {
            throw new IllegalArgumentException("A multi_action dialog needs at least one action");
        }
        DialogValidation.positive(columns, "columns");
    }

    /**
     * Creates a grid of two columns and no footer.
     *
     * @param base    the shared title, contents and behaviour
     * @param actions the buttons of the grid
     * @return the dialog
     * @since 0.1.0
     */
    @Contract("_, _ -> new")
    public static MultiActionDialog of(final DialogBase base, final List<DialogActionButton> actions) {
        return new MultiActionDialog(base, actions, DEFAULT_COLUMNS, null);
    }

    /**
     * @param columns how many buttons sit side by side
     * @return a copy of this dialog laid out in the given number of columns
     * @since 0.1.0
     */
    @Contract("_ -> new")
    public MultiActionDialog columns(final int columns) {
        return new MultiActionDialog(base, actions, columns, exitAction);
    }

    /**
     * @param exitAction the footer button, or {@code null} for no footer
     * @return a copy of this dialog carrying the given footer
     * @since 0.1.0
     */
    @Contract("_ -> new")
    public MultiActionDialog exitAction(final @Nullable DialogActionButton exitAction) {
        return new MultiActionDialog(base, actions, columns, exitAction);
    }
}
