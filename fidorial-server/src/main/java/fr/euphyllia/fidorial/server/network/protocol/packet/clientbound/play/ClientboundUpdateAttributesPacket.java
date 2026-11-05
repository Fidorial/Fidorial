package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.StringLikeCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
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
public record ClientboundUpdateAttributesPacket(int entityId, List<Value> values) implements ClientboundPacket<ClientboundUpdateAttributesPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundUpdateAttributesPacket> CODEC = NetworkEncoder
            .field(PrimitiveCodecs.VAR_INT, ClientboundUpdateAttributesPacket::entityId)
            .then(Value.CODEC.listOf(PacketBuffer::writeVarInt), ClientboundUpdateAttributesPacket::values);

    /**
     * One attribute.
     *
     * @param attributeId the network id of the attribute in the {@code minecraft:attribute} registry
     * @param base        the base value
     * @param modifiers   the modifiers applied on top of the base value
     */
    public record Value(int attributeId, double base, List<AttributeModifier> modifiers) {

        private static final NetworkEncoder<PacketBuffer, AttributeModifier> MODIFIER = NetworkEncoder
                .field(StringLikeCodecs.KEY, AttributeModifier::id)
                .then(PrimitiveCodecs.DOUBLE, AttributeModifier::amount)
                .then(PrimitiveCodecs.VAR_INT, modifier -> modifier.operation().networkId());

        public static final NetworkEncoder<PacketBuffer, Value> CODEC = NetworkEncoder
                .field(PrimitiveCodecs.VAR_INT, Value::attributeId)
                .then(PrimitiveCodecs.DOUBLE, Value::base)
                .then(MODIFIER.listOf(PacketBuffer::writeVarInt), Value::modifiers);

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
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundUpdateAttributesPacket> codec() {
        return CODEC;
    }
}
