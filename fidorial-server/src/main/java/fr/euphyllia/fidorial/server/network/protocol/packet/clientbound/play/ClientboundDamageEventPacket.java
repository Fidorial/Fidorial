package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.fidorial.math.Location;
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
 * @param damageTypeId   the network id of the entry in {@code minecraft:damage_type}
 * @param causingEntity  the entity credited with the damage, or {@code null}
 * @param directEntity   the entity that landed the hit, or {@code null}
 * @param sourcePosition the point damage radiated from, or {@code null} to derive it from the
 *                       entities
 */
public record ClientboundDamageEventPacket(
        int entityId,
        int damageTypeId,
        @Nullable Integer causingEntity,
        @Nullable Integer directEntity,
        @Nullable Location sourcePosition) implements ClientboundPacket {

    @Override
    public Key name() {
        return PlayClientboundPackets.DAMAGE_EVENT;
    }

    @Override
    public void write(final PacketBuffer buf) {
        buf.writeVarInt(entityId);
        buf.writeVarInt(damageTypeId);
        buf.writeVarInt(causingEntity == null ? 0 : causingEntity + 1);
        buf.writeVarInt(directEntity == null ? 0 : directEntity + 1);
        if (sourcePosition == null) {
            buf.writeBoolean(false);
            return;
        }
        buf.writeBoolean(true);
        buf.writeDouble(sourcePosition.x());
        buf.writeDouble(sourcePosition.y());
        buf.writeDouble(sourcePosition.z());
    }
}
