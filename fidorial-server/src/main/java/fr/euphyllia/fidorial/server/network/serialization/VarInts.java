package fr.euphyllia.fidorial.server.network.serialization;

import fr.euphyllia.fidorial.server.codecs.adventure.ComponentCodecs;
import fr.euphyllia.fidorial.server.network.serialization.util.nbt.NbtIo;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import io.papermc.adventurex.nbt.dfu.BinaryTagOps;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.text.Component;

import java.nio.charset.StandardCharsets;

public final class VarInts {

    private VarInts() {
    }

    public static int readVarInt(final ByteBuf buf) {
        int value = 0;
        int position = 0;
        byte current;
        do {
            if (position == 35) {
                throw new DecoderException("VarInt too large");
            }
            current = buf.readByte();
            value |= (current & 0x7F) << position;
            position += 7;
        } while ((current & 0x80) != 0);
        return value;
    }

    public static void writeVarInt(final ByteBuf buf, int value) {
        while ((value & ~0x7F) != 0) {
            buf.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        buf.writeByte(value);
    }

    public static long readVarLong(final ByteBuf buf) {
        long value = 0;
        int position = 0;
        byte current;
        do {
            if (position == 70) {
                throw new DecoderException("VarLong too large");
            }
            current = buf.readByte();
            value |= (long) (current & 0x7F) << position;
            position += 7;
        } while ((current & 0x80) != 0);
        return value;
    }

    public static void writeVarLong(final ByteBuf buf, long value) {
        while ((value & ~0x7FL) != 0L) {
            buf.writeByte((int) (value & 0x7FL) | 0x80);
            value >>>= 7;
        }
        buf.writeByte((int) value);
    }

    public static int varIntSize(int value) {
        int size = 1;
        while ((value & ~0x7F) != 0) {
            size++;
            value >>>= 7;
        }
        return size;
    }

    public static String readString(final ByteBuf buf, final int maxLength) {
        final int length = readVarInt(buf);
        if (length < 0 || length > maxLength * 4) {
            throw new DecoderException("Invalid string ( " + length + " bytes)");
        }
        final String s = buf.toString(buf.readerIndex(), length, StandardCharsets.UTF_8);
        buf.skipBytes(length);
        return s;
    }

    public static void writeString(final ByteBuf buf, final String value) {
        final byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        writeVarInt(buf, bytes.length);
        buf.writeBytes(bytes);
    }

    public static byte[] readByteArray(final ByteBuf buf, final int maxLength) {
        final int length = readVarInt(buf);
        if (length < 0 || length > maxLength) throw new DecoderException("invalid byte[]");
        final byte[] data = new byte[length];
        buf.readBytes(data);
        return data;
    }

    public static void writeByteArray(final ByteBuf buf, final byte[] data) {
        writeVarInt(buf, data.length);
        buf.writeBytes(data);
    }

    public static void writeComponent(final ByteBuf buf, final Component message) {
        final BinaryTag nbt = ComponentCodecs.COMPONENT_CODEC.encodeStart(BinaryTagOps.binaryTagOps(), message)
                 .getOrThrow(msg -> new EncoderException("Failed to encode Component: " + msg));
        NbtIo.writeNbt(buf, nbt);
    }

    public static Component readComponent(final ByteBuf buf, final int maxLength) {
        final int start = buf.readerIndex();
        final BinaryTag nbt = NbtIo.readNbt(buf, maxLength);
        final int consumed = buf.readerIndex() - start;
        if (consumed > maxLength) {
            throw new DecoderException("Component NBT exceeds maximum size: " + consumed + " > " + maxLength);
        }

        return ComponentCodecs.COMPONENT_CODEC.parse(BinaryTagOps.binaryTagOps(), nbt)
                .getOrThrow(msg -> new DecoderException("Failed to decode Component: " + msg));
    }
}
