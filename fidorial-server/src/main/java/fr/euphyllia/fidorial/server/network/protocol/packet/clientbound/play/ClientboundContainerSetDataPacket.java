package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;

/**
 * Updates one property of a window: the disabled slots of a crafter, and later the progress
 * arrow of a furnace or the fuel of a brewing stand.
 *
 * <p>Format:
 *
 * <pre>
 *   Window ID   VarInt
 *   Property    Short
 *   Value       Short
 * </pre>
 *
 * <p><a href="https://minecraft.wiki/w/Java_Edition_protocol/Packets#Set_Container_Property">Packets#Set_Container_Property</a>
 */
public record ClientboundContainerSetDataPacket(int windowId, int property, int value) implements ClientboundPacket<ClientboundContainerSetDataPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundContainerSetDataPacket> CODEC = NetworkEncoder.member(ClientboundContainerSetDataPacket::encode);

    @Override
    public Key name() {
        return PlayClientboundPackets.CONTAINER_SET_DATA;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundContainerSetDataPacket> codec() {
        return CODEC;
    }

    private void encode(final CodecAwarePacketBuffer buf) {
        buf.writeVarInt(windowId);
        buf.writeShort(property);
        buf.writeShort(value);
    }
}
