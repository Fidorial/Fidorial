package fr.euphyllia.fidorial.server.world.structure;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.ByteBinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.DoubleBinaryTag;
import net.kyori.adventure.nbt.IntBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.nbt.LongBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import net.kyori.adventure.nbt.TagStringIO;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

public final class JsonNbt {

    private JsonNbt() {
    }

    public static CompoundBinaryTag compound(final JsonElement element) {
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            try {
                return TagStringIO.tagStringIO().asCompound(element.getAsString());
            } catch (final IOException invalid) {
                return CompoundBinaryTag.empty();
            }
        }
        return convert(element) instanceof final CompoundBinaryTag compound ? compound : CompoundBinaryTag.empty();
    }

    private static BinaryTag convert(final JsonElement element) {
        if (element instanceof final JsonObject object) {
            final CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
            for (final Map.Entry<String, JsonElement> entry : object.entrySet()) {
                builder.put(entry.getKey(), convert(entry.getValue()));
            }
            return builder.build();
        }
        if (element instanceof final JsonArray array) {
            final ListBinaryTag.Builder<BinaryTag> builder = ListBinaryTag.heterogeneousListBinaryTag();
            for (final JsonElement child : array) {
                builder.add(convert(child));
            }
            return builder.build();
        }
        if (element instanceof final JsonPrimitive primitive) {
            if (primitive.isBoolean()) {
                return ByteBinaryTag.byteBinaryTag((byte) (primitive.getAsBoolean() ? 1 : 0));
            }
            if (primitive.isNumber()) {
                final BigDecimal number = primitive.getAsBigDecimal();
                if (number.scale() <= 0 || number.stripTrailingZeros().scale() <= 0) {
                    final long value = number.longValue();
                    return value == (int) value ? IntBinaryTag.intBinaryTag((int) value) : LongBinaryTag.longBinaryTag(value);
                }
                return DoubleBinaryTag.doubleBinaryTag(number.doubleValue());
            }
            return StringBinaryTag.stringBinaryTag(primitive.getAsString());
        }
        return CompoundBinaryTag.empty();
    }
}
