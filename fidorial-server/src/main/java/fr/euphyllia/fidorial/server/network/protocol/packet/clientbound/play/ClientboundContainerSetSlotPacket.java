package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.util.ItemStackWriter;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import fr.fidorial.item.ItemStack;
import net.kyori.adventure.key.Key;

/**
 * Updates a single slot of a window.
 *
 * <p>Format:
 *
 * <pre>
 *   Window ID   VarInt
 *   State ID    VarInt
 *   Slot        Short
 *   Slot Data   Slot
 * </pre>
 *
 * <p>https://minecraft.wiki/w/Java_Edition_protocol/Packets#Set_Container_Slot
 */
public record ClientboundContainerSetSlotPacket(
        int windowId, int stateId, int slot, ItemStack stack)
        implements ClientboundPacket<ClientboundContainerSetSlotPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundContainerSetSlotPacket> CODEC = NetworkEncoder
            .field(PrimitiveCodecs.VAR_INT, ClientboundContainerSetSlotPacket::windowId)
            .then(PrimitiveCodecs.VAR_INT, ClientboundContainerSetSlotPacket::stateId)
            .then(PrimitiveCodecs.SHORT.comap(Integer::shortValue), ClientboundContainerSetSlotPacket::slot)
            .then(ItemStackWriter.SLOT, ClientboundContainerSetSlotPacket::stack);

    @Override
    public Key name() {
        return PlayClientboundPackets.CONTAINER_SET_SLOT;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundContainerSetSlotPacket> codec() {
        return CODEC;
    }
}
