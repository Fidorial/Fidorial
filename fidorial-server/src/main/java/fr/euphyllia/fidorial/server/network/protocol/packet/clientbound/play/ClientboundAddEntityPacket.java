package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.entity.AbstractEntity;
import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.util.LocationPositionData;
import fr.euphyllia.fidorial.server.network.protocol.packet.util.PositionData;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import fr.fidorial.entity.LivingEntity;
import fr.fidorial.math.Location;
import net.kyori.adventure.key.Key;

import java.util.UUID;

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#Spawn_Entity
public record ClientboundAddEntityPacket(
        int entityId,
        UUID uuid,
        int typeNetworkId,
        PositionData.Vec3D position,
        PositionData.VelocityVec3D velocity,
        float pitch,
        float yaw,
        float headYaw,
        int data)
        implements ClientboundPacket<ClientboundAddEntityPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundAddEntityPacket> CODEC = NetworkEncoder
            .field(PrimitiveCodecs.VAR_INT, ClientboundAddEntityPacket::entityId)
            .then(PrimitiveCodecs.UUID, ClientboundAddEntityPacket::uuid)
            .then(PrimitiveCodecs.VAR_INT, ClientboundAddEntityPacket::typeNetworkId)
            .then(PositionData.Vec3D.CODEC, ClientboundAddEntityPacket::position)
            .then(PositionData.VelocityVec3D.CODEC, ClientboundAddEntityPacket::velocity)
            .then(PrimitiveCodecs.ANGLE, ClientboundAddEntityPacket::pitch)
            .then(PrimitiveCodecs.ANGLE, ClientboundAddEntityPacket::yaw)
            .then(PrimitiveCodecs.ANGLE, ClientboundAddEntityPacket::headYaw)
            .then(PrimitiveCodecs.VAR_INT, ClientboundAddEntityPacket::data);

    public static ClientboundAddEntityPacket of(final AbstractEntity entity) {
        final Location location = entity.location();
        return new ClientboundAddEntityPacket(
                entity.entityId(),
                entity.uuid(),
                EntityTypes.networkId(entity.type()),
                LocationPositionData.vec3(location),
                new PositionData.VelocityVec3D(0.0, 0.0, 0.0),
                location.pitch(),
                location.yaw(),
                entity instanceof final LivingEntity living ? living.headYaw() : 0.0f,
                0);
    }

    @Override
    public Key name() {
        return PlayClientboundPackets.ADD_ENTITY;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundAddEntityPacket> codec() {
        return CODEC;
    }
}
