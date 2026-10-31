package fr.euphyllia.fidorial.server.world.storage.datafixers.util.nbt;

import ca.spottedleaf.converter.types.ObjectType;
import ca.spottedleaf.converter.types.TypeUtil;
import net.kyori.adventure.nbt.BinaryTag;
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
import org.jspecify.annotations.Nullable;

public final class NbtTypeUtil extends TypeUtil<BinaryTag> {

    public static final NbtTypeUtil INSTANCE = new NbtTypeUtil();

    @Override
    public NbtListType createEmptyList() {
        return new NbtListType();
    }

    @Override
    public NbtMapType createEmptyMap() {
        return new NbtMapType();
    }

    @Override
    @Nullable public Object convertFromBaseToGeneric(final @Nullable BinaryTag input, final TypeUtil<?> to) {
        return baseToGeneric(input);
    }

    @Override
    public Object convertGenericToGeneric(final Object valueGeneric, final TypeUtil<?> to) {
        throw new UnsupportedOperationException("Cross TypeUtil conversion not implemented");
    }

    @Override
    @Nullable public Object baseToGeneric(final @Nullable BinaryTag input) {
        if (input == null || input == EndBinaryTag.endBinaryTag()) {
            return null;
        }
        return switch (input) {
            case final CompoundBinaryTag c -> NbtMapType.of(c);
            case final ListBinaryTag l -> NbtListType.of(l);
            case final ByteBinaryTag b -> b.value();
            case final ShortBinaryTag s -> s.value();
            case final IntBinaryTag i -> i.value();
            case final LongBinaryTag l -> l.value();
            case final FloatBinaryTag f -> f.value();
            case final DoubleBinaryTag d -> d.value();
            case final StringBinaryTag s -> s.value();
            case final ByteArrayBinaryTag b -> b.value();
            case final IntArrayBinaryTag i -> i.value();
            case final LongArrayBinaryTag l -> l.value();
            default -> throw new IllegalStateException("Unhandled tag type: " + input.type());
        };
    }

    @Override
    public BinaryTag genericToBase(final @Nullable Object input) {
        if (input == null) {
            return EndBinaryTag.endBinaryTag();
        }
        return switch (input) {
            case final NbtMapType m -> m.toCompound();
            case final NbtListType l -> l.toListTag();
            case final Byte b -> ByteBinaryTag.byteBinaryTag(b);
            case final Short s -> ShortBinaryTag.shortBinaryTag(s);
            case final Integer i -> IntBinaryTag.intBinaryTag(i);
            case final Long l -> LongBinaryTag.longBinaryTag(l);
            case final Float f -> FloatBinaryTag.floatBinaryTag(f);
            case final Double d -> DoubleBinaryTag.doubleBinaryTag(d);
            case final String s -> StringBinaryTag.stringBinaryTag(s);
            case final byte[] b -> ByteArrayBinaryTag.byteArrayBinaryTag(b);
            case final int[] i -> IntArrayBinaryTag.intArrayBinaryTag(i);
            case final long[] l -> LongArrayBinaryTag.longArrayBinaryTag(l);
            default -> throw new IllegalStateException("Cannot convert to BinaryTag: " + input.getClass());
        };
    }

    @Override
    public boolean isCompatibleNumber(final Number number) {
        return number instanceof Byte || number instanceof Short || number instanceof Integer
                || number instanceof Long || number instanceof Float || number instanceof Double;
    }

    @Override
    public boolean isCompatibleArray(final Object array) {
        return array instanceof byte[] || array instanceof int[] || array instanceof long[];
    }

    @Override
    @Nullable public ObjectType getTypeBase(final BinaryTag value) {
        return switch (value) {
            case ByteBinaryTag _ -> ObjectType.BYTE;
            case ShortBinaryTag _ -> ObjectType.SHORT;
            case IntBinaryTag _ -> ObjectType.INT;
            case LongBinaryTag _ -> ObjectType.LONG;
            case FloatBinaryTag _ -> ObjectType.FLOAT;
            case DoubleBinaryTag _ -> ObjectType.DOUBLE;
            case StringBinaryTag _ -> ObjectType.STRING;
            case ByteArrayBinaryTag _ -> ObjectType.BYTE_ARRAY;
            case IntArrayBinaryTag _ -> ObjectType.INT_ARRAY;
            case LongArrayBinaryTag _ -> ObjectType.LONG_ARRAY;
            case CompoundBinaryTag _ -> ObjectType.MAP;
            case ListBinaryTag _ -> ObjectType.LIST;
            default -> null;
        };
    }

    @Override
    public Object deepCopy(final BinaryTag base) {
        return base;
    }
}
