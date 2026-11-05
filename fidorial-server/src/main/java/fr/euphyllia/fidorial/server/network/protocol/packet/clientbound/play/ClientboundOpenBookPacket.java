package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;

/**
 * Opens the written book held in a hand.
 *
 * <p><a href="https://minecraft.wiki/w/Java_Edition_protocol/Packets#Open_Book">Open Book</a></p>
 *
 * @param offHand {@code true} for the book in the off hand, {@code false} for the main hand
 */
public record ClientboundOpenBookPacket(boolean offHand) implements ClientboundPacket<ClientboundOpenBookPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundOpenBookPacket> CODEC =
            NetworkEncoder.field(PrimitiveCodecs.VAR_INT, packet -> packet.offHand() ? 1 : 0);

    @Override
    public Key name() {
        return PlayClientboundPackets.OPEN_BOOK;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundOpenBookPacket> codec() {
        return CODEC;
    }
}
