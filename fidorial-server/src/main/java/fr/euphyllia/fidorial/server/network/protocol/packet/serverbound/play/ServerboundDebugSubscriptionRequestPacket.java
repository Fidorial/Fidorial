package fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play;

import fr.euphyllia.fidorial.server.debug.DebugChannel;
import fr.euphyllia.fidorial.server.debug.DebugChannels;
import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.PlayPacketListener;
import fr.fidorial.protocol.PacketListener;
import fr.fidorial.protocol.ServerboundPacket;
import io.netty.handler.codec.DecoderException;

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#Debug_Subscription_Request
public record ServerboundDebugSubscriptionRequestPacket(int requested) implements ServerboundPacket {

    private static final int MAX_SUBSCRIPTIONS = 32;

    public static ServerboundDebugSubscriptionRequestPacket read(final PacketBuffer buf) {
        final int count = buf.readVarInt();
        if (count < 0 || count > MAX_SUBSCRIPTIONS) {
            throw new DecoderException("Too many debug subscriptions: " + count);
        }
        int requested = 0;
        for (int i = 0; i < count; i++) {
            final DebugChannel<?> channel = DebugChannels.byNetworkId(buf.readVarInt());
            if (channel != null) {
                requested |= channel.bit();
            }
        }
        return new ServerboundDebugSubscriptionRequestPacket(requested);
    }

    @Override
    public void handle(final PacketListener listener) {
        if (listener instanceof final PlayPacketListener play) {
            play.handleDebugSubscriptionRequest(this);
        }
    }
}
