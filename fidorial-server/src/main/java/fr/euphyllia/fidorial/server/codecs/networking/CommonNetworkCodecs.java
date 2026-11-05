package fr.euphyllia.fidorial.server.codecs.networking;

import fr.euphyllia.fidorial.server.network.protocol.packet.util.PositionData;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
import fr.fidorial.math.Position;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class CommonNetworkCodecs {

    private CommonNetworkCodecs() {
        throw new UnsupportedOperationException("CommonNetworkCodecs cannot be instantiated.");
    }

    /**
     * Codecs for protocol primitives and the small composites built from them.
     */
    public static final class PrimitiveCodecs {

        public static final NetworkCodec<PacketBuffer, Boolean> BOOL = NetworkCodec.of(PacketBuffer::readBoolean, PacketBuffer::writeBoolean);
        public static final NetworkCodec<PacketBuffer, Byte> BYTE = NetworkCodec.of(PacketBuffer::readByte, PacketBuffer::writeByte);
        public static final NetworkCodec<PacketBuffer, Integer> UNSIGNED_BYTE = NetworkCodec.of(PacketBuffer::readUByte, PacketBuffer::writeByte);
        public static final NetworkCodec<PacketBuffer, Short> SHORT = NetworkCodec.of(PacketBuffer::readShort, PacketBuffer::writeShort);
        public static final NetworkCodec<PacketBuffer, Integer> UNSIGNED_SHORT = NetworkCodec.of(PacketBuffer::readUShort, PacketBuffer::writeShort);
        public static final NetworkCodec<PacketBuffer, Integer> INT = NetworkCodec.of(PacketBuffer::readInt, PacketBuffer::writeInt);
        public static final NetworkCodec<PacketBuffer, Integer> VAR_INT = NetworkCodec.of(PacketBuffer::readVarInt, PacketBuffer::writeVarInt);
        public static final NetworkCodec<PacketBuffer, Long> LONG = NetworkCodec.of(PacketBuffer::readLong, PacketBuffer::writeLong);
        public static final NetworkCodec<PacketBuffer, Long> VAR_LONG = NetworkCodec.of(PacketBuffer::readVarLong, PacketBuffer::writeVarLong);
        public static final NetworkCodec<PacketBuffer, Float> FLOAT = NetworkCodec.of(PacketBuffer::readFloat, PacketBuffer::writeFloat);
        public static final NetworkCodec<PacketBuffer, Double> DOUBLE = NetworkCodec.of(PacketBuffer::readDouble, PacketBuffer::writeDouble);
        public static final NetworkCodec<PacketBuffer, UUID> UUID = NetworkCodec.of(PacketBuffer::readUuid, PacketBuffer::writeUuid);

        /**
         * A rotation in degrees, packed into one byte.
         */
        public static final NetworkCodec<PacketBuffer, Float> ANGLE =
                NetworkCodec.of(buf -> buf.readByte() * 360f / 256f, PacketBuffer::writeAngle);

        /**
         * A VarInt length followed by the bytes.
         */
        public static final NetworkCodec<PacketBuffer, byte[]> BYTE_ARRAY = byteArray(Integer.MAX_VALUE);

        /**
         * Every byte left in the packet, unprefixed; only valid as the last field.
         */
        public static final NetworkCodec<PacketBuffer, byte[]> REMAINING_BYTES =
                NetworkCodec.of(PacketBuffer::readRemainingBytes, PacketBuffer::writeRawBytes);

        /**
         * A VarInt size followed by VarInts.
         */
        public static final NetworkCodec<PacketBuffer, int[]> VAR_INT_ARRAY = NetworkCodec.of(
                buf -> {
                    final int[] values = new int[buf.readVarInt()];
                    for (int i = 0; i < values.length; i++) {
                        values[i] = buf.readVarInt();
                    }
                    return values;
                },
                PacketBuffer::writeVarIntArray);

        /**
         * A block position packed into one long.
         */
        public static final NetworkCodec<PacketBuffer, Position> BLOCK_POS =
                NetworkCodec.of(PacketBuffer::readPosition, (buf, pos) -> buf.writePosition(pos.blockX(), pos.blockY(), pos.blockZ()));

        /**
         * Three doubles.
         */
        public static final NetworkCodec<PacketBuffer, PositionData.Vec3D> VEC3D =
                NetworkCodec.of(PositionData.Vec3D::readFrom, (buf, vec) -> vec.writeTo(buf));

        private PrimitiveCodecs() {
            throw new UnsupportedOperationException("PrimitiveCodecs cannot be instantiated.");
        }

        public static NetworkCodec<PacketBuffer, byte[]> byteArray(final int maxLength) {
            return NetworkCodec.of(buf -> buf.readByteArray(maxLength), PacketBuffer::writeByteArray);
        }

        /**
         * A VarInt size followed by the elements.
         */
        public static <B extends PacketBuffer, T> NetworkCodec<B, List<T>> prefixedList(final NetworkCodec<B, T> element, final int maxSize) {
            return element.listOf(PacketBuffer::readVarInt, PacketBuffer::writeVarInt, maxSize);
        }

        /**
         * A boolean presence flag followed by the value when it isn't {@code null}.
         */
        public static <B extends PacketBuffer, T> NetworkEncoder<B, @Nullable T> prefixedNullable(final NetworkEncoder<B, T> value) {
            return value.nullable(PacketBuffer::writeBoolean);
        }

        /**
         * An enum constant sent as its ordinal, in a VarInt.
         */
        public static <E extends Enum<E>> NetworkCodec<PacketBuffer, E> enumByOrdinal(final Class<E> type) {
            final E[] values = type.getEnumConstants();
            return VAR_INT.xmap(
                    ordinal -> {
                        if (ordinal < 0 || ordinal >= values.length) {
                            throw new IllegalArgumentException(type.getSimpleName() + " ordinal " + ordinal + " out of range");
                        }
                        return values[ordinal];
                    },
                    Enum::ordinal);
        }

        /**
         * A boolean presence flag followed by the value when present.
         */
        public static <B extends PacketBuffer, T> NetworkCodec<B, Optional<T>> prefixedOptional(final NetworkCodec<B, T> value) {
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

        /**
         * A VarInt size followed by the keys.
         */
        public static final NetworkCodec<PacketBuffer, Key[]> KEY_ARRAY = NetworkCodec.of(
                buf -> {
                    final Key[] keys = new Key[buf.readVarInt()];
                    for (int i = 0; i < keys.length; i++) {
                        keys[i] = buf.readKey();
                    }
                    return keys;
                },
                PacketBuffer::writeKeyArray);

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

    /**
     * Codecs for Adventure text.
     */
    public static final class TextCodecs {

        /**
         * The largest component NBT accepted when reading, in bytes.
         */
        public static final int MAX_COMPONENT_BYTES = 2_097_152;

        /**
         * A component sent as network NBT.
         */
        public static final NetworkCodec<PacketBuffer, Component> COMPONENT =
                NetworkCodec.of(buf -> buf.readComponent(MAX_COMPONENT_BYTES), PacketBuffer::writeComponent);

        private TextCodecs() {
            throw new UnsupportedOperationException("TextCodecs cannot be instantiated.");
        }
    }
}
