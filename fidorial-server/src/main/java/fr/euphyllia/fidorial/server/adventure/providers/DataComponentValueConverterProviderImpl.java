package fr.euphyllia.fidorial.server.adventure.providers;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import fr.euphyllia.fidorial.server.codecs.adventure.NbtCodecs;
import fr.euphyllia.fidorial.server.codecs.item.ItemStackCodecs;
import fr.fidorial.item.data.DataComponentType;
import fr.fidorial.item.data.DataComponentTypes;
import fr.fidorial.item.data.TypedDataComponentValue;
import io.papermc.adventurex.nbt.dfu.BinaryTagOps;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.event.DataComponentValueConverterRegistry;
import net.kyori.adventure.text.serializer.gson.GsonDataComponentValue;

import java.util.List;

/**
 * Converts the {@link TypedDataComponentValue}s of item hover events to and from the data component
 * values of the Adventure serializers: {@link BinaryTagHolder} for MiniMessage and
 * {@link GsonDataComponentValue} for the Gson serializer.
 */
public final class DataComponentValueConverterProviderImpl implements DataComponentValueConverterRegistry.Provider {

    private static final Key ID = Key.key("fidorial", "data_component_value");

    @Override
    public Key id() {
        return ID;
    }

    @Override
    public Iterable<DataComponentValueConverterRegistry.Conversion<?, ?>> conversions() {
        return List.of(
                DataComponentValueConverterRegistry.Conversion.convert(
                        TypedDataComponentValue.class, BinaryTagHolder.class,
                        (key, value) -> NbtCodecs.toHolder(orThrow(key,
                                ItemStackCodecs.encodeValue(BinaryTagOps.binaryTagOps(), value)))),
                DataComponentValueConverterRegistry.Conversion.convert(
                        TypedDataComponentValue.class, GsonDataComponentValue.class,
                        (key, value) -> GsonDataComponentValue.gsonDataComponentValue(orThrow(key,
                                ItemStackCodecs.encodeValue(JsonOps.INSTANCE, value)))),
                DataComponentValueConverterRegistry.Conversion.convert(
                        BinaryTagHolder.class, TypedDataComponentValue.class,
                        (key, holder) -> orThrow(key, ItemStackCodecs.decodeValue(
                                BinaryTagOps.binaryTagOps(), type(key), NbtCodecs.fromHolder(holder)))),
                DataComponentValueConverterRegistry.Conversion.convert(
                        GsonDataComponentValue.class, TypedDataComponentValue.class,
                        (key, value) -> orThrow(key, ItemStackCodecs.decodeValue(
                                JsonOps.INSTANCE, type(key), value.element())))
        );
    }

    private static DataComponentType<?> type(final Key key) {
        final DataComponentType<?> type = DataComponentTypes.byKey(key);
        if (type == null) {
            throw new IllegalArgumentException("Unknown data component " + key.asString());
        }
        return type;
    }

    private static <T> T orThrow(final Key key, final DataResult<T> result) {
        return result.getOrThrow(message -> new IllegalArgumentException(
                "Invalid value for the data component " + key.asString() + ": " + message));
    }
}
