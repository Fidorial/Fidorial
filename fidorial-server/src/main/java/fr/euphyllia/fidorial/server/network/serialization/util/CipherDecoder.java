package fr.euphyllia.fidorial.server.network.serialization.util;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import javax.crypto.Cipher;
import java.util.List;

public final class CipherDecoder extends MessageToMessageDecoder<ByteBuf> {

    private final Cipher cipher;

    public CipherDecoder(final Cipher cipher) {
        this.cipher = cipher;
    }

    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf msg, final List<Object> out) throws Exception {
        final byte[] input = new byte[msg.readableBytes()];
        msg.readBytes(input);
        out.add(ctx.alloc().buffer().writeBytes(cipher.update(input)));
    }
}
