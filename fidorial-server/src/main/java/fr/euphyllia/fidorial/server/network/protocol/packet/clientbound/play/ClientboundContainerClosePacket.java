package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;

/**
 * Forcibly closes a window on the client (chest broken while open, player teleported, plugin taking
 * over, ...).
 *
 * <p>Format:
 *
 * <pre>
 *   Window ID   VarInt
 * </pre>
 *
 * <p>The vanilla client ignores the given ID and closes the active window.
 *
 * <p>https://minecraft.wiki/w/Java_Edition_protocol/Packets#Close_Container
 */
public record ClientboundContainerClosePacket(int windowId) implements ClientboundPacket<ClientboundContainerClosePacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundContainerClosePacket> CODEC =
            NetworkEncoder.field(PrimitiveCodecs.VAR_INT, ClientboundContainerClosePacket::windowId);

    @Override
    public Key name() {
        return PlayClientboundPackets.CONTAINER_CLOSE;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundContainerClosePacket> codec() {
        return CODEC;
    }
}
