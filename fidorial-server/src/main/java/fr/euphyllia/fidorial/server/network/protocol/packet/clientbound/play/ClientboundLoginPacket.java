package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.util.annotations.NeedsToBeRevisited;
import net.kyori.adventure.key.Key;

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#Login_(play)
@NeedsToBeRevisited("Sends stubs for some fields")
public record ClientboundLoginPacket(
        int entityId,
        boolean isHardcore,
        Key[] dimensions,
        Key dimensionKey,
        int dimensionTypeId,
        long hashedSeed,
        int viewDistance,
        int simulationDistance,
        int gameMode,
        boolean isDebug,
        boolean isFlat,
        boolean onlineMode,
        boolean enforcesSecureChat,
        boolean reducedDebugInfo,
        boolean enableRespawnScreen,
        boolean doLimitedCrafting
) implements ClientboundPacket {

    @Override
    public Key name() {
        return PlayClientboundPackets.LOGIN;
    }

    @Override
    public void write(final PacketBuffer buf) {
        buf.writeInt(entityId);
        buf.writeBoolean(isHardcore); // hardcore
        buf.writeKeyArray(dimensions); // liste des dimensions
        buf.writeVarInt(0); // maxPlayers (obsolete)
        buf.writeVarInt(viewDistance);
        buf.writeVarInt(simulationDistance);
        buf.writeBoolean(reducedDebugInfo); // game rule reduced_debug_info
        buf.writeBoolean(enableRespawnScreen); // !immediate_respawn
        buf.writeBoolean(doLimitedCrafting); // game rule limited_crafting
        buf.writeVarInt(dimensionTypeId);
        buf.writeKey(dimensionKey);
        buf.writeLong(hashedSeed); // hashedSeed
        buf.writeVarInt(gameMode); // gameMode (survie)
        buf.writeVarInt(0); // previousGameMode
        buf.writeBoolean(isDebug); // isDebug
        buf.writeBoolean(isFlat); // isFlat
        buf.writeBoolean(false); // hasDeathLocation
        buf.writeVarInt(0); // portalCooldown
        buf.writeVarInt(63); // seaLevel
        buf.writeBoolean(onlineMode);
        buf.writeBoolean(enforcesSecureChat);
    }
}
