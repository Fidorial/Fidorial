package fr.fidorial.world.block;

import java.util.List;

/**
 * A property of a block type and the values it accepts.
 *
 * @param name   the property name, for instance {@code facing}
 * @param values the accepted values, never empty
 * @since 0.1.0
 */
public record BlockProperty(String name, List<String> values) {

    public BlockProperty {
        values = List.copyOf(values);
        if (values.isEmpty()) {
            throw new IllegalArgumentException("Property '" + name + "' must have at least one value");
        }
    }

    /**
     * {@return the index of a value, or {@code -1} if it is not accepted}
     *
     * @param value the value
     * @since 0.1.0
     */
    public int indexOf(final String value) {
        return values.indexOf(value);
    }

    /**
     * {@return {@code true} if the value is accepted}
     *
     * @param value the value
     * @since 0.1.0
     */
    public boolean isValid(final String value) {
        return values.contains(value);
    }
}
