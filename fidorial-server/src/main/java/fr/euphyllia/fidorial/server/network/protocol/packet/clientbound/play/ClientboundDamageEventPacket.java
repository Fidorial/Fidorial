package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.codecs.networking.RegistryNetworkCodecs;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
import fr.fidorial.math.Location;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.DamageType;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

/**
 * Tells the client an entity took damage, and of which kind.
 *
 * <p>The client uses this to pick the hit sound, the screen tint and the death message, so it is
 * sent alongside the hurt animation rather than instead of it.</p>
 *
 * <p><a href="https://minecraft.wiki/w/Java_Edition_protocol/Packets#Damage_Event">Damage Event</a></p>
 *
 * @param entityId       the entity taking the damage
 * @param damageType     the entry of {@code minecraft:damage_type}, sent as its network id
 * @param causingEntity  the entity credited with the damage, or {@code null}
 * @param directEntity   the entity that landed the hit, or {@code null}
 * @param sourcePosition the point damage radiated from, or {@code null} to derive it from the
 *                       entities
 */
public record ClientboundDamageEventPacket(
        int entityId,
        TypedKey<DamageType> damageType,
        @Nullable Integer causingEntity,
        @Nullable Integer directEntity,
        @Nullable Location sourcePosition) implements ClientboundPacket<ClientboundDamageEventPacket> {

    /**
     * An entity id plus one, or zero for none.
     */
    private static final NetworkEncoder<PacketBuffer, @Nullable Integer> OPTIONAL_ENTITY_ID =
            PrimitiveCodecs.VAR_INT.comap(id -> id == null ? 0 : id + 1);

    private static final NetworkEncoder<PacketBuffer, Location> SOURCE_POSITION = NetworkEncoder
            .field(PrimitiveCodecs.DOUBLE, Location::x)
            .then(PrimitiveCodecs.DOUBLE, Location::y)
            .then(PrimitiveCodecs.DOUBLE, Location::z);

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundDamageEventPacket> CODEC = NetworkEncoder
            .field(PrimitiveCodecs.VAR_INT, ClientboundDamageEventPacket::entityId)
            .then(RegistryNetworkCodecs.id(RegistryKey.DAMAGE_TYPE), ClientboundDamageEventPacket::damageType)
            .then(OPTIONAL_ENTITY_ID, ClientboundDamageEventPacket::causingEntity)
            .then(OPTIONAL_ENTITY_ID, ClientboundDamageEventPacket::directEntity)
            .then(PrimitiveCodecs.prefixedNullable(SOURCE_POSITION), ClientboundDamageEventPacket::sourcePosition);

    @Override
    public Key name() {
        return PlayClientboundPackets.DAMAGE_EVENT;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundDamageEventPacket> codec() {
        return CODEC;
    }
}
