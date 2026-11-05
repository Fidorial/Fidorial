package fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkDecoder;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.PlayPacketListener;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
import fr.fidorial.protocol.PacketListener;
import fr.fidorial.protocol.ServerboundPacket;

/**
 * The full keyboard state of the player, sent whenever it changes.
 *
 * <p><a href="https://minecraft.wiki/w/Java_Edition_protocol/Packets#Player_Input">Player Input</a></p>
 */
public record ServerboundPlayerInputPacket(int flags) implements ServerboundPacket {

    private static final int FORWARD = 0x01;
    private static final int BACKWARD = 0x02;
    private static final int LEFT = 0x04;
    private static final int RIGHT = 0x08;
    private static final int JUMP = 0x10;
    private static final int SHIFT = 0x20;
    private static final int SPRINT = 0x40;

    public static final NetworkDecoder<PacketBuffer, ServerboundPlayerInputPacket> DECODER = NetworkDecoder.composite(
            PrimitiveCodecs.UNSIGNED_BYTE,
            ServerboundPlayerInputPacket::new);

    public boolean forward() {
        return (flags & FORWARD) != 0;
    }

    public boolean backward() {
        return (flags & BACKWARD) != 0;
    }

    public boolean left() {
        return (flags & LEFT) != 0;
    }

    public boolean right() {
        return (flags & RIGHT) != 0;
    }

    public boolean jumping() {
        return (flags & JUMP) != 0;
    }

    public boolean sneaking() {
        return (flags & SHIFT) != 0;
    }

    public boolean sprinting() {
        return (flags & SPRINT) != 0;
    }

    @Override
    public void handle(final PacketListener listener) {
        ((PlayPacketListener) listener).handlePlayerInput(this);
    }
}
