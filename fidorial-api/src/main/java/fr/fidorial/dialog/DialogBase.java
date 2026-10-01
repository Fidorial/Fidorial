package fr.fidorial.dialog;

import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 *
 * @param title              the screen title, always visible whatever the dialog type
 * @param externalTitle      the label of the button leading to this dialog from elsewhere, for
 *                           instance the pause menu; falls back to {@code title} when absent
 * @param body               the elements laid out between the title and the inputs
 * @param inputs             the controls the player fills in
 * @param canCloseWithEscape whether the escape key dismisses the dialog
 * @param pause              whether the dialog pauses a single-player game; irrelevant on a
 *                           dedicated server but still part of the format
 * @param afterAction        what the client does with the screen once a button has been pressed
 * @since 0.1.0
 */
public record DialogBase(
        Component title,
        @Nullable Component externalTitle,
        List<DialogBody> body,
        List<DialogInput> inputs,
        boolean canCloseWithEscape,
        boolean pause,
        DialogAfterAction afterAction
) {

    /**
     * @param title              the screen title
     * @param externalTitle      the label of the button leading to this dialog, or {@code null}
     * @param body               the elements laid out between the title and the inputs
     * @param inputs             the controls the player fills in
     * @param canCloseWithEscape whether the escape key dismisses the dialog
     * @param pause              whether the dialog pauses a single-player game
     * @param afterAction        what the client does once a button has been pressed
     * @throws IllegalArgumentException if {@code afterAction} is
     *                                  {@link DialogAfterAction#NONE} while {@code pause} is set,
     *                                  which would lock a single-player client out of the game
     * @since 0.1.0
     */
    public DialogBase {
        Preconditions.checkNotNull(title, "The title of a dialog base must not be null");
        Preconditions.checkNotNull(afterAction, "The after action of a dialog base must not be null");
        body = List.copyOf(body);
        inputs = List.copyOf(inputs);

        if (afterAction == DialogAfterAction.NONE && pause) {
            throw new IllegalArgumentException(
                    "after_action=none requires pause=false, otherwise a single-player client can no longer "
                            + "return to the game");
        }

        final Set<String> seen = new HashSet<>(inputs.size());
        for (final DialogInput input : inputs) {
            if (!seen.add(input.key())) {
                throw new IllegalArgumentException("Duplicate input key: " + input.key());
            }
        }
    }

    /**
     * Creates a bare dialog base carrying nothing but a title.
     *
     * @param title the screen title
     * @return the base
     * @since 0.1.0
     */
    @Contract("_ -> new")
    public static DialogBase of(final ComponentLike title) {
        return builder(title).build();
    }

    /**
     * Starts building a dialog base.
     *
     * @param title the screen title
     * @return a fresh builder
     * @since 0.1.0
     */
    @Contract("_ -> new")
    public static Builder builder(final ComponentLike title) {
        return new Builder(title.asComponent());
    }

    /**
     * {@return a builder pre-filled with this base}
     *
     * @since 0.1.0
     */
    @Contract("-> new")
    public Builder toBuilder() {
        return new Builder(this);
    }

    /**
     * Fluent builder for {@link DialogBase}.
     *
     * @since 0.1.0
     */
    public static final class Builder {

        private final List<DialogBody> body = new ArrayList<>();
        private final List<DialogInput> inputs = new ArrayList<>();
        private Component title;
        private @Nullable Component externalTitle;
        private boolean canCloseWithEscape = true;
        private boolean pause = true;
        private DialogAfterAction afterAction = DialogAfterAction.CLOSE;

        Builder(final Component title) {
            this.title = Preconditions.checkNotNull(title, "The title of a dialog base builder must not be null");
        }

        Builder(final DialogBase base) {
            this.title = base.title();
            this.externalTitle = base.externalTitle();
            this.body.addAll(base.body());
            this.inputs.addAll(base.inputs());
            this.canCloseWithEscape = base.canCloseWithEscape();
            this.pause = base.pause();
            this.afterAction = base.afterAction();
        }

        /**
         * @param title the screen title
         * @return this builder
         * @since 0.1.0
         */
        @Contract("_ -> this")
        public Builder title(final ComponentLike title) {
            this.title = title.asComponent();
            return this;
        }

        /**
         * @param externalTitle the label of the button leading to this dialog, or {@code null} to
         *                      reuse the title
         * @return this builder
         * @since 0.1.0
         */
        @Contract("_ -> this")
        public Builder externalTitle(final @Nullable ComponentLike externalTitle) {
            this.externalTitle = externalTitle == null ? null : externalTitle.asComponent();
            return this;
        }

        /**
         * Appends a body element.
         *
         * @param element the element to append
         * @return this builder
         * @since 0.1.0
         */
        @Contract("_ -> this")
        public Builder body(final DialogBody element) {
            Preconditions.checkNotNull(element, "The element passed to DialogBase.Builder.body() must not be null");
            this.body.add(element);
            return this;
        }

        /**
         * Appends a plain text body element.
         *
         * @param message the text to display
         * @return this builder
         * @since 0.1.0
         */
        @Contract("_ -> this")
        public Builder body(final ComponentLike message) {
            return body(DialogBody.message(message));
        }

        /**
         * Appends several body elements.
         *
         * @param elements the elements to append
         * @return this builder
         * @since 0.1.0
         */
        @Contract("_ -> this")
        public Builder body(final List<? extends DialogBody> elements) {
            this.body.addAll(elements);
            return this;
        }

        /**
         * Appends an input control.
         *
         * @param input the control to append
         * @return this builder
         * @since 0.1.0
         */
        @Contract("_ -> this")
        public Builder input(final DialogInput input) {
            Preconditions.checkNotNull(input, "The input passed to DialogBase.Builder.input() must not be null");
            this.inputs.add(input);
            return this;
        }

        /**
         * Appends several input controls.
         *
         * @param inputs the controls to append
         * @return this builder
         * @since 0.1.0
         */
        @Contract("_ -> this")
        public Builder inputs(final List<? extends DialogInput> inputs) {
            this.inputs.addAll(inputs);
            return this;
        }

        /**
         * @param canCloseWithEscape whether the escape key dismisses the dialog
         * @return this builder
         * @since 0.1.0
         */
        @Contract("_ -> this")
        public Builder canCloseWithEscape(final boolean canCloseWithEscape) {
            this.canCloseWithEscape = canCloseWithEscape;
            return this;
        }

        /**
         * @param pause whether the dialog pauses a single-player game
         * @return this builder
         * @since 0.1.0
         */
        @Contract("_ -> this")
        public Builder pause(final boolean pause) {
            this.pause = pause;
            return this;
        }

        /**
         * Sets what the client does once a button has been pressed.
         *
         * <p>Picking {@link DialogAfterAction#NONE} also clears the {@linkplain #pause(boolean)
         * pause} flag, since the two are mutually exclusive.</p>
         *
         * @param afterAction the behaviour to apply
         * @return this builder
         * @since 0.1.0
         */
        @Contract("_ -> this")
        public Builder afterAction(final DialogAfterAction afterAction) {
            this.afterAction = Preconditions.checkNotNull(afterAction, "The after action passed to DialogBase.Builder.afterAction() must not be null");
            if (afterAction == DialogAfterAction.NONE) {
                this.pause = false;
            }
            return this;
        }

        /**
         * {@return the built base}
         *
         * @since 0.1.0
         */
        @Contract("-> new")
        public DialogBase build() {
            return new DialogBase(title, externalTitle, body, inputs, canCloseWithEscape, pause, afterAction);
        }
    }
}
