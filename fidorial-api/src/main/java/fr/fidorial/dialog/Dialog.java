package fr.fidorial.dialog;

import fr.fidorial.registry.TypedKey;
import net.kyori.adventure.dialog.DialogLike;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.ComponentLike;
import org.jetbrains.annotations.Contract;

import java.util.List;

/**
 * A dialog to show a player: either a full definition, or a reference to one held by the {@link DialogRegistry}.
 *
 * @since 0.1.0
 */
public sealed interface Dialog extends DialogLike permits DialogDefinition, DialogReference {

    /**
     * Points at a dialog held by the {@linkplain DialogRegistry registry} rather than describing
     * one.
     *
     * @param key the key the dialog is registered under
     * @return the reference
     * @since 0.1.0
     */
    @Contract("_ -> new")
    static DialogReference reference(final Key key) {
        return new DialogReference(key);
    }

    /**
     * Points at a dialog held by the {@linkplain DialogRegistry registry}.
     *
     * @param key the typed key the dialog is registered under
     * @return the reference
     * @since 0.1.0
     */
    @Contract("_ -> new")
    static DialogReference reference(final TypedKey<fr.fidorial.registry.data.Dialog> key) {
        return new DialogReference(key.key());
    }

    /**
     * Creates a one button dialog whose button reads {@code gui.ok} and only dismisses the screen.
     *
     * @param title   the screen title
     * @param message the text to display
     * @return the dialog
     * @since 0.1.0
     */
    @Contract("_, _ -> new")
    static NoticeDialog notice(final ComponentLike title, final ComponentLike message) {
        return NoticeDialog.of(DialogBase.builder(title).body(message).build());
    }

    /**
     * Creates a two button dialog.
     *
     * @param title   the screen title
     * @param message the text to display
     * @param yes     the button for the positive outcome
     * @param no      the button for the negative outcome
     * @return the dialog
     * @since 0.1.0
     */
    @Contract("_, _, _, _ -> new")
    static ConfirmationDialog confirmation(
            final ComponentLike title,
            final ComponentLike message,
            final DialogActionButton yes,
            final DialogActionButton no
    ) {
        return new ConfirmationDialog(DialogBase.builder(title).body(message).build(), yes, no);
    }

    /**
     * Creates a dialog holding a grid of buttons.
     *
     * @param title   the screen title
     * @param actions the buttons to lay out
     * @return the dialog
     * @since 0.1.0
     */
    @Contract("_, _ -> new")
    static MultiActionDialog multiAction(final ComponentLike title, final List<DialogActionButton> actions) {
        return MultiActionDialog.of(DialogBase.of(title), actions);
    }
}
