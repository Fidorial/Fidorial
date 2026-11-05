package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.util.ItemStackWriter;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
import fr.fidorial.item.ItemStack;
import net.kyori.adventure.key.Key;

import java.util.List;

/**
 * Fully replaces the contents of a window.
 *
 * <p>Format:
 *
 * <pre>
 *   Window ID     VarInt
 *   State ID      VarInt
 *   Slots         Prefixed Array of Slot
 *   Carried item  Slot
 * </pre>
 *
 * <p>Also resent to resynchronize the client after each click, mirroring what the vanilla server
 * does when a State ID does not match.
 *
 * <p><a href="https://minecraft.wiki/w/Java_Edition_protocol/Packets#Set_Container_Content">Packets#Set_Container_Content</a>
 */
public record ClientboundContainerSetContentPacket(
        int windowId, int stateId, List<ItemStack> slots, ItemStack carried)
        implements ClientboundPacket<ClientboundContainerSetContentPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundContainerSetContentPacket> CODEC = NetworkEncoder
            .field(PrimitiveCodecs.VAR_INT, ClientboundContainerSetContentPacket::windowId)
            .then(PrimitiveCodecs.VAR_INT, ClientboundContainerSetContentPacket::stateId)
            .then(ItemStackWriter.SLOT.listOf(PacketBuffer::writeVarInt), ClientboundContainerSetContentPacket::slots)
            .then(ItemStackWriter.SLOT, ClientboundContainerSetContentPacket::carried); // cursor

    public ClientboundContainerSetContentPacket {
        slots = List.copyOf(slots);
    }

    @Override
    public Key name() {
        return PlayClientboundPackets.CONTAINER_SET_CONTENT;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundContainerSetContentPacket> codec() {
        return CODEC;
    }
}
