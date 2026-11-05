package fr.euphyllia.fidorial.server.network.serialization.util;

import fr.euphyllia.fidorial.server.network.protocol.ProtocolConstants;
import fr.euphyllia.fidorial.server.network.serialization.VarInts;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public final class CompressionDecoder extends MessageToMessageDecoder<ByteBuf> {

    private final int threshold;
    private final Inflater inflater = new Inflater();

    public CompressionDecoder(final int threshold) {
        this.threshold = threshold;
    }

    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) {
        if (!in.isReadable()) return;

        final int dataLength = VarInts.readVarInt(in);
        if (dataLength == 0) {

            out.add(in.readRetainedSlice(in.readableBytes()));
            return;
        }
        if (dataLength < threshold) {
            throw new DecoderException("Compress packet under the threshold (" + dataLength + ")");
        }
        if (dataLength > ProtocolConstants.MAX_PACKET_SIZE) {
            throw new DecoderException("Decompressed packet is too large (" + dataLength + ")");
        }

        final byte[] compressed = new byte[in.readableBytes()];
        in.readBytes(compressed);
        inflater.setInput(compressed);

        final byte[] decompressed = new byte[dataLength];
        try {
            final int produced = inflater.inflate(decompressed);
            if (produced != dataLength) {
                throw new DecoderException(
                        "Inconsistent inflated size : " + produced + " != " + dataLength);
            }
            out.add(ctx.alloc().buffer(dataLength).writeBytes(decompressed));
        } catch (final DataFormatException e) {
            throw new DecoderException("Invalid zlib stream", e);
        } finally {
            inflater.reset();
        }
    }

    @Override
    public void handlerRemoved(final ChannelHandlerContext ctx) {
        inflater.end();
    }
}
