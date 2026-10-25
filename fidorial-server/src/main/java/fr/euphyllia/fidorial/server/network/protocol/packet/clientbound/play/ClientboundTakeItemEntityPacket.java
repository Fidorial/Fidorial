package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import net.kyori.adventure.key.Key;

/**
 * Shows an entity picking up an item: the client flies the item into the collector and plays the
 * pickup sound. It does not remove the item, a {@link ClientboundRemoveEntitiesPacket} does.
 *
 * <p><a href="https://minecraft.wiki/w/Java_Edition_protocol/Packets#Pickup_Item">Pickup Item</a></p>
 *
 * @param itemEntityId the item being picked up
 * @param collectorId  the entity picking it up
 * @param count        how many items are picked up
 */
public record ClientboundTakeItemEntityPacket(int itemEntityId, int collectorId, int count) implements ClientboundPacket {

    @Override
    public Key name() {
        return PlayClientboundPackets.TAKE_ITEM_ENTITY;
    }

    @Override
    public void write(final PacketBuffer buf) {
        buf.writeVarInt(itemEntityId);
        buf.writeVarInt(collectorId);
        buf.writeVarInt(count);
    }
}
