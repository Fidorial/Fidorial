package fr.euphyllia.fidorial.server.world.storage.util;

import com.mojang.serialization.Codec;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import io.papermc.adventurex.nbt.dfu.BinaryTagOps;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.DoubleBinaryTag;
import net.kyori.adventure.nbt.FloatBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import org.jetbrains.annotations.Contract;

import java.util.List;

/// Contains utilities for writing and reading [BinaryTag]s.
@SuppressWarnings("unused")
public final class BinaryTags {

    private BinaryTags() {
    }

    /// Writes `values` into a new [ListBinaryTag], without boxing.
    @Contract(pure = true)
    public static ListBinaryTag doubleList(final double... values) {
        final ListBinaryTag.Builder<BinaryTag> builder = ListBinaryTag.builder(values.length);
        for (final double value : values) {
            builder.add(DoubleBinaryTag.doubleBinaryTag(value));
        }
        return builder.build();
    }

    /// Writes `values` into a new [ListBinaryTag], without boxing.
    @Contract(pure = true)
    public static ListBinaryTag floatList(final float... values) {
        final ListBinaryTag.Builder<BinaryTag> builder = ListBinaryTag.builder(values.length);
        for (final float value : values) {
            builder.add(FloatBinaryTag.floatBinaryTag(value));
        }
        return builder.build();
    }

    /// Writes `values` into a new [ListBinaryTag].
    /// @throws EncoderException when a value fails to encode.
    @Contract(pure = true)
    public static <T> ListBinaryTag writeList(final List<T> values, final Codec<T> codec) {
        final BinaryTag encoded = codec.listOf()
                .encodeStart(BinaryTagOps.binaryTagOps(), values)
                .getOrThrow(EncoderException::new);
        if (!(encoded instanceof final ListBinaryTag list)) {
            throw new EncoderException("A list codec encoded a " + encoded.type() + " instead of a list");
        }
        return list;
    }

    /// Decodes elements of a [ListBinaryTag] into `out`.
    /// @throws DecoderException when an element fails to decode.
    public static <T> void readList(final ListBinaryTag list, final List<T> out, final Codec<T> codec) {
        for (final BinaryTag tag : list) {
            out.add(codec.parse(BinaryTagOps.binaryTagOps(), tag)
                    .getOrThrow(DecoderException::new));
        }
    }

    /// Decodes elements of a [ListBinaryTag] into `out`, skipping entries that fail to decode.
    public static <T> void readListLenient(final ListBinaryTag list, final List<T> out, final Codec<T> codec) {
        for (final BinaryTag tag : list) {
            codec.parse(BinaryTagOps.binaryTagOps(), tag)
                    .result()
                    .ifPresent(out::add);
        }
    }

    /// Gets the double at the `index`.
    ///
    /// Returns `0.0` instead of throwing when the index is out of bounds or the element is not a double.
    @Contract(pure = true)
    public static double doubleAt(final ListBinaryTag list, final int index) {
        if (index < 0 || index >= list.size()) {
            return 0.0;
        }
        return list.get(index) instanceof final DoubleBinaryTag tag ? tag.doubleValue() : 0.0;
    }

    /// Gets the float at the `index`.
    ///
    /// Returns `0f` instead of throwing when the index is out of bounds or the element is not a float.
    @Contract(pure = true)
    public static float floatAt(final ListBinaryTag list, final int index) {
        if (index < 0 || index >= list.size()) {
            return 0f;
        }
        return list.get(index) instanceof final FloatBinaryTag tag ? tag.floatValue() : 0f;
    }
}
