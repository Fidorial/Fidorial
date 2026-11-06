package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import fr.fidorial.entity.GameMode;
import fr.fidorial.entity.player.PlayerAbilities;
import net.kyori.adventure.key.Key;

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#Player_Abilities_(clientbound)
public record ClientboundPlayerAbilitiesPacket(byte flags, float flySpeed, float walkSpeed)
        implements ClientboundPacket<ClientboundPlayerAbilitiesPacket> {

    public static final byte INVULNERABLE = 0x01;
    public static final byte FLYING = 0x02;
    public static final byte ALLOW_FLYING = 0x04;
    public static final byte CREATIVE_MODE = 0x08;

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundPlayerAbilitiesPacket> CODEC = NetworkEncoder
            .field(PrimitiveCodecs.BYTE, ClientboundPlayerAbilitiesPacket::flags)
            .then(PrimitiveCodecs.FLOAT, ClientboundPlayerAbilitiesPacket::flySpeed)
            .then(PrimitiveCodecs.FLOAT, ClientboundPlayerAbilitiesPacket::walkSpeed);

    public static ClientboundPlayerAbilitiesPacket abilities(final GameMode mode, final PlayerAbilities abilities) {
        final boolean invulnerable = mode == GameMode.CREATIVE || mode == GameMode.SPECTATOR;
        final byte flags = (byte) ((invulnerable ? INVULNERABLE : 0)
                | (abilities.flying() ? FLYING : 0)
                | (abilities.flightAllowed() ? ALLOW_FLYING : 0)
                | (mode == GameMode.CREATIVE ? CREATIVE_MODE : 0));
        return new ClientboundPlayerAbilitiesPacket(flags, abilities.flySpeed(), abilities.walkSpeed());
    }

    @Override
    public Key name() {
        return PlayClientboundPackets.PLAYER_ABILITIES;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundPlayerAbilitiesPacket> codec() {
        return CODEC;
    }
}
