package fr.euphyllia.fidorial.server.codecs.networking;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.utils.PositionData;
import fr.fidorial.world.BlockPos;
import net.kyori.adventure.key.Key;

import java.util.List;
import java.util.Optional;

public final class CommonNetworkCodecs {

    private CommonNetworkCodecs() {
        throw new UnsupportedOperationException("CommonNetworkCodecs cannot be instantiated.");
    }

    /**
     * Codecs for protocol primitives and the small composites built from them.
     */
    public static final class PrimitiveCodecs {

        public static final NetworkCodec<PacketBuffer, Boolean> BOOL = NetworkCodec.of(PacketBuffer::readBoolean, PacketBuffer::writeBoolean);
        public static final NetworkCodec<PacketBuffer, Integer> INT = NetworkCodec.of(PacketBuffer::readInt, PacketBuffer::writeInt);
        public static final NetworkCodec<PacketBuffer, Integer> VAR_INT = NetworkCodec.of(PacketBuffer::readVarInt, PacketBuffer::writeVarInt);
        public static final NetworkCodec<PacketBuffer, Long> LONG = NetworkCodec.of(PacketBuffer::readLong, PacketBuffer::writeLong);
        public static final NetworkCodec<PacketBuffer, Float> FLOAT = NetworkCodec.of(PacketBuffer::readFloat, PacketBuffer::writeFloat);
        public static final NetworkCodec<PacketBuffer, Double> DOUBLE = NetworkCodec.of(PacketBuffer::readDouble, PacketBuffer::writeDouble);

        /**
         * A block position packed into one long.
         */
        public static final NetworkCodec<PacketBuffer, BlockPos> BLOCK_POS =
                NetworkCodec.of(PacketBuffer::readPosition, (buf, pos) -> buf.writePosition(pos.x(), pos.y(), pos.z()));

        /**
         * Three doubles.
         */
        public static final NetworkCodec<PacketBuffer, PositionData.Vec3D> VEC3D =
                NetworkCodec.of(PositionData.Vec3D::readFrom, (buf, vec) -> vec.writeTo(buf));

        private PrimitiveCodecs() {
            throw new UnsupportedOperationException("PrimitiveCodecs cannot be instantiated.");
        }

        /**
         * A VarInt size followed by the elements.
         */
        public static <T> NetworkCodec<PacketBuffer, List<T>> prefixedList(final NetworkCodec<PacketBuffer, T> element, final int maxSize) {
            return element.listOf(PacketBuffer::readVarInt, PacketBuffer::writeVarInt, maxSize);
        }

        /**
         * A boolean presence flag followed by the value when present.
         */
        public static <T> NetworkCodec<PacketBuffer, Optional<T>> prefixedOptional(final NetworkCodec<PacketBuffer, T> value) {
            return value.optional(PacketBuffer::readBoolean, PacketBuffer::writeBoolean);
        }
    }

    /**
     * Codecs for string-like values, such as {@linkplain String} itself and {@linkplain Key}.
     */
    public static final class StringLikeCodecs {

        /**
         * The protocol's default maximum string length, in characters.
         */
        public static final int MAX_STRING_LENGTH = 32767;

        public static final NetworkCodec<PacketBuffer, String> STRING = string(MAX_STRING_LENGTH);

        /**
         * A key sent as one {@code namespace:value} string.
         */
        public static final NetworkCodec<PacketBuffer, Key> KEY = STRING.xmap(Key::key, Key::asString);

        /**
         * A key sent as two strings, namespace then value.
         */
        public static final NetworkCodec<PacketBuffer, Key> SPLIT_KEY = splitKey(MAX_STRING_LENGTH);

        private StringLikeCodecs() {
            throw new UnsupportedOperationException("StringLikeCodecs cannot be instantiated.");
        }

        public static NetworkCodec<PacketBuffer, String> string(final int maxLength) {
            return NetworkCodec.of(
                    buf -> buf.readString(maxLength),
                    (buf, value) -> {
                        if (value.length() > maxLength) {
                            throw new IllegalArgumentException("string length " + value.length() + " exceeds " + maxLength);
                        }
                        buf.writeString(value);
                    });
        }

        /**
         * A key sent as two strings, namespace then value, each at most {@code maxLength} characters.
         */
        public static NetworkCodec<PacketBuffer, Key> splitKey(final int maxLength) {
            final NetworkCodec<PacketBuffer, String> part = string(maxLength);
            return NetworkCodec.of(
                    buf -> {
                        final String namespace = part.read(buf);
                        final String value = part.read(buf);
                        return Key.key(namespace, value);
                    },
                    (buf, key) -> {
                        part.write(buf, key.namespace());
                        part.write(buf, key.value());
                    });
        }
    }
}
