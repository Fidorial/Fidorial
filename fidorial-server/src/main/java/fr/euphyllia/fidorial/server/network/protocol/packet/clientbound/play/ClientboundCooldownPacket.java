package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.StringLikeCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;

/**
 * Greys out the items of a cooldown group in the hotbar of a player, and keeps their client from
 * using them, for a while.
 *
 * <p><a href="https://minecraft.wiki/w/Java_Edition_protocol/Packets#Set_Cooldown">Set Cooldown</a></p>
 *
 * @param group the cooldown group, the id of the item for most items
 * @param ticks how long the cooldown lasts, {@code 0} to end it
 */
public record ClientboundCooldownPacket(Key group, int ticks) implements ClientboundPacket<ClientboundCooldownPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundCooldownPacket> CODEC = NetworkEncoder
            .field(StringLikeCodecs.KEY, ClientboundCooldownPacket::group)
            .then(PrimitiveCodecs.VAR_INT, ClientboundCooldownPacket::ticks);

    @Override
    public Key name() {
        return PlayClientboundPackets.COOLDOWN;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundCooldownPacket> codec() {
        return CODEC;
    }
}
