package fr.fidorial.dialog;

import com.google.common.base.Preconditions;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;


/**
 * What happens when a dialog button is pressed.
 *
 * @since 0.1.0
 */
public sealed interface DialogAction
        permits DialogAction.Static, DialogAction.ShowDialog, DialogAction.DynamicRunCommand, DialogAction.DynamicCustom {

    /**
     * Wraps an Adventure click event as a static dialog action.
     *
     * @param event the click event to perform
     * @return the action
     * @throws IllegalArgumentException if {@code event} opens a file, which dialogs do not support
     * @since 0.1.0
     */
    @Contract("_ -> new")
    static DialogAction of(final ClickEvent<?> event) {
        return new Static(event);
    }

    /**
     * Opens a URL in the player's browser.
     *
     * @param url the address to open
     * @return the action
     * @since 0.1.0
     */
    @Contract("_ -> new")
    static DialogAction openUrl(final String url) {
        return new Static(ClickEvent.openUrl(url));
    }

    /**
     * Runs a command as the player, who therefore needs the matching permission.
     *
     * @param command the command to run, with or without its leading slash
     * @return the action
     * @since 0.1.0
     */
    @Contract("_ -> new")
    static DialogAction runCommand(final String command) {
        return new Static(ClickEvent.runCommand(command));
    }

    /**
     * Opens the chat box pre-filled with some text.
     *
     * @param command the text to suggest
     * @return the action
     * @since 0.1.0
     */
    @Contract("_ -> new")
    static DialogAction suggestCommand(final String command) {
        return new Static(ClickEvent.suggestCommand(command));
    }

    /**
     * Copies text to the player's clipboard.
     *
     * @param value the text to copy
     * @return the action
     * @since 0.1.0
     */
    @Contract("_ -> new")
    static DialogAction copyToClipboard(final String value) {
        return new Static(ClickEvent.copyToClipboard(value));
    }

    /**
     * Runs server-side code when the button is pressed.
     *
     * @param callback the code to run, receiving the player who clicked
     * @return the action
     * @since 0.1.0
     */
    @Contract("_ -> new")
    static DialogAction callback(final ClickCallback<Audience> callback) {
        return new Static(ClickEvent.callback(callback));
    }

    /**
     * Runs server-side code when the button is pressed.
     *
     * @param callback the code to run, receiving the player who clicked
     * @param options  how long the callback lives and how often it may fire
     * @return the action
     * @since 0.1.0
     */
    @Contract("_, _ -> new")
    static DialogAction callback(final ClickCallback<Audience> callback, final ClickCallback.Options options) {
        return new Static(ClickEvent.callback(callback, options));
    }

    /**
     * Sends a bare event to the server, without any dialog input attached.
     *
     * @param id the identifier the server matches on
     * @return the action
     * @since 0.1.0
     */
    @Contract("_ -> new")
    static DialogAction custom(final Key id) {
        return new Static(ClickEvent.custom(id, null));
    }

    /**
     * Sends an event with a fixed payload to the server.
     *
     * @param id      the identifier the server matches on
     * @param payload the payload to attach, or {@code null} for none
     * @return the action
     * @since 0.1.0
     */
    @Contract("_, _ -> new")
    static DialogAction custom(final Key id, final @Nullable BinaryTagHolder payload) {
        return new Static(ClickEvent.custom(id, payload));
    }

    /**
     * Opens another dialog, defined inline or referenced from the registry.
     *
     * @param dialog the dialog to open
     * @return the action
     * @since 0.1.0
     */
    @Contract("_ -> new")
    static DialogAction showDialog(final Dialog dialog) {
        return new ShowDialog(dialog);
    }

    /**
     * Builds a command from the dialog's inputs and runs it as the player.
     *
     * @param template the macro command to run, with or without its leading slash
     * @return the action
     * @since 0.1.0
     */
    @Contract("_ -> new")
    static DialogAction dynamicRunCommand(final String template) {
        return new DynamicRunCommand(template);
    }

    /**
     * Sends every input value back to the server under a single identifier.
     *
     * @param id the identifier the server matches on
     * @return the action
     * @since 0.1.0
     */
    @Contract("_ -> new")
    static DialogAction dynamicCustom(final Key id) {
        return new DynamicCustom(id, null);
    }

    /**
     * Sends every input value back to the server, alongside some fixed data.
     *
     * @param id        the identifier the server matches on
     * @param additions extra tags merged into the payload next to the input values
     * @return the action
     * @since 0.1.0
     */
    @Contract("_, _ -> new")
    static DialogAction dynamicCustom(final Key id, final @Nullable CompoundBinaryTag additions) {
        return new DynamicCustom(id, additions);
    }

    /**
     * An action mirroring a text component click event.
     *
     * @param event the click event performed on press
     * @since 0.1.0
     */
    record Static(ClickEvent<?> event) implements DialogAction {

        /**
         * @param event the click event performed on press
         * @since 0.1.0
         */
        public Static {
            Preconditions.checkArgument(event != null, "The event of a dialog action static must not be null");
            if (event.action() instanceof ClickEvent.Action.OpenFile) {
                throw new IllegalArgumentException("Dialogs do not support the open_file click action");
            }
        }
    }

    /**
     * An action opening another dialog.
     *
     * @param dialog the dialog to open
     * @since 0.1.0
     */
    record ShowDialog(Dialog dialog) implements DialogAction {

        /**
         * @param dialog the dialog to open
         * @since 0.1.0
         */
        public ShowDialog {
            Preconditions.checkArgument(dialog != null, "The dialog of a dialog action show dialog must not be null");
        }
    }

    /**
     * An action running a macro command built from the dialog inputs.
     *
     * @param template the macro command to run
     * @since 0.1.0
     */
    record DynamicRunCommand(String template) implements DialogAction {

        /**
         * @param template the macro command to run
         * @since 0.1.0
         */
        public DynamicRunCommand {
            Preconditions.checkArgument(template != null, "The template of a dialog action dynamic run command must not be null");
        }
    }

    /**
     * An action posting the dialog inputs back to the server.
     *
     * @param id        the identifier the server matches on
     * @param additions extra tags merged into the payload, or {@code null} for none
     * @since 0.1.0
     */
    record DynamicCustom(Key id, @Nullable CompoundBinaryTag additions) implements DialogAction {

        /**
         * @param id        the identifier the server matches on
         * @param additions extra tags merged into the payload, or {@code null} for none
         * @since 0.1.0
         */
        public DynamicCustom {
            Preconditions.checkArgument(id != null, "The ID of a dialog action dynamic custom must not be null");
        }
    }
}
