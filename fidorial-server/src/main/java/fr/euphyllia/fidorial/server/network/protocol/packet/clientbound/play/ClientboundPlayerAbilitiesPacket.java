package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.fidorial.entity.GameMode;
import net.kyori.adventure.key.Key;

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#Player_Abilities_(clientbound)
public record ClientboundPlayerAbilitiesPacket(byte flags, float flyingSpeed, float fovModifier)
        implements ClientboundPacket {

    public static final byte INVULNERABLE = 0x01;
    public static final byte FLYING = 0x02;
    public static final byte ALLOW_FLYING = 0x04;
    public static final byte CREATIVE_MODE = 0x08;

    private static final float DEFAULT_FLY_SPEED = 0.05f;
    private static final float DEFAULT_FOV_MODIFIER = 0.1f;

    public static ClientboundPlayerAbilitiesPacket forGameMode(final GameMode mode) {
        final byte flags =
                switch (mode) {
                    case SURVIVAL, ADVENTURE -> 0;
                    case CREATIVE -> (byte) (INVULNERABLE | ALLOW_FLYING | CREATIVE_MODE);
                    case SPECTATOR -> (byte) (INVULNERABLE | FLYING | ALLOW_FLYING);
                };
        return new ClientboundPlayerAbilitiesPacket(flags, DEFAULT_FLY_SPEED, DEFAULT_FOV_MODIFIER);
    }

    @Override
    public Key name() {
        return PlayClientboundPackets.PLAYER_ABILITIES;
    }

    @Override
    public void write(final PacketBuffer buf) {
        buf.writeByte(flags).writeFloat(flyingSpeed).writeFloat(fovModifier);
    }
}
