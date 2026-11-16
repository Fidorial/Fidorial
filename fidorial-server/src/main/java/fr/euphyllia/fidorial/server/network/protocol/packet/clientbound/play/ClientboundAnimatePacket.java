package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;

/**
 * Plays a one-shot animation on an entity: a player waking up, or the sparks of a critical hit. An
 * arm swing has its own packet, {@link ClientboundSwingAnimationPacket}.
 *
 * <p><a href="https://minecraft.wiki/w/Java_Edition_protocol/Packets#Entity_Animation">Entity Animation</a></p>
 */
public record ClientboundAnimatePacket(int entityId, int action) implements ClientboundPacket<ClientboundAnimatePacket> {
    /**
     * Only a player can wake up: the client takes the entity of this action for a player.
     */
    public static final int WAKE_UP = 0;
    public static final int CRITICAL_HIT = 1;
    public static final int MAGIC_CRITICAL_HIT = 2;

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundAnimatePacket> CODEC = NetworkEncoder
            .field(PrimitiveCodecs.VAR_INT, ClientboundAnimatePacket::entityId)
            .then(PrimitiveCodecs.UNSIGNED_BYTE, ClientboundAnimatePacket::action);

    /**
     * {@return the packet that shows a player waking up}
     *
     * @param playerId the entity id of a player, never of another entity
     */
    public static ClientboundAnimatePacket wakeUp(final int playerId) {
        return new ClientboundAnimatePacket(playerId, WAKE_UP);
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
