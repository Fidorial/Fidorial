package fr.euphyllia.fidorial.server.codecs.networking;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import net.kyori.adventure.key.Key;

public final class CommonNetworkCodecs {

    private CommonNetworkCodecs() {
        throw new UnsupportedOperationException("CommonNetworkCodecs cannot be instantiated.");
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
