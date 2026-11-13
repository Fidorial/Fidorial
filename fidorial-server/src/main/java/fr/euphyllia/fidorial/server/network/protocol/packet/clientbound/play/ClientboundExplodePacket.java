package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.codecs.networking.RegistryNetworkCodecs;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.util.PositionData;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
import fr.euphyllia.fidorial.server.util.annotations.NeedsToBeRevisited;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.ParticleType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.jspecify.annotations.Nullable;

import java.util.List;

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#Explosion
@NeedsToBeRevisited("Doesn't support particles with data; we need a proper particle API first")
public record ClientboundExplodePacket(
        PositionData.Vec3D location,
        float radius,
        int blockCount,
        // TODO: particle data, see BlockParticleAlternative
        @Nullable TypedKey<ParticleType> explosionParticle,
        Sound.@Nullable Type explosionSound,
        List<BlockParticleAlternative> blockParticleAlternatives)
        implements ClientboundPacket<ClientboundExplodePacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundExplodePacket> CODEC = NetworkEncoder
            .field(PositionData.Vec3D.CODEC, ClientboundExplodePacket::location)
            .then(PrimitiveCodecs.FLOAT, ClientboundExplodePacket::radius)
            .then(PrimitiveCodecs.INT, ClientboundExplodePacket::blockCount)
            .then(PrimitiveCodecs.prefixedNullable(RegistryNetworkCodecs.id(RegistryKey.PARTICLE_TYPE)), ClientboundExplodePacket::explosionParticle)
            .then(PrimitiveCodecs.prefixedNullable(SoundEventWriter.SOUND_EVENT), ClientboundExplodePacket::explosionSoundKey)
            .then(BlockParticleAlternative.CODEC.listOf(PacketBuffer::writeVarInt), ClientboundExplodePacket::blockParticleAlternatives);

    @Override
    public Key name() {
        return PlayClientboundPackets.EXPLODE;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundExplodePacket> codec() {
        return CODEC;
    }

    private @Nullable Key explosionSoundKey() {
        final Sound.Type sound = this.explosionSound;
        return sound == null ? null : sound.key();
    }

    public record BlockParticleAlternative(TypedKey<ParticleType> particle, float scaling, float speed, int weight) {
        public static final NetworkEncoder<CodecAwarePacketBuffer, BlockParticleAlternative> CODEC = NetworkEncoder
                .field(RegistryNetworkCodecs.id(RegistryKey.PARTICLE_TYPE), BlockParticleAlternative::particle)
                // TODO: Particle data (https://minecraft.wiki/w/Java_Edition_protocol/Particles)
                //.then(?, BlockParticleAlternative::data)
                .then(PrimitiveCodecs.FLOAT, BlockParticleAlternative::scaling)
                .then(PrimitiveCodecs.FLOAT, BlockParticleAlternative::speed)
                .then(PrimitiveCodecs.VAR_INT, BlockParticleAlternative::weight);
    }
}
