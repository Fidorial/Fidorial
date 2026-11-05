package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.StringLikeCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.codecs.networking.RegistryNetworkCodecs;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import fr.euphyllia.fidorial.server.util.annotations.NeedsToBeRevisited;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.world.dimension.DimensionTypeDefinition;
import net.kyori.adventure.key.Key;

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#Login_(play)
@NeedsToBeRevisited("Sends stubs for some fields")
public record ClientboundLoginPacket(
        int entityId,
        boolean isHardcore,
        Key[] dimensions,
        Key dimensionKey,
        TypedKey<DimensionTypeDefinition> dimensionType,
        int viewDistance,
        int simulationDistance,
        int gameMode,
        boolean isFlat,
        boolean onlineMode,
        boolean enforcesSecureChat,
        boolean reducedDebugInfo,
        boolean enableRespawnScreen,
        boolean doLimitedCrafting
) implements ClientboundPacket<ClientboundLoginPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundLoginPacket> CODEC = NetworkEncoder
            .field(PrimitiveCodecs.INT, ClientboundLoginPacket::entityId)
            .then(PrimitiveCodecs.BOOL, ClientboundLoginPacket::isHardcore)
            .then(StringLikeCodecs.KEY_ARRAY, ClientboundLoginPacket::dimensions)
            .thenConstant(PrimitiveCodecs.VAR_INT, 0) // maxPlayers (obsolete)
            .then(PrimitiveCodecs.VAR_INT, ClientboundLoginPacket::viewDistance)
            .then(PrimitiveCodecs.VAR_INT, ClientboundLoginPacket::simulationDistance)
            .then(PrimitiveCodecs.BOOL, ClientboundLoginPacket::reducedDebugInfo) // game rule reduced_debug_info
            .then(PrimitiveCodecs.BOOL, ClientboundLoginPacket::enableRespawnScreen) // !immediate_respawn
            .then(PrimitiveCodecs.BOOL, ClientboundLoginPacket::doLimitedCrafting) // game rule limited_crafting
            .then(RegistryNetworkCodecs.id(RegistryKey.DIMENSION_TYPE), ClientboundLoginPacket::dimensionType)
            .then(StringLikeCodecs.KEY, ClientboundLoginPacket::dimensionKey)
            .then(PrimitiveCodecs.VAR_INT, ClientboundLoginPacket::gameMode)
            .thenConstant(PrimitiveCodecs.VAR_INT, 0) // previousGameMode
            .then(PrimitiveCodecs.BOOL, ClientboundLoginPacket::isFlat)
            .thenConstant(PrimitiveCodecs.BOOL, false) // hasDeathLocation
            .thenConstant(PrimitiveCodecs.VAR_INT, 0) // portalCooldown
            .thenConstant(PrimitiveCodecs.VAR_INT, 63) // seaLevel
            .then(PrimitiveCodecs.BOOL, ClientboundLoginPacket::onlineMode)
            .then(PrimitiveCodecs.BOOL, ClientboundLoginPacket::enforcesSecureChat);

    @Override
    public Key name() {
        return PlayClientboundPackets.LOGIN;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundLoginPacket> codec() {
        return CODEC;
    }
}
