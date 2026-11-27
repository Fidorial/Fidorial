package fr.fidorial.world.block;

import com.google.common.base.Preconditions;

/// The value one property takes in a block state.
///
/// @param property the property
/// @param value    one of its values
/// @param <T>      the type of the values
/// @since 0.1.0
public record PropertyValue<T>(BlockProperty<T> property, T value) {

    public PropertyValue {
        Preconditions.checkArgument(property != null, "The property of a property value must not be null");
        Preconditions.checkArgument(property.indexOf(value) >= 0,
                "%s is not a value of the property '%s'", value, property.name());
    }

    /// {@return the value as written in a block state}
    ///
    /// @since 0.1.0
    public String valueName() {
        return property.nameOf(value);
    }
}
