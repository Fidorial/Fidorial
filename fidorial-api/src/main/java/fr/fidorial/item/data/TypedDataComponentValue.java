package fr.fidorial.item.data;

import fr.fidorial.item.ItemStack;
import net.kyori.adventure.text.event.DataComponentValue;
import net.kyori.adventure.text.event.DataComponentValueConverterRegistry;
import net.kyori.adventure.text.event.HoverEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * The value of a data component, as held by an {@linkplain HoverEvent.ShowItem item hover event}.
 *
 * <p>The server registers conversions between this value and the data component values of the
 * Adventure serializers, such as {@link net.kyori.adventure.nbt.api.BinaryTagHolder} for MiniMessage, in
 * the {@link DataComponentValueConverterRegistry}.</p>
 *
 * @param <T> the value type of the component
 * @see ItemStack#asHoverEvent(java.util.function.UnaryOperator)
 * @see ItemStack#of(HoverEvent.ShowItem)
 * @since 0.1.0
 */
@ApiStatus.NonExtendable
public interface TypedDataComponentValue<T> extends DataComponentValue {

    /**
     * Creates a data component value.
     *
     * @param type  the component
     * @param value the value of the component
     * @param <T>   the value type of the component
     * @return the data component value
     * @throws IllegalArgumentException if {@code value} is not an instance of the value type of
     *                                  {@code type}
     * @since 0.1.0
     */
    static <T> TypedDataComponentValue<T> of(final DataComponentType<T> type, final T value) {
        return new TypedDataComponentValueImpl<>(type, value);
    }

    /**
     * {@return the component}
     *
     * @since 0.1.0
     */
    DataComponentType<T> type();

    /**
     * {@return the value of the component}
     *
     * @since 0.1.0
     */
    T value();
}
