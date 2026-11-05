package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;

/**
 * Plays a one-shot animation on an entity: an arm swing, or the sparks of a critical hit.
 *
 * <p><a href="https://minecraft.wiki/w/Java_Edition_protocol/Packets#Entity_Animation">Entity Animation</a></p>
 */
public record ClientboundAnimatePacket(int entityId, int action) implements ClientboundPacket<ClientboundAnimatePacket> {
    public static final int LEAVE_BED = 2;
    public static final int CRITICAL_HIT = 4;
    public static final int MAGIC_CRITICAL_HIT = 5;

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundAnimatePacket> CODEC = NetworkEncoder
            .field(PrimitiveCodecs.VAR_INT, ClientboundAnimatePacket::entityId)
            .then(PrimitiveCodecs.UNSIGNED_BYTE, ClientboundAnimatePacket::action);

    public static ClientboundAnimatePacket leaveBed(final int entityId) {
        return new ClientboundAnimatePacket(entityId, LEAVE_BED);
    }

    public static ClientboundAnimatePacket criticalHit(final int entityId) {
        return new ClientboundAnimatePacket(entityId, CRITICAL_HIT);
    }

    public static ClientboundAnimatePacket magicCriticalHit(final int entityId) {
        return new ClientboundAnimatePacket(entityId, MAGIC_CRITICAL_HIT);
    }

    @Override
    public Key name() {
        return PlayClientboundPackets.ANIMATE;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundAnimatePacket> codec() {
        return CODEC;
    }
}
