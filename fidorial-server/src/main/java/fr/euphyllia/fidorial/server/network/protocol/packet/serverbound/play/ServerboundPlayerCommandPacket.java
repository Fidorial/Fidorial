package fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkDecoder;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.PlayPacketListener;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
import fr.fidorial.protocol.PacketListener;
import fr.fidorial.protocol.ServerboundPacket;

/**
 * An action the player performed: sprinting, leaving a bed, horse jumps, opening a vehicle's inventory or starting to glide.
 *
 * @param entityId  the player's entity id; ignored
 * @param action    the action performed
 * @param jumpBoost the horse jump strength, from 0 to 100, for {@link Action#START_HORSE_JUMP}; 0 otherwise
 */
// https://minecraft.wiki/w/Java_Edition_protocol/Packets#Player_Command
public record ServerboundPlayerCommandPacket(int entityId, Action action, int jumpBoost) implements ServerboundPacket {

    public static final NetworkDecoder<PacketBuffer, ServerboundPlayerCommandPacket> DECODER = NetworkDecoder.composite(
            PrimitiveCodecs.VAR_INT,
            PrimitiveCodecs.enumByOrdinal(Action.class),
            PrimitiveCodecs.VAR_INT,
            ServerboundPlayerCommandPacket::new);

    @Override
    public void handle(final PacketListener listener) {
        ((PlayPacketListener) listener).handlePlayerCommand(this);
    }

    // in protocol order
    public enum Action {
        LEAVE_BED,
        START_SPRINTING,
        STOP_SPRINTING,
        START_HORSE_JUMP,
        STOP_HORSE_JUMP,
        OPEN_VEHICLE_INVENTORY,
        START_FALL_FLYING
    }
}
