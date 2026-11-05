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

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#Respawn
@NeedsToBeRevisited("Sends stubs for some fields")
public record ClientboundRespawnPacket(Key dimensionKey, TypedKey<DimensionTypeDefinition> dimensionType, int gameMode, int dataToKeep, boolean isFlat)
        implements ClientboundPacket<ClientboundRespawnPacket> {

    /**
     * Keep nothing across the dimension change.
     */
    public static final int KEEP_NOTHING = 0;
    /**
     * Keep the player's attributes across the dimension change.
     */
    public static final int KEEP_ATTRIBUTES = 1;
    /**
     * Keep the player's entity metadata across the dimension change.
     */
    public static final int KEEP_METADATA = 2;
    /**
     * Keep both attributes and metadata across the dimension change.
     */
    public static final int KEEP_ALL = KEEP_ATTRIBUTES | KEEP_METADATA;

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundRespawnPacket> CODEC = NetworkEncoder
            .field(RegistryNetworkCodecs.id(RegistryKey.DIMENSION_TYPE), ClientboundRespawnPacket::dimensionType)
            .then(StringLikeCodecs.KEY, ClientboundRespawnPacket::dimensionKey) // dimension name
            .then(PrimitiveCodecs.VAR_INT, ClientboundRespawnPacket::gameMode) // game mode (unsigned byte)
            .thenConstant(PrimitiveCodecs.VAR_INT, 0) // previous game mode (0 = undefined)
            .then(PrimitiveCodecs.BOOL, ClientboundRespawnPacket::isFlat)
            .thenConstant(PrimitiveCodecs.BOOL, false) // has death location
            .thenConstant(PrimitiveCodecs.VAR_INT, 0) // portal cooldown ticks
            .thenConstant(PrimitiveCodecs.VAR_INT, 63) // sea level
            .then(PrimitiveCodecs.UNSIGNED_BYTE, ClientboundRespawnPacket::dataToKeep);

    public ClientboundRespawnPacket(final Key dimensionKey, final TypedKey<DimensionTypeDefinition> dimensionType, final int gameMode, final boolean isFlat) {
        this(dimensionKey, dimensionType, gameMode, KEEP_NOTHING, isFlat);
    }

    @Override
    public Key name() {
        return PlayClientboundPackets.RESPAWN;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundRespawnPacket> codec() {
        return CODEC;
    }
}
