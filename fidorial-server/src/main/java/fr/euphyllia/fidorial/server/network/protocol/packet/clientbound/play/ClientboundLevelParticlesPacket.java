package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.codecs.networking.RegistryNetworkCodecs;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.ParticleType;
import net.kyori.adventure.key.Key;

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#Particle
/**
 * Shows particles of a type that carries no options, such as splashes or bubbles, around a point.
 *
 * <p>With a count of {@code 0}, a single particle shows at the point, moving by the spread on each
 * axis times the speed. Otherwise, each particle shows at a random distance from the point along
 * each axis, up to the spread, moving randomly at most at the speed.</p>
 *
 * @param particle the type of the particles, one without options
 * @param x        where the particles show along the x axis
 * @param y        where the particles show along the y axis
 * @param z        where the particles show along the z axis
 * @param spreadX  how far from the point they show along the x axis
 * @param spreadY  how far from the point they show along the y axis
 * @param spreadZ  how far from the point they show along the z axis
 * @param speed    how fast they move at most
 * @param count    how many particles show
 */
public record ClientboundLevelParticlesPacket(TypedKey<ParticleType> particle, double x, double y, double z,
                                              float spreadX, float spreadY, float spreadZ, float speed, int count)
        implements ClientboundPacket<ClientboundLevelParticlesPacket> {

    /**
     * How the particles are spread: at random around the point.
     */
    private static final int RANDOM_AROUND = 0;

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundLevelParticlesPacket> CODEC = NetworkEncoder
            .field(RegistryNetworkCodecs.id(RegistryKey.PARTICLE_TYPE), ClientboundLevelParticlesPacket::particle)
            // shown as far as the client shows particles, and not to the players who hide them
            .thenConstant(PrimitiveCodecs.BOOL, false)
            .thenConstant(PrimitiveCodecs.BOOL, false)
            .then(PrimitiveCodecs.DOUBLE, ClientboundLevelParticlesPacket::x)
            .then(PrimitiveCodecs.DOUBLE, ClientboundLevelParticlesPacket::y)
            .then(PrimitiveCodecs.DOUBLE, ClientboundLevelParticlesPacket::z)
            .then(PrimitiveCodecs.FLOAT, ClientboundLevelParticlesPacket::spreadX)
            .then(PrimitiveCodecs.FLOAT, ClientboundLevelParticlesPacket::spreadY)
            .then(PrimitiveCodecs.FLOAT, ClientboundLevelParticlesPacket::spreadZ)
            .then(PrimitiveCodecs.FLOAT, ClientboundLevelParticlesPacket::speed)
            .then(PrimitiveCodecs.FLOAT, ClientboundLevelParticlesPacket::speed)
            .then(PrimitiveCodecs.FLOAT, ClientboundLevelParticlesPacket::speed)
            .then(PrimitiveCodecs.VAR_INT, ClientboundLevelParticlesPacket::count)
            .thenConstant(PrimitiveCodecs.VAR_INT, RANDOM_AROUND);

    @Override
    public Key name() {
        return PlayClientboundPackets.LEVEL_PARTICLES;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundLevelParticlesPacket> codec() {
        return CODEC;
    }
}
