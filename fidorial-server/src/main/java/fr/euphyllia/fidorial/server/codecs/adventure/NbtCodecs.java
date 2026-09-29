package fr.euphyllia.fidorial.server.codecs.adventure;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import io.papermc.adventurex.nbt.dfu.BinaryTagOps;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.nbt.TagStringIO;
import net.kyori.adventure.nbt.api.BinaryTagHolder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public final class NbtCodecs {

    public static final Codec<CompoundBinaryTag> COMPOUND_BINARY_TAG_CODEC = new Codec<>() {
        @Override
        public <T> DataResult<Pair<CompoundBinaryTag, T>> decode(final DynamicOps<T> ops, final T input) {
            final BinaryTag tag;
            try {
                tag = ops.convertTo(BinaryTagOps.binaryTagOps(), input);
            } catch (final Exception e) {
                return DataResult.error(() -> "Failed to convert to a binary tag: " + e.getMessage());
            }
            if (!(tag instanceof final CompoundBinaryTag compound)) {
                return DataResult.error(() -> "Expected a compound tag, got: " + tag);
            }
            return DataResult.success(Pair.of(compound, input));
        }

        @Override
        public <T> DataResult<T> encode(final CompoundBinaryTag input, final DynamicOps<T> ops, final T prefix) {
            try {
                return DataResult.success(BinaryTagOps.binaryTagOps().convertTo(ops, input));
            } catch (final Exception e) {
                return DataResult.error(() -> "Failed to encode compound tag: " + e.getMessage());
            }
        }
    };

    private static final net.kyori.adventure.util.Codec<CompoundBinaryTag, String, IOException, IOException> SNBT_BRIDGE_CODEC =
            new net.kyori.adventure.util.Codec<>() {
                @Override
                public CompoundBinaryTag decode(final String encoded) throws IOException {
                    return TagStringIO.tagStringIO().asCompound(encoded);
                }

                @Override
                public String encode(final CompoundBinaryTag decoded) throws IOException {
                    return TagStringIO.tagStringIO().asString(decoded);
                }
            };

    public static final Codec<BinaryTagHolder> COMPOUND_BINARY_TAG_HOLDER_CODEC =
            COMPOUND_BINARY_TAG_CODEC.flatComapMap(
                    tag -> {
                        try {
                            return BinaryTagHolder.encode(tag, SNBT_BRIDGE_CODEC);
                        } catch (final IOException e) {
                            // structurally should never actually fail.
                            throw new IllegalStateException(e);
                        }
                    },
                    holder -> {
                        try {
                            return DataResult.success(holder.get(SNBT_BRIDGE_CODEC));
                        } catch (final IOException e) {
                            return DataResult.error(() -> "Invalid SNBT payload: " + e.getMessage());
                        }
                    }
            );

    public static final Codec<BinaryTag> BINARY_TAG_CODEC = new Codec<>() {
        @Override
        public <T> DataResult<Pair<BinaryTag, T>> decode(final DynamicOps<T> ops, final T input) {
            try {
                return DataResult.success(Pair.of(toTag(ops, input), input));
            } catch (final Exception e) {
                return DataResult.error(() -> "Failed to convert to a binary tag: " + e.getMessage());
            }
        }

        @Override
        public <T> DataResult<T> encode(final BinaryTag input, final DynamicOps<T> ops, final T prefix) {
            try {
                return DataResult.success(BinaryTagOps.binaryTagOps().convertTo(ops, input));
            } catch (final Exception e) {
                return DataResult.error(() -> "Failed to encode tag: " + e.getMessage());
            }
        }
    };

    private static <T> BinaryTag toTag(final DynamicOps<T> ops, final T input) {
        final DynamicOps<BinaryTag> out = BinaryTagOps.binaryTagOps();

        final Optional<Stream<T>> list = ops.getStream(input).result();
        if (list.isPresent()) {
            final List<BinaryTag> elements = new ArrayList<>();
            list.get().forEach(element -> elements.add(toTag(ops, element)));
            return elements.isEmpty()
                    ? ListBinaryTag.empty()
                    : ListBinaryTag.heterogeneousListBinaryTag().add(elements).build();
        }

        final Optional<MapLike<T>> map = ops.getMap(input).result();
        if (map.isPresent()) {
            final RecordBuilder<BinaryTag> builder = out.mapBuilder();
            map.get().entries().forEach(entry ->
                    builder.add(toTag(ops, entry.getFirst()), toTag(ops, entry.getSecond())));
            return builder.build(out.empty()).getOrThrow(IllegalStateException::new);
        }

        return ops.convertTo(out, input);
    }
}
