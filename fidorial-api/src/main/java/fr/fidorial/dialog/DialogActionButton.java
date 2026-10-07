package fr.fidorial.dialog;

import com.google.common.base.Preconditions;
import fr.fidorial.annotation.SinceMinecraft;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;


/**
 * A button of a dialog.
 *
 * @param label   the text of the button
 * @param tooltip the text shown on hover, or {@code null}
 * @param width   the width of the button, between 1 and {@value #MAX_WIDTH}
 * @param action  what pressing the button does, or {@code null} to only close the dialog
 * @since 0.1.0
 */
@SinceMinecraft("1.21.6")
public record DialogActionButton(
        Component label,
        @Nullable Component tooltip,
        int width,
        @Nullable DialogAction action
) {

    /**
     * Largest width the client accepts for a button.
     *
     * @since 0.1.0
     */
    public static final int MAX_WIDTH = 1024;

    /**
     * Default width of a button.
     *
     * @since 0.1.0
     */
    public static final int DEFAULT_WIDTH = 150;

    /**
     * @param label   the text on the button
     * @param tooltip an optional text shown while the button is hovered
     * @param width   the width of the button
     * @param action  what pressing the button does, or {@code null} to only dismiss the dialog
     * @since 0.1.0
     */
    public DialogActionButton {
        Preconditions.checkArgument(label != null, "The label of a dialog action button must not be null");
        DialogValidation.width(width, MAX_WIDTH, "width");
    }

    /**
     * Creates a button of the default width that only dismisses the dialog.
     *
     * @param label the text on the button
     * @return the button
     * @since 0.1.0
     */
    @Contract("_ -> new")
    public static DialogActionButton of(final ComponentLike label) {
        return new DialogActionButton(label.asComponent(), null, DEFAULT_WIDTH, null);
    }

    /**
     * Creates a button of the default width.
     *
     * @param label  the text on the button
     * @param action what pressing the button does
     * @return the button
     * @since 0.1.0
     */
    @Contract("_, _ -> new")
    public static DialogActionButton of(final ComponentLike label, final DialogAction action) {
        return new DialogActionButton(label.asComponent(), null, DEFAULT_WIDTH, action);
    }

    /**
     * Starts building a button.
     *
     * @param label the text on the button
     * @return a fresh builder
     * @since 0.1.0
     */
    @Contract("_ -> new")
    public static Builder builder(final ComponentLike label) {
        return new Builder(label.asComponent());
    }

    /**
     * Fluent builder for {@link DialogActionButton}.
     *
     * @since 0.1.0
     */
    public static final class Builder {

        private Component label;
        private @Nullable Component tooltip;
        private int width = DEFAULT_WIDTH;
        private @Nullable DialogAction action;

        Builder(final Component label) {
            Preconditions.checkArgument(label != null, "The label of a dialog action button builder must not be null");
            this.label = label;
        }

        /**
         * @param label the text on the button
         * @return this builder
         * @since 0.1.0
         */
        @Contract("_ -> this")
        public Builder label(final ComponentLike label) {
            this.label = label.asComponent();
            return this;
        }

        /**
         * @param tooltip the text shown while the button is hovered
         * @return this builder
         * @since 0.1.0
         */
        @Contract("_ -> this")
        public Builder tooltip(final @Nullable ComponentLike tooltip) {
            this.tooltip = tooltip == null ? null : tooltip.asComponent();
            return this;
        }

        /**
         * @param width the width of the button
         * @return this builder
         * @since 0.1.0
         */
        @Contract("_ -> this")
        public Builder width(final int width) {
            this.width = width;
            return this;
        }

        /**
         * @param action what pressing the button does, or {@code null} to only dismiss the dialog
         * @return this builder
         * @since 0.1.0
         */
        @Contract("_ -> this")
        public Builder action(final @Nullable DialogAction action) {
            this.action = action;
            return this;
        }

        /**
         * {@return the built button}
         *
         * @since 0.1.0
         */
        @Contract("-> new")
        public DialogActionButton build() {
            return new DialogActionButton(label, tooltip, width, action);
        }
    }
}
