package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.fidorial.attribute.AttributeModifier;
import net.kyori.adventure.key.Key;

import java.util.Collection;
import java.util.List;

/**
 * Sets attribute values of an entity on the client.
 *
 * <p><a href="https://minecraft.wiki/w/Java_Edition_protocol/Packets#Update_Attributes">Update Attributes</a></p>
 *
 * @param entityId the entity
 * @param values   the attributes, each with its base value and the modifiers it carries; the client
 *                 drops the modifiers it had on these attributes and keeps the ones sent
 */
public record ClientboundUpdateAttributesPacket(int entityId, List<Value> values) implements ClientboundPacket {

    /**
     * One attribute.
     *
     * @param attributeId the network id of the attribute in the {@code minecraft:attribute} registry
     * @param base        the base value
     * @param modifiers   the modifiers applied on top of the base value
     */
    public record Value(int attributeId, double base, List<AttributeModifier> modifiers) {

        public Value {
            modifiers = List.copyOf(modifiers);
        }

        /**
         * @param attributeId the network id of the attribute
         * @param base        the base value
         * @param modifiers   the modifiers
         * @return the value
         */
        public static Value of(final int attributeId, final double base, final Collection<AttributeModifier> modifiers) {
            return new Value(attributeId, base, List.copyOf(modifiers));
        }
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
            buf.writeVarInt(value.modifiers().size());
            for (final AttributeModifier modifier : value.modifiers()) {
                buf.writeKey(modifier.id());
                buf.writeDouble(modifier.amount());
                buf.writeVarInt(modifier.operation().networkId());
            }
        }
    }
}
