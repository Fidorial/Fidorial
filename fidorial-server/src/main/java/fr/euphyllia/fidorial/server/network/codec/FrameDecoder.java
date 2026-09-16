package fr.euphyllia.fidorial.server.network.codec;

import fr.euphyllia.fidorial.server.network.protocol.ProtocolConstants;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;

import java.util.List;

public final class FrameDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) {
        in.markReaderIndex();

        int length = 0;
        for (int i = 0; i < 3; i++) {
            if (!in.isReadable()) {
                in.resetReaderIndex();
                return;
            }
            final byte b = in.readByte();
            length |= (b & 0x7F) << (i * 7);
            if ((b & 0x80) == 0) {
                if (length < 0 || length > ProtocolConstants.MAX_PACKET_SIZE) {
                    throw new CorruptedFrameException("Frame de " + length + " octets");
                }
                if (in.readableBytes() < length) {
                    in.resetReaderIndex();
                    return;
                }
                out.add(in.readBytes(length));
                return;
            }
        }
        throw new CorruptedFrameException("VarInt de longueur > 3 octets");
    }
}
