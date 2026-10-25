package fr.fidorial.item.data;

import com.google.common.base.Preconditions;

record TypedDataComponentValueImpl<T>(DataComponentType<T> type, T value) implements TypedDataComponentValue<T> {

    TypedDataComponentValueImpl {
        Preconditions.checkArgument(type != null, "The type of a data component value must not be null");
        Preconditions.checkArgument(value != null, "The value of the data component %s must not be null", type);
        Preconditions.checkArgument(type.valueType().isInstance(value),
                "The value of the data component %s must be a %s, got %s", type, type.valueType().getName(), value);
    }
}
