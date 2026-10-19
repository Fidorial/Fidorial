package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import net.kyori.adventure.key.Key;

import java.util.List;

/**
 * Sets attribute values of an entity on the client.
 *
 * <p><a href="https://minecraft.wiki/w/Java_Edition_protocol/Packets#Update_Attributes">Update Attributes</a></p>
 *
 * @param entityId the entity
 * @param values   the attributes, each with its base value; no modifier is sent, so the client drops
 *                 the ones it had on these attributes
 */
public record ClientboundUpdateAttributesPacket(int entityId, List<Value> values) implements ClientboundPacket {

    /**
     * One attribute.
     *
     * @param attributeId the network id of the attribute in the {@code minecraft:attribute} registry
     * @param base        the base value
     */
    public record Value(int attributeId, double base) {
    }

    public ClientboundUpdateAttributesPacket {
        values = List.copyOf(values);
    }

    @Override
    public Key name() {
        return PlayClientboundPackets.UPDATE_ATTRIBUTES;
    }

    @Override
    public void write(final PacketBuffer buf) {
        buf.writeVarInt(entityId);
        buf.writeVarInt(values.size());
        for (final Value value : values) {
            buf.writeVarInt(value.attributeId());
            buf.writeDouble(value.base());
            buf.writeVarInt(0);
        }
    }
}
