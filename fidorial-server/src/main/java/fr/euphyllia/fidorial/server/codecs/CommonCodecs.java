package fr.euphyllia.fidorial.server.codecs;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.util.TriState;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class CommonCodecs {

    private CommonCodecs() {
    }

    public static final Codec<UUID> UUID_CODEC = Codec.INT_STREAM.comapFlatMap(
            stream -> {
                final int[] i = stream.toArray();
                if (i.length != 4) {
                    return DataResult.error(() -> "Expected 4 ints for a UUID, got " + i.length);
                }
                return DataResult.success(new UUID(
                        ((long) i[0] << 32) | (i[1] & 0xFFFFFFFFL),
                        ((long) i[2] << 32) | (i[3] & 0xFFFFFFFFL)));
            },
            uuid -> IntStream.of(
                    (int) (uuid.getMostSignificantBits() >> 32),
                    (int) uuid.getMostSignificantBits(),
                    (int) (uuid.getLeastSignificantBits() >> 32),
                    (int) uuid.getLeastSignificantBits())
    );

    public static final Codec<UUID> UUID_STRING_CODEC = Codec.STRING.comapFlatMap(uuid -> {
        try {
            return DataResult.success(UUID.fromString(uuid.strip()));
        } catch (final IllegalArgumentException e) {
            return DataResult.error(() -> "Not a valid UUID: " + uuid);
    }}, UUID::toString);

    public static final Codec<Key> KEY_CODEC = Codec.STRING.comapFlatMap(
            s -> Key.parseable(s)
                    ? DataResult.success(Key.key(s))
                    : DataResult.error(() -> "Not a valid key: " + s),
            Key::asString
    );

    public static final Codec<Integer> RGB_COLOR = Codec.either(Codec.STRING, Codec.INT)
            .comapFlatMap(
                    either -> either.map(CommonCodecs::parseHexColor, DataResult::success),
                    color -> Either.left(formatHexColor(color)));

    public static final Codec<Integer> ARGB_COLOR = Codec.either(Codec.STRING, Codec.INT)
            .comapFlatMap(
                    either -> either.map(CommonCodecs::parseHexColorARGB, DataResult::success),
                    color -> Either.left(formatHexColorARGB(color)));

    public static final Codec<TriState> TRI_STATE = Codec.STRING.comapFlatMap(
            name -> switch (name) {
                case "true" -> DataResult.success(TriState.TRUE);
                case "false" -> DataResult.success(TriState.FALSE);
                case "not_set" -> DataResult.success(TriState.NOT_SET);
                default -> DataResult.error(() -> "Unknown tri-state value: " + name);
            },
            state -> switch (state) {
                case TRUE -> "true";
                case FALSE -> "false";
                case NOT_SET -> "not_set";
            });

    public static final Codec<Integer> POSITIVE_INT = Codec.intRange(1, Integer.MAX_VALUE);
    public static final Codec<Integer> NON_NEGATIVE_INT = Codec.intRange(0, Integer.MAX_VALUE);

    private static String formatHexColor(final int color) {
        return String.format("#%06x", color & 0xFFFFFF);
    }

    private static DataResult<Integer> parseHexColor(final String value) {
        final String digits = value.startsWith("#") ? value.substring(1) : value;
        if (digits.length() != 6) {
            return DataResult.error(() -> "Expected a #rrggbb color, got " + value);
        }
        try {
            return DataResult.success(Integer.parseInt(digits, 16));
        } catch (final NumberFormatException exception) {
            return DataResult.error(() -> "Malformed hexadecimal color: " + value);
        }
    }

    private static String formatHexColorARGB(final int color) {
        return String.format("#%08x", color);
    }

    private static DataResult<Integer> parseHexColorARGB(final String value) {
        final String digits = value.startsWith("#") ? value.substring(1) : value;
        if (digits.length() != 8) {
            return DataResult.error(() -> "Expected a #aarrggbb color, got " + value);
        }
        try {
            return DataResult.success((int) Long.parseLong(digits, 16));
        } catch (final NumberFormatException exception) {
            return DataResult.error(() -> "Malformed hexadecimal color: " + value);
        }
    }

    public static final Codec<String> NON_BLANK_STRING = Codec.STRING.validate(check(value -> !value.isBlank(), _ -> "Must not be blank"));

    public static <T> Function<T, DataResult<T>> check(final Predicate<? super T> test, final Function<? super T, String> message) {
        return value -> test.test(value) ? DataResult.success(value) : DataResult.error(() -> message.apply(value));
    }

    public static <T> Codec<T> dispatch(final String typeKey, final Function<? super T, String> type, final Map<String, ? extends MapCodec<? extends T>> variants) {
        final String expected = variants.keySet().stream().sorted().collect(Collectors.joining(", "));
        return Codec.STRING.partialDispatch(typeKey,
                value -> DataResult.success(type.apply(value)),
                name -> {
                    final MapCodec<? extends T> codec = variants.get(name);
                    return codec != null
                            ? DataResult.success(codec)
                            : DataResult.error(() -> "Unknown " + typeKey + " '" + name + "', expected one of: " + expected);
                });
    }
}
