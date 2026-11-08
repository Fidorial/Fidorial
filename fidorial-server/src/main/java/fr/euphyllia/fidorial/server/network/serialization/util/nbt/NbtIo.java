package fr.euphyllia.fidorial.server.network.serialization.util.nbt;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.BinaryTagType;
import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.ByteArrayBinaryTag;
import net.kyori.adventure.nbt.ByteBinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.DoubleBinaryTag;
import net.kyori.adventure.nbt.EndBinaryTag;
import net.kyori.adventure.nbt.FloatBinaryTag;
import net.kyori.adventure.nbt.IntArrayBinaryTag;
import net.kyori.adventure.nbt.IntBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.nbt.LongArrayBinaryTag;
import net.kyori.adventure.nbt.LongBinaryTag;
import net.kyori.adventure.nbt.ShortBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class NbtIo {

    private NbtIo() {
    }

    public static void writeNbt(final ByteBuf buf, final BinaryTag tag) {
        try {
            writeNetwork(new DataOutputStream(new ByteBufOutputStream(buf)), tag);
        } catch (final IOException e) {
            throw new EncoderException("Failed writing NBT", e);
        }
    }

    public static BinaryTag readNbt(final ByteBuf buf, final long maxBytes) {
        try {
            return readNetwork(
                    new DataInputStream(new ByteBufInputStream(buf)),
                    NbtReadLimits.withBudget(maxBytes)
            );
        } catch (final IOException e) {
            throw new DecoderException("Failed reading NBT", e);
        }
    }

    private static void writeNetwork(final DataOutput out, final BinaryTag tag) throws IOException {
        out.writeByte(tag.type().id());
        writePayload(out, tag);
    }

    private static void writePayload(final DataOutput out, final BinaryTag tag) throws IOException {
        switch (tag) {
            case final ByteBinaryTag b -> out.writeByte(b.value());
            case final ShortBinaryTag s -> out.writeShort(s.value());
            case final IntBinaryTag i -> out.writeInt(i.value());
            case final LongBinaryTag l -> out.writeLong(l.value());
            case final FloatBinaryTag f -> out.writeFloat(f.value());
            case final DoubleBinaryTag d -> out.writeDouble(d.value());
            case final ByteArrayBinaryTag a -> {
                final byte[] value = a.value();
                out.writeInt(value.length);
                out.write(value);
            }
            case final StringBinaryTag s -> out.writeUTF(s.value());
            case final IntArrayBinaryTag a -> {
                final int[] value = a.value();
                out.writeInt(value.length);
                for (final int v : value) out.writeInt(v);
            }
            case final LongArrayBinaryTag a -> {
                final long[] value = a.value();
                out.writeInt(value.length);
                for (final long v : value) out.writeLong(v);
            }
            case final ListBinaryTag list -> writeList(out, list);
            case final CompoundBinaryTag compound -> {
                for (final Map.Entry<String, ? extends BinaryTag> entry : compound) {
                    final BinaryTag value = entry.getValue();
                    out.writeByte(value.type().id());
                    out.writeUTF(entry.getKey());
                    writePayload(out, value);
                }
                out.writeByte(BinaryTagTypes.END.id());
            }
            case final EndBinaryTag ignored -> throw new IOException("Cannot write TAG_End as a payload");
            default -> throw new IOException("Unsupported BinaryTag implementation: " + tag.getClass());
        }
    }

    private static void writeList(final DataOutput out, final ListBinaryTag list) throws IOException {
        final int elementType = identifyRaw(list);
        out.writeByte(elementType);
        out.writeInt(list.size());
        for (final BinaryTag item : list) {
            writePayload(out, wrapOnNeed(elementType, item));
        }
    }

    private static int identifyRaw(final ListBinaryTag list) {
        int homogenousType = 0;
        for (final BinaryTag item : list) {
            final int id = item.type().id();
            if (homogenousType == 0) {
                homogenousType = id;
            } else if (homogenousType != id) {
                return BinaryTagTypes.COMPOUND.id();
            }
        }
        return homogenousType;
    }

    private static BinaryTag wrapOnNeed(final int elementType, final BinaryTag tag) {
        if (elementType != BinaryTagTypes.COMPOUND.id()) {
            return tag;
        }
        if (tag instanceof final CompoundBinaryTag compound && !isWrapper(compound)) {
            return compound;
        }
        return wrapPart(tag);
    }

    private static boolean isWrapper(final CompoundBinaryTag tag) {
        return tag.keySet().size() == 1 && tag.keySet().contains("");
    }

    private static CompoundBinaryTag wrapPart(final BinaryTag tag) {
        return CompoundBinaryTag.from(Map.of("", tag));
    }

    private static BinaryTag readNetwork(final DataInput in, final NbtReadLimits limits) throws IOException {
        final int id = in.readUnsignedByte();
        return readPayload(in, id, limits, 0);
    }

    @FunctionalInterface
    private interface TagReader {
        BinaryTag read(DataInput in, NbtReadLimits limits, int depth) throws IOException;
    }

    private static final Map<BinaryTagType<?>, TagReader> READERS = Map.ofEntries(
            Map.entry(BinaryTagTypes.BYTE, (in, _, _) -> ByteBinaryTag.byteBinaryTag(in.readByte())),
            Map.entry(BinaryTagTypes.SHORT, (in, _, _) -> ShortBinaryTag.shortBinaryTag(in.readShort())),
            Map.entry(BinaryTagTypes.INT, (in, _, _) -> IntBinaryTag.intBinaryTag(in.readInt())),
            Map.entry(BinaryTagTypes.LONG, (in, _, _) -> LongBinaryTag.longBinaryTag(in.readLong())),
            Map.entry(BinaryTagTypes.FLOAT, (in, _, _) -> FloatBinaryTag.floatBinaryTag(in.readFloat())),
            Map.entry(BinaryTagTypes.DOUBLE, (in, _, _) -> DoubleBinaryTag.doubleBinaryTag(in.readDouble())),
            Map.entry(BinaryTagTypes.BYTE_ARRAY, NbtIo::readByteArray),
            Map.entry(BinaryTagTypes.STRING, NbtIo::readString),
            Map.entry(BinaryTagTypes.LIST, NbtIo::readList),
            Map.entry(BinaryTagTypes.COMPOUND, NbtIo::readCompound),
            Map.entry(BinaryTagTypes.INT_ARRAY, NbtIo::readIntArray),
            Map.entry(BinaryTagTypes.LONG_ARRAY, NbtIo::readLongArray)
    );

    private static BinaryTag readPayload(final DataInput in, final int id, final NbtReadLimits limits, final int depth) throws IOException {
        final BinaryTagType<?> type = BinaryTagType.binaryTagType((byte) id);
        if (type == BinaryTagTypes.END) {
            throw new IOException("TAG_End inattendu");
        }
        limits.spendFor(type);

        final TagReader reader = READERS.get(type);
        if (reader == null) {
            throw new DecoderException("Unsupported NBT tag type: " + type);
        }
        return reader.read(in, limits, depth);
    }

    private static BinaryTag readByteArray(final DataInput in, final NbtReadLimits limits, final int depth) throws IOException {
        final int len = in.readInt();
        if (len < 0) throw new DecoderException("Negative TAG_Byte_Array length: " + len);
        limits.spend(len);
        final byte[] arr = new byte[len];
        in.readFully(arr);
        return ByteArrayBinaryTag.byteArrayBinaryTag(arr);
    }

    private static BinaryTag readString(final DataInput in, final NbtReadLimits limits, final int depth) throws IOException {
        final String s = in.readUTF();
        limits.spendStringValue(s.length());
        return StringBinaryTag.stringBinaryTag(s);
    }

    private static BinaryTag readList(final DataInput in, final NbtReadLimits limits, final int depth) throws IOException {
        limits.checkDepth(depth + 1);
        final int elementId = in.readUnsignedByte();
        final int len = in.readInt();
        if (len < 0) throw new DecoderException("Negative TAG_List length: " + len);
        limits.spend((long) len * 4L);
        final boolean maybeWrapped = elementId == BinaryTagTypes.COMPOUND.id();
        final ListBinaryTag.Builder<BinaryTag> builder = ListBinaryTag.builder();
        for (int i = 0; i < len; i++) {
            final BinaryTag value = readPayload(in, elementId, limits, depth + 1);
            builder.add(maybeWrapped ? unwrapIfNeeded(value) : value);
        }
        return builder.build();
    }

    private static BinaryTag unwrapIfNeeded(final BinaryTag tag) {
        if (tag instanceof final CompoundBinaryTag compound && compound.keySet().size() == 1 && compound.keySet().contains("")) {
            final BinaryTag unwrapped = compound.get("");
            return unwrapped != null ? unwrapped : tag;
        }
        return tag;
    }

    private static BinaryTag readCompound(final DataInput in, final NbtReadLimits limits, final int depth) throws IOException {
        limits.checkDepth(depth + 1);
        final CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
        final Set<String> seenKeys = new HashSet<>();
        while (true) {
            final int childId = in.readUnsignedByte();
            if (childId == BinaryTagTypes.END.id()) break;
            final String key = in.readUTF();
            limits.spendCompoundKey(key.length());
            final BinaryTag value = readPayload(in, childId, limits, depth + 1);
            final boolean isNewKey = seenKeys.add(key);
            builder.put(key, value);
            if (isNewKey) {
                limits.spendNewCompoundEntry();
            }
        }
        return builder.build();
    }

    private static BinaryTag readIntArray(final DataInput in, final NbtReadLimits limits, final int depth) throws IOException {
        final int len = in.readInt();
        if (len < 0) throw new DecoderException("Negative TAG_Int_Array length: " + len);
        limits.spend((long) len * 4L);
        final int[] arr = new int[len];
        for (int i = 0; i < len; i++) arr[i] = in.readInt();
        return IntArrayBinaryTag.intArrayBinaryTag(arr);
    }

    private static BinaryTag readLongArray(final DataInput in, final NbtReadLimits limits, final int depth) throws IOException {
        final int len = in.readInt();
        if (len < 0) throw new DecoderException("Negative TAG_Long_Array length: " + len);
        limits.spend((long) len * 8L);
        final long[] arr = new long[len];
        for (int i = 0; i < len; i++) arr[i] = in.readLong();
        return LongArrayBinaryTag.longArrayBinaryTag(arr);
    }
}
