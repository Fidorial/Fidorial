package fr.euphyllia.fidorial.server.codecs;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkCodec;
import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.registry.ServerRegistry;
import fr.fidorial.registry.Reference;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;

/**
 * Codecs for {@link TypedKey} and {@link Reference}.
 */
public final class ReferenceCodecs {

    private ReferenceCodecs() {
        throw new UnsupportedOperationException("ReferenceCodecs cannot be instantiated.");
    }

    /**
     * {@return a key of {@code registry}, as a {@code namespace:value} string}
     */
    public static <T> Codec<TypedKey<T>> key(final RegistryKey<T> registry) {
        return CommonCodecs.KEY_CODEC.xmap(key -> TypedKey.withRegistry(registry, key), TypedKey::key);
    }

    /**
     * {@return a reference: a key string, or the value inline}
     *
     * @param inline the codec of a value given inline
     */
    public static <T> Codec<Reference<T>> reference(final RegistryKey<T> registry, final Codec<T> inline) {
        return Codec.either(key(registry), inline).xmap(
                either -> either.<Reference<T>>map(key -> key, Reference::inline),
                reference -> switch (reference) {
                    case final TypedKey<T> key -> Either.left(key);
                    case final Reference.Inline<T> value -> Either.right(value.value());
                });
    }

    /**
     * {@return a key of {@code registry}, as its network ID}
     */
    public static <T> NetworkCodec<PacketBuffer, TypedKey<T>> networkKey(final ServerRegistry<T> registry) {
        return NetworkCodec.of(
                buf -> byNetworkId(registry, buf.readVarInt()),
                (buf, key) -> buf.writeVarInt(networkId(registry, key)));
    }

    /**
     * {@return a reference: its network ID plus one, or {@code 0} followed by the value inline}
     *
     * @param inline the codec of a value given inline
     */
    public static <T> NetworkCodec<PacketBuffer, Reference<T>> networkReference(
            final ServerRegistry<T> registry, final NetworkCodec<PacketBuffer, T> inline) {
        return NetworkCodec.of(
                buf -> {
                    final int id = buf.readVarInt();
                    return id == 0 ? Reference.inline(inline.read(buf)) : byNetworkId(registry, id - 1);
                },
                (buf, reference) -> {
                    switch (reference) {
                        case final TypedKey<T> key -> buf.writeVarInt(networkId(registry, key) + 1);
                        case final Reference.Inline<T> value -> {
                            buf.writeVarInt(0);
                            inline.write(buf, value.value());
                        }
                    }
                });
    }

    private static <T> int networkId(final ServerRegistry<T> registry, final TypedKey<T> key) {
        final int id = registry.networkId(key);
        if (id < 0) {
            throw new IllegalArgumentException("Nothing is registered under " + key.asString() + " in " + registry.name().asString());
        }
        return id;
    }

    private static <T> TypedKey<T> byNetworkId(final ServerRegistry<T> registry, final int id) {
        final TypedKey<T> key = registry.byNetworkId(id);
        if (key == null) {
            throw new IllegalArgumentException("No entry with the network ID " + id + " in " + registry.name().asString());
        }
        return key;
    }
}
