package fr.euphyllia.fidorial.server.network.serialization.util;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import javax.crypto.Cipher;

public final class CipherEncoder extends MessageToByteEncoder<ByteBuf> {

    private final Cipher cipher;

    public CipherEncoder(final Cipher cipher) {
        this.cipher = cipher;
    }

    @Override
    protected void encode(final ChannelHandlerContext ctx, final ByteBuf msg, final ByteBuf out) throws Exception {
        final byte[] input = new byte[msg.readableBytes()];
        msg.readBytes(input);
        out.writeBytes(cipher.update(input));
    }
}
