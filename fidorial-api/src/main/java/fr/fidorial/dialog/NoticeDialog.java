package fr.fidorial.dialog;

import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Contract;


/**
 * A dialog with a single button in its footer.
 *
 * @param base   the shared title, contents and behaviour
 * @param action the only button of the dialog
 * @sinceMinecraft 1.21.6
 * @since 0.1.0
 */
public record NoticeDialog(DialogBase base, DialogActionButton action) implements DialogDefinition {

    /**
     * The button vanilla falls back to: a plain {@code gui.ok} that only dismisses the dialog.
     *
     * @since 0.1.0
     */
    public static final DialogActionButton DEFAULT_ACTION =
            DialogActionButton.of(Component.translatable("gui.ok"));

    /**
     * @param base   the shared title, contents and behaviour
     * @param action the only button of the dialog
     * @since 0.1.0
     */
    public NoticeDialog {
        Preconditions.checkArgument(base != null, "The base of a notice dialog must not be null");
        Preconditions.checkArgument(action != null, "The action of a notice dialog must not be null");
    }

    /**
     * Creates a notice whose button reads {@code gui.ok} and only dismisses the dialog.
     *
     * @param base the shared title, contents and behaviour
     * @return the dialog
     * @since 0.1.0
     */
    @Contract("_ -> new")
    public static NoticeDialog of(final DialogBase base) {
        return new NoticeDialog(base, DEFAULT_ACTION);
    }
}
