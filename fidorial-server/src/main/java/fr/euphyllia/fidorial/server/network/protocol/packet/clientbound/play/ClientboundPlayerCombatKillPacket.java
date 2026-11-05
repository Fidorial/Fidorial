package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.TextCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;

public record ClientboundPlayerCombatKillPacket(int playerId, Component message) implements ClientboundPacket<ClientboundPlayerCombatKillPacket> {
    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundPlayerCombatKillPacket> CODEC = NetworkEncoder
            .field(PrimitiveCodecs.VAR_INT, ClientboundPlayerCombatKillPacket::playerId)
            .then(TextCodecs.COMPONENT, ClientboundPlayerCombatKillPacket::message);

    @Override
    public Key name() {
        return PlayClientboundPackets.PLAYER_COMBAT_KILL;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundPlayerCombatKillPacket> codec() {
        return CODEC;
    }
}
