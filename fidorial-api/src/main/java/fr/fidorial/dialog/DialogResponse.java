package fr.fidorial.dialog;

import com.google.common.base.Preconditions;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.NumberBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import org.jetbrains.annotations.Contract;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;

/**
 * The values a player submitted from a dialog, keyed by input key.
 *
 * @param values the raw payload received from the client
 * @since 0.1.0
 */
public record DialogResponse(CompoundBinaryTag values) {
    /**
     * A response carrying nothing, as sent by a dialog with no input control.
     *
     * @since 0.1.0
     */
    public static final DialogResponse EMPTY = new DialogResponse(CompoundBinaryTag.empty());

    /**
     * @param values the raw payload received from the client
     * @since 0.1.0
     */
    public DialogResponse {
        Preconditions.checkArgument(values != null, "The values of a dialog response must not be null");
    }

    /**
     * Reads a value as text.
     *
     * <p>Numbers and booleans are rendered as their string form rather than rejected.</p>
     *
     * @param key the input key to read
     * @return the value, or empty when the payload holds nothing under that key
     * @since 0.1.0
     */
    @Contract(pure = true)
    public Optional<String> text(final String key) {
        final BinaryTag tag = values.get(key);
        return switch (tag) {
            case final StringBinaryTag string -> Optional.of(string.value());
            case final NumberBinaryTag number -> Optional.of(
                    tag.type() == BinaryTagTypes.BYTE || tag.type() == BinaryTagTypes.SHORT
                            || tag.type() == BinaryTagTypes.INT || tag.type() == BinaryTagTypes.LONG
                            ? Long.toString(number.longValue())
                            : Double.toString(number.doubleValue()));
            case null, default -> Optional.empty();
        };
    }

    /**
     * Reads a value as a flag.
     *
     * <p>A checkbox arrives as a byte, but a text input configured with
     * {@link DialogInput.Bool#values(String, String) custom strings} arrives as text, so
     * {@code "true"} and {@code "1"} are accepted too.</p>
     *
     * @param key the input key to read
     * @return the value, or empty when the payload holds nothing readable under that key
     * @since 0.1.0
     */
    @Contract(pure = true)
    public Optional<Boolean> bool(final String key) {
        final BinaryTag tag = values.get(key);
        return switch (tag) {
            case null -> Optional.empty();
            case final NumberBinaryTag number -> Optional.of(number.intValue() != 0);
            case final StringBinaryTag string -> {
                final String value = string.value();
                if (value.equalsIgnoreCase("true") || value.equals("1")) {
                    yield Optional.of(true);
                }
                if (value.equalsIgnoreCase("false") || value.equals("0")) {
                    yield Optional.of(false);
                }
                yield Optional.empty();
            }
            default -> Optional.empty();
        };
    }

    /**
     * Reads a value as a number.
     *
     * @param key the input key to read
     * @return the value, or empty when the payload holds nothing numeric under that key
     * @since 0.1.0
     */
    @Contract(pure = true)
    public OptionalDouble number(final String key) {
        final BinaryTag tag = values.get(key);
        return switch (tag) {
            case final NumberBinaryTag number -> OptionalDouble.of(number.doubleValue());
            case final StringBinaryTag string -> {
                try {
                    yield OptionalDouble.of(Double.parseDouble(string.value()));
                } catch (final NumberFormatException ignored) {
                    yield OptionalDouble.empty();
                }
            }
            case null, default -> OptionalDouble.empty();
        };
    }

    /**
     * {@return whether the payload holds anything under {@code key}}
     *
     * @param key the input key to look up
     * @since 0.1.0
     */
    @Contract(pure = true)
    public boolean contains(final String key) {
        return values.get(key) != null;
    }

    /**
     * {@return every key the payload carries}
     *
     * @since 0.1.0
     */
    @Contract(pure = true)
    public Set<String> keys() {
        return values.keySet();
    }

    /**
     * {@return whether the payload is empty}
     *
     * @since 0.1.0
     */
    @Contract(pure = true)
    public boolean isEmpty() {
        return values.size() == 0;
    }
}
