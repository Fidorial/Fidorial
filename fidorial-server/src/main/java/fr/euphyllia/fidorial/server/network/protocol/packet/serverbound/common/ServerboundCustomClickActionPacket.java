package fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.common;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.StringLikeCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkDecoder;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.ConfigurationPacketListener;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.PlayPacketListener;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
import fr.fidorial.protocol.PacketListener;
import fr.fidorial.protocol.ServerboundPacket;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#Custom_Click_Action
public record ServerboundCustomClickActionPacket(Key id, BinaryTag payload) implements ServerboundPacket {

    private static final int MAX_PAYLOAD_BYTES = 65536;

    private static final NetworkDecoder<PacketBuffer, BinaryTag> PAYLOAD = buf -> buf.readSizedNbt(MAX_PAYLOAD_BYTES);

    public static final NetworkDecoder<PacketBuffer, ServerboundCustomClickActionPacket> DECODER = NetworkDecoder.composite(
            StringLikeCodecs.KEY,
            PAYLOAD,
            ServerboundCustomClickActionPacket::new);

    @Override
    public void handle(final PacketListener listener) {
        if (listener instanceof final PlayPacketListener play) {
            play.handleCustomClickAction(this);
        } else if (listener instanceof final ConfigurationPacketListener config) {
            config.handleCustomClickAction(this);
        }
    }
}
