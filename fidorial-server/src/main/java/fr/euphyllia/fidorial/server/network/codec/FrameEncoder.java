package fr.euphyllia.fidorial.server.network.codec;

import fr.euphyllia.fidorial.server.network.VarInts;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public final class FrameEncoder extends MessageToByteEncoder<ByteBuf> {

    @Override
    protected void encode(final ChannelHandlerContext ctx, final ByteBuf msg, final ByteBuf out) {
        final int length = msg.readableBytes();
        out.ensureWritable(VarInts.varIntSize(length) + length);
        VarInts.writeVarInt(out, length);
        out.writeBytes(msg);
    }
}
