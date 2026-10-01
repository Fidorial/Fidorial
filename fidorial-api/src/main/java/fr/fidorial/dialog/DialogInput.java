package fr.fidorial.dialog;

import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public sealed interface DialogInput
        permits DialogInput.Text, DialogInput.Bool, DialogInput.SingleOption, DialogInput.NumberRange {
    /**
     * Largest width the client accepts for an input control.
     *
     * @since 0.1.0
     */
    int MAX_WIDTH = 1024;

    /**
     * Default width of an input control.
     *
     * @since 0.1.0
     */
    int DEFAULT_WIDTH = 200;

    /**
     * Starts building a single line text field.
     *
     * @param key   the identifier the value is sent under
     * @param label the label displayed next to the field
     * @return a fresh builder
     * @since 0.1.0
     */
    @Contract("_, _ -> new")
    static Text.Builder text(final String key, final ComponentLike label) {
        return new Text.Builder(key, label.asComponent());
    }

    /**
     * Creates an unchecked checkbox sending {@code true} or {@code false}.
     *
     * @param key   the identifier the value is sent under
     * @param label the label displayed next to the checkbox
     * @return the input control
     * @since 0.1.0
     */
    @Contract("_, _ -> new")
    static Bool checkbox(final String key, final ComponentLike label) {
        return new Bool(key, label.asComponent(), false, "true", "false");
    }

    /**
     * Starts building a preset option selection.
     *
     * @param key   the identifier the value is sent under
     * @param label the label displayed next to the control
     * @return a fresh builder
     * @since 0.1.0
     */
    @Contract("_, _ -> new")
    static SingleOption.Builder singleOption(final String key, final ComponentLike label) {
        return new SingleOption.Builder(key, label.asComponent());
    }

    /**
     * Starts building a number slider.
     *
     * @param key   the identifier the value is sent under
     * @param label the label displayed next to the slider
     * @param start the lowest value of the slider
     * @param end   the highest value of the slider
     * @return a fresh builder
     * @since 0.1.0
     */
    @Contract("_, _, _, _ -> new")
    static NumberRange.Builder numberRange(
            final String key,
            final ComponentLike label,
            final float start,
            final float end
    ) {
        return new NumberRange.Builder(key, label.asComponent(), start, end);
    }

    /**
     * {@return the identifier the submitted value is sent under}
     *
     * <p>Only letters, digits and {@code _} are allowed, so that the key is usable as a macro
     * argument.</p>
     *
     * @since 0.1.0
     */
    String key();

    /**
     * {@return the label displayed to the left of the control}
     *
     * @since 0.1.0
     */
    Component label();

    /**
     * A text field.
     *
     * <p>Substituted into a macro command with special characters escaped, and sent as a plain
     * string tag to a custom action.</p>
     *
     * @param key          the identifier the value is sent under
     * @param label        the label displayed to the left of the field
     * @param width        the width of the field, between 1 and {@value DialogInput#MAX_WIDTH}
     * @param labelVisible whether the label is drawn at all
     * @param initial      the value the field starts with
     * @param maxLength    the largest number of characters the player may type
     * @param multiline    when present, turns the field into a multiline text area
     * @since 0.1.0
     */
    record Text(
            String key,
            Component label,
            int width,
            boolean labelVisible,
            String initial,
            int maxLength,
            @Nullable Multiline multiline
    ) implements DialogInput {

        /**
         * Default value of {@link #maxLength()}.
         *
         * @since 0.1.0
         */
        public static final int DEFAULT_MAX_LENGTH = 32;

        /**
         * @param key          the identifier the value is sent under
         * @param label        the label displayed to the left of the field
         * @param width        the width of the field
         * @param labelVisible whether the label is drawn at all
         * @param initial      the value the field starts with
         * @param maxLength    the largest number of characters the player may type
         * @param multiline    when present, turns the field into a multiline text area
         * @since 0.1.0
         */
        public Text {
            DialogValidation.inputKey(key);
            Preconditions.checkNotNull(label, "The label of a dialog input text must not be null");
            Preconditions.checkNotNull(initial, "The initial value of a dialog input text must not be null");
            DialogValidation.width(width, MAX_WIDTH, "width");
            DialogValidation.positive(maxLength, "maxLength");
        }

        /**
         * Turns a text field into a text area.
         *
         * @param maxLines the largest number of lines accepted, or {@code null} for no limit
         * @param height   the height of the area, between 1 and {@value #MAX_HEIGHT}, or
         *                 {@code null} for the client default
         * @since 0.1.0
         */
        public record Multiline(@Nullable Integer maxLines, @Nullable Integer height) {

            /**
             * Largest height the client accepts for a text area.
             *
             * @since 0.1.0
             */
            public static final int MAX_HEIGHT = 512;

            /**
             * @param maxLines the largest number of lines accepted, or {@code null} for no limit
             * @param height   the height of the area, or {@code null} for the client default
             * @since 0.1.0
             */
            public Multiline {
                if (maxLines != null) {
                    DialogValidation.positive(maxLines, "maxLines");
                }
                if (height != null) {
                    DialogValidation.width(height, MAX_HEIGHT, "height");
                }
            }
        }

        /**
         * Fluent builder for {@link Text}.
         *
         * @since 0.1.0
         */
        public static final class Builder {

            private final String key;
            private Component label;
            private int width = DEFAULT_WIDTH;
            private boolean labelVisible = true;
            private String initial = "";
            private int maxLength = DEFAULT_MAX_LENGTH;
            private @Nullable Multiline multiline;

            Builder(final String key, final Component label) {
                this.key = DialogValidation.inputKey(key);
                this.label = Preconditions.checkNotNull(label, "The label of a dialog input text builder must not be null");
            }

            /**
             * @param label the label displayed to the left of the field
             * @return this builder
             * @since 0.1.0
             */
            @Contract("_ -> this")
            public Builder label(final ComponentLike label) {
                this.label = label.asComponent();
                return this;
            }

            /**
             * @param width the width of the field
             * @return this builder
             * @since 0.1.0
             */
            @Contract("_ -> this")
            public Builder width(final int width) {
                this.width = width;
                return this;
            }

            /**
             * @param labelVisible whether the label is drawn at all
             * @return this builder
             * @since 0.1.0
             */
            @Contract("_ -> this")
            public Builder labelVisible(final boolean labelVisible) {
                this.labelVisible = labelVisible;
                return this;
            }

            /**
             * @param initial the value the field starts with
             * @return this builder
             * @since 0.1.0
             */
            @Contract("_ -> this")
            public Builder initial(final String initial) {
                this.initial = Preconditions.checkNotNull(initial, "The initial value passed to DialogInput.Text.Builder.initial() must not be null");
                return this;
            }

            /**
             * @param maxLength the largest number of characters the player may type
             * @return this builder
             * @since 0.1.0
             */
            @Contract("_ -> this")
            public Builder maxLength(final int maxLength) {
                this.maxLength = maxLength;
                return this;
            }

            /**
             * Turns the field into a text area.
             *
             * @param maxLines the largest number of lines accepted, or {@code null} for no limit
             * @param height   the height of the area, or {@code null} for the client default
             * @return this builder
             * @since 0.1.0
             */
            @Contract("_, _ -> this")
            public Builder multiline(final @Nullable Integer maxLines, final @Nullable Integer height) {
                this.multiline = new Multiline(maxLines, height);
                return this;
            }

            /**
             * {@return the built input control}
             *
             * @since 0.1.0
             */
            @Contract("-> new")
            public Text build() {
                return new Text(key, label, width, labelVisible, initial, maxLength, multiline);
            }
        }
    }

    /**
     * A checkbox, mapping to the vanilla {@code minecraft:boolean} control.
     *
     * @param key     the identifier the value is sent under
     * @param label   the label displayed to the left of the checkbox
     * @param initial whether the box starts checked
     * @param onTrue  the string sent while the box is checked
     * @param onFalse the string sent while the box is unchecked
     * @since 0.1.0
     */
    record Bool(String key, Component label, boolean initial, String onTrue, String onFalse) implements DialogInput {

        /**
         * @param key     the identifier the value is sent under
         * @param label   the label displayed to the left of the checkbox
         * @param initial whether the box starts checked
         * @param onTrue  the string sent while the box is checked
         * @param onFalse the string sent while the box is unchecked
         * @since 0.1.0
         */
        public Bool {
            DialogValidation.inputKey(key);
            Preconditions.checkNotNull(label, "The label of a dialog input bool must not be null");
            Preconditions.checkNotNull(onTrue, "The value a dialog input bool sends when checked must not be null");
            Preconditions.checkNotNull(onFalse, "The value a dialog input bool sends when unchecked must not be null");
        }

        /**
         * @param initial whether the box starts checked
         * @return a copy of this control starting in the given state
         * @since 0.1.0
         */
        @Contract("_ -> new")
        public Bool initial(final boolean initial) {
            return new Bool(key, label, initial, onTrue, onFalse);
        }

        /**
         * @param onTrue  the string sent while the box is checked
         * @param onFalse the string sent while the box is unchecked
         * @return a copy of this control sending the given values
         * @since 0.1.0
         */
        @Contract("_, _ -> new")
        public Bool values(final String onTrue, final String onFalse) {
            return new Bool(key, label, initial, onTrue, onFalse);
        }
    }

    /**
     * A drop-down of preset options.
     *
     * @param key          the identifier the value is sent under
     * @param label        the label displayed to the left of the control
     * @param options      the options to pick from, never empty
     * @param labelVisible whether the label is drawn at all
     * @param width        the width of the control, between 1 and {@value DialogInput#MAX_WIDTH}
     * @since 0.1.0
     */
    record SingleOption(
            String key,
            Component label,
            List<Entry> options,
            boolean labelVisible,
            int width
    ) implements DialogInput {

        /**
         * @param key          the identifier the value is sent under
         * @param label        the label displayed to the left of the control
         * @param options      the options to pick from, never empty
         * @param labelVisible whether the label is drawn at all
         * @param width        the width of the control
         * @since 0.1.0
         */
        public SingleOption {
            DialogValidation.inputKey(key);
            Preconditions.checkNotNull(label, "The label of a dialog input single option must not be null");
            options = List.copyOf(options);
            if (options.isEmpty()) {
                throw new IllegalArgumentException("A single_option input needs at least one option");
            }
            DialogValidation.width(width, MAX_WIDTH, "width");

            int initial = 0;
            for (final Entry entry : options) {
                if (entry.initial()) {
                    initial++;
                }
            }
            if (initial > 1) {
                throw new IllegalArgumentException("At most one option may be marked as initial, found " + initial);
            }
        }

        /**
         * One entry of a {@link SingleOption} control.
         *
         * @param id      the value sent when this entry is picked
         * @param display how the entry reads on screen, or {@code null} to display {@code id}
         * @param initial whether the control starts on this entry
         * @since 0.1.0
         */
        public record Entry(String id, @Nullable Component display, boolean initial) {

            /**
             * @param id      the value sent when this entry is picked
             * @param display how the entry reads on screen, or {@code null} to display {@code id}
             * @param initial whether the control starts on this entry
             * @since 0.1.0
             */
            public Entry {
                Preconditions.checkNotNull(id, "The ID of a dialog input single option entry must not be null");
                if (id.isEmpty()) {
                    throw new IllegalArgumentException("An option id cannot be empty");
                }
            }

            /**
             * Creates an entry displaying its own identifier.
             *
             * @param id the value sent when this entry is picked
             * @return the entry
             * @since 0.1.0
             */
            @Contract("_ -> new")
            public static Entry of(final String id) {
                return new Entry(id, null, false);
            }

            /**
             * @param id      the value sent when this entry is picked
             * @param display how the entry reads on screen
             * @return the entry
             * @since 0.1.0
             */
            @Contract("_, _ -> new")
            public static Entry of(final String id, final ComponentLike display) {
                return new Entry(id, display.asComponent(), false);
            }

            /**
             * {@return a copy of this entry the control starts on}
             *
             * @since 0.1.0
             */
            @Contract("-> new")
            public Entry asInitial() {
                return new Entry(id, display, true);
            }
        }

        /**
         * Fluent builder for {@link SingleOption}.
         *
         * @since 0.1.0
         */
        public static final class Builder {

            private final String key;
            private final List<Entry> options = new ArrayList<>();
            private Component label;
            private boolean labelVisible = true;
            private int width = DEFAULT_WIDTH;

            Builder(final String key, final Component label) {
                this.key = DialogValidation.inputKey(key);
                this.label = Preconditions.checkNotNull(label, "The label of a dialog input single option builder must not be null");
            }

            /**
             * @param label the label displayed to the left of the control
             * @return this builder
             * @since 0.1.0
             */
            @Contract("_ -> this")
            public Builder label(final ComponentLike label) {
                this.label = label.asComponent();
                return this;
            }

            /**
             * @param labelVisible whether the label is drawn at all
             * @return this builder
             * @since 0.1.0
             */
            @Contract("_ -> this")
            public Builder labelVisible(final boolean labelVisible) {
                this.labelVisible = labelVisible;
                return this;
            }

            /**
             * @param width the width of the control
             * @return this builder
             * @since 0.1.0
             */
            @Contract("_ -> this")
            public Builder width(final int width) {
                this.width = width;
                return this;
            }

            /**
             * @param option the entry to append
             * @return this builder
             * @since 0.1.0
             */
            @Contract("_ -> this")
            public Builder option(final Entry option) {
                Preconditions.checkNotNull(option, "The option passed to DialogInput.SingleOption.Builder.option() must not be null");
                this.options.add(option);
                return this;
            }

            /**
             * @param id      the value sent when this entry is picked
             * @param display how the entry reads on screen
             * @return this builder
             * @since 0.1.0
             */
            @Contract("_, _ -> this")
            public Builder option(final String id, final ComponentLike display) {
                return option(Entry.of(id, display));
            }

            /**
             * @param options the entries to append
             * @return this builder
             * @since 0.1.0
             */
            @Contract("_ -> this")
            public Builder options(final List<Entry> options) {
                this.options.addAll(options);
                return this;
            }

            /**
             * {@return the built input control}
             *
             * @since 0.1.0
             */
            @Contract("-> new")
            public SingleOption build() {
                return new SingleOption(key, label, List.copyOf(options), labelVisible, width);
            }
        }
    }

    /**
     * A slider between two numbers.
     *
     * @param key         the identifier the value is sent under
     * @param label       the label displayed to the left of the slider
     * @param labelFormat the translation key building the label, taking the label and the current
     *                    value as arguments
     * @param width       the width of the slider, between 1 and {@value DialogInput#MAX_WIDTH}
     * @param start       the lowest value of the slider
     * @param end         the highest value of the slider
     * @param step        the increment between two accepted values, or {@code null} to accept any
     *                    value in the range
     * @param initial     the value the slider starts on, or {@code null} for the middle of the range
     * @since 0.1.0
     */
    record NumberRange(
            String key,
            Component label,
            String labelFormat,
            int width,
            float start,
            float end,
            @Nullable Float step,
            @Nullable Float initial
    ) implements DialogInput {

        /**
         * Default value of {@link #labelFormat()}.
         *
         * @since 0.1.0
         */
        public static final String DEFAULT_LABEL_FORMAT = "options.generic_value";

        /**
         * @param key         the identifier the value is sent under
         * @param label       the label displayed to the left of the slider
         * @param labelFormat the translation key building the label
         * @param width       the width of the slider
         * @param start       the lowest value of the slider
         * @param end         the highest value of the slider
         * @param step        the increment between two accepted values, or {@code null} for any
         * @param initial     the value the slider starts on, or {@code null} for the middle
         * @since 0.1.0
         */
        public NumberRange {
            DialogValidation.inputKey(key);
            Preconditions.checkNotNull(label, "The label of a dialog input number range must not be null");
            Preconditions.checkNotNull(labelFormat, "The label format of a dialog input number range must not be null");
            DialogValidation.width(width, MAX_WIDTH, "width");
            if (step != null && step <= 0f) {
                throw new IllegalArgumentException("step must be positive, was " + step);
            }
        }

        /**
         * Fluent builder for {@link NumberRange}.
         *
         * @since 0.1.0
         */
        public static final class Builder {

            private final String key;
            private final float start;
            private final float end;
            private Component label;
            private String labelFormat = DEFAULT_LABEL_FORMAT;
            private int width = DEFAULT_WIDTH;
            private @Nullable Float step;
            private @Nullable Float initial;

            Builder(final String key, final Component label, final float start, final float end) {
                this.key = DialogValidation.inputKey(key);
                this.label = Preconditions.checkNotNull(label, "The label of a dialog input number range builder must not be null");
                this.start = start;
                this.end = end;
            }

            /**
             * @param label the label displayed to the left of the slider
             * @return this builder
             * @since 0.1.0
             */
            @Contract("_ -> this")
            public Builder label(final ComponentLike label) {
                this.label = label.asComponent();
                return this;
            }

            /**
             * @param labelFormat the translation key building the label
             * @return this builder
             * @since 0.1.0
             */
            @Contract("_ -> this")
            public Builder labelFormat(final String labelFormat) {
                this.labelFormat = Preconditions.checkNotNull(labelFormat, "The label format passed to DialogInput.NumberRange.Builder.labelFormat() must not be null");
                return this;
            }

            /**
             * @param width the width of the slider
             * @return this builder
             * @since 0.1.0
             */
            @Contract("_ -> this")
            public Builder width(final int width) {
                this.width = width;
                return this;
            }

            /**
             * @param step the increment between two accepted values
             * @return this builder
             * @since 0.1.0
             */
            @Contract("_ -> this")
            public Builder step(final float step) {
                this.step = step;
                return this;
            }

            /**
             * @param initial the value the slider starts on
             * @return this builder
             * @since 0.1.0
             */
            @Contract("_ -> this")
            public Builder initial(final float initial) {
                this.initial = initial;
                return this;
            }

            /**
             * {@return the built input control}
             *
             * @since 0.1.0
             */
            @Contract("-> new")
            public NumberRange build() {
                return new NumberRange(key, label, labelFormat, width, start, end, step, initial);
            }
        }
    }
}
