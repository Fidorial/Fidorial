package fr.euphyllia.fidorial.server.registry;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import io.papermc.adventurex.nbt.dfu.BinaryTagOps;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;

import java.util.Objects;
import java.util.function.Function;

/**
 * Reads the values of a data registry from their data pack JSON, and writes them as sent to clients.
 *
 * @param <T> the value type
 */
public interface RegistryCodec<T> {

    /**
     * @throws IllegalArgumentException if the JSON is invalid
     */
    T decode(Key key, JsonElement json);

    /**
     * {@return the NBT sent to clients in the registry data}
     *
     * @throws IllegalArgumentException if the value cannot be encoded
     */
    BinaryTag encode(Key key, T value);

    /**
     * {@return a registry codec backed by a DFU codec}
     *
     * @param codec the codec of the value registered under a key
     */
    static <T> RegistryCodec<T> of(final Function<Key, Codec<T>> codec) {
        Objects.requireNonNull(codec, "codec");
        return new RegistryCodec<>() {
            @Override
            public T decode(final Key key, final JsonElement json) {
                return codec.apply(key).parse(JsonOps.INSTANCE, json)
                        .getOrThrow(message -> new IllegalArgumentException("Invalid " + key.asString() + ": " + message));
            }

            @Override
            public BinaryTag encode(final Key key, final T value) {
                return codec.apply(key).encodeStart(BinaryTagOps.binaryTagOps(), value)
                        .getOrThrow(message -> new IllegalArgumentException("Cannot encode " + key.asString() + ": " + message));
            }
        };
    }
}
