package fr.euphyllia.fidorial.server.network.protocol.packet;

import fr.euphyllia.fidorial.server.codecs.networking.NetworkDecoder;
import fr.euphyllia.fidorial.server.network.connection.ConnectionState;
import fr.euphyllia.fidorial.server.network.protocol.catalog.ConfigurationServerboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.catalog.HandshakeServerboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.catalog.LoginServerboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayServerboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.catalog.StatusServerboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.common.ServerboundClientInformationPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.common.ServerboundCustomClickActionPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.common.ServerboundResourcePackPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.configuration.ServerboundAcceptCodeOfConductPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.configuration.ServerboundFinishConfigurationPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.configuration.ServerboundSelectKnownPacksPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.handshake.ServerboundIntentionPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.login.ServerboundCustomQueryAnswerPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.login.ServerboundHelloPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.login.ServerboundKeyPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.login.ServerboundLoginAcknowledgedPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.login.ServerboundOptionsAppliedPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundAcceptTeleportationPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundAcknowledgeConfigurationPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundAttackPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundChatAckPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundChatCommandPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundChatPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundChatSessionUpdatePacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundClientCommandPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundClientTickEndPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundCommandSuggestionPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundContainerClickPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundContainerClosePacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundContainerSlotStateChangedPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundDebugSubscriptionRequestPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundInteractPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundKeepAlivePacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundMovePlayerPosPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundMovePlayerPosRotPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundMovePlayerRotPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundMovePlayerStatusOnlyPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundPlayerActionPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundPlayerInputPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundPlayerLoadedPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundPunchPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundSetCarriedItemPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundSetCreativeModeSlotPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundSetGameRulePacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundUseItemOnPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundUseItemPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.status.ServerboundPingRequestPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.status.ServerboundStatusRequestPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import fr.fidorial.protocol.ServerboundPacket;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import static fr.euphyllia.fidorial.server.FidorialServer.LOGGER;

public class ServerboundPackets {

    private static final Map<ConnectionState, Map<Key, NetworkDecoder<? super CodecAwarePacketBuffer, ? extends ServerboundPacket>>> READERS = new EnumMap<>(ConnectionState.class);

    static {
        on(ConnectionState.HANDSHAKE)
                .register(HandshakeServerboundPackets.INTENTION, ServerboundIntentionPacket.DECODER);

        on(ConnectionState.STATUS)
                .register(StatusServerboundPackets.STATUS_REQUEST, ServerboundStatusRequestPacket.DECODER)
                .register(StatusServerboundPackets.PING_REQUEST, ServerboundPingRequestPacket.DECODER);

        on(ConnectionState.LOGIN)
                .register(LoginServerboundPackets.HELLO, ServerboundHelloPacket.DECODER)
                .register(LoginServerboundPackets.KEY, ServerboundKeyPacket.DECODER)
                .register(LoginServerboundPackets.CUSTOM_QUERY_ANSWER, ServerboundCustomQueryAnswerPacket.DECODER)
                .register(LoginServerboundPackets.LOGIN_ACKNOWLEDGED, ServerboundLoginAcknowledgedPacket.DECODER)
                .register(LoginServerboundPackets.OPTIONS_APPLIED, ServerboundOptionsAppliedPacket.DECODER);

        on(ConnectionState.CONFIGURATION)
                .register(ConfigurationServerboundPackets.SELECT_KNOWN_PACKS, ServerboundSelectKnownPacksPacket.DECODER)
                .register(ConfigurationServerboundPackets.CLIENT_INFORMATION, ServerboundClientInformationPacket.DECODER)
                .register(ConfigurationServerboundPackets.CUSTOM_CLICK_ACTION, ServerboundCustomClickActionPacket.DECODER)
                .register(ConfigurationServerboundPackets.RESOURCE_PACK, ServerboundResourcePackPacket.DECODER)
                .register(ConfigurationServerboundPackets.FINISH_CONFIGURATION, ServerboundFinishConfigurationPacket.DECODER)
                .register(ConfigurationServerboundPackets.ACCEPT_CODE_OF_CONDUCT, ServerboundAcceptCodeOfConductPacket.DECODER);

        on(ConnectionState.PLAY)
                .register(PlayServerboundPackets.PLAYER_LOADED, ServerboundPlayerLoadedPacket.DECODER)
                .register(PlayServerboundPackets.ACCEPT_TELEPORTATION, ServerboundAcceptTeleportationPacket.DECODER)
                .register(PlayServerboundPackets.KEEP_ALIVE, ServerboundKeepAlivePacket.DECODER)
                .register(PlayServerboundPackets.SET_CREATIVE_MODE_SLOT, ServerboundSetCreativeModeSlotPacket.DECODER)
                .register(PlayServerboundPackets.USE_ITEM_ON, ServerboundUseItemOnPacket.DECODER)
                .register(PlayServerboundPackets.USE_ITEM, ServerboundUseItemPacket.DECODER)
                .register(PlayServerboundPackets.PLAYER_ACTION, ServerboundPlayerActionPacket.DECODER)
                .register(PlayServerboundPackets.SET_CARRIED_ITEM, ServerboundSetCarriedItemPacket.DECODER)
                .register(PlayServerboundPackets.MOVE_PLAYER_POS, ServerboundMovePlayerPosPacket.DECODER)
                .register(PlayServerboundPackets.MOVE_PLAYER_ROT, ServerboundMovePlayerRotPacket.DECODER)
                .register(PlayServerboundPackets.MOVE_PLAYER_POS_ROT, ServerboundMovePlayerPosRotPacket.DECODER)
                .register(PlayServerboundPackets.MOVE_PLAYER_STATUS_ONLY, ServerboundMovePlayerStatusOnlyPacket.DECODER)
                .register(PlayServerboundPackets.CLIENT_INFORMATION, ServerboundClientInformationPacket.DECODER)
                .register(PlayServerboundPackets.CLIENT_TICK_END, ServerboundClientTickEndPacket.DECODER)
                .register(PlayServerboundPackets.CHAT_ACK, ServerboundChatAckPacket.DECODER)
                .register(PlayServerboundPackets.CHAT_SESSION_UPDATE, ServerboundChatSessionUpdatePacket.DECODER)
                .register(PlayServerboundPackets.CHAT_COMMAND, ServerboundChatCommandPacket.DECODER)
                .register(PlayServerboundPackets.CHAT, ServerboundChatPacket.DECODER)
                .register(PlayServerboundPackets.COMMAND_SUGGESTION, ServerboundCommandSuggestionPacket.DECODER)
                .register(PlayServerboundPackets.CONTAINER_CLICK, ServerboundContainerClickPacket.DECODER)
                .register(PlayServerboundPackets.CONTAINER_CLOSE, ServerboundContainerClosePacket.DECODER)
                .register(PlayServerboundPackets.CONTAINER_SLOT_STATE_CHANGED, ServerboundContainerSlotStateChangedPacket.DECODER)
                .register(PlayServerboundPackets.CUSTOM_CLICK_ACTION, ServerboundCustomClickActionPacket.DECODER)
                .register(PlayServerboundPackets.RESOURCE_PACK, ServerboundResourcePackPacket.DECODER)
                .register(PlayServerboundPackets.ATTACK, ServerboundAttackPacket.DECODER)
                .register(PlayServerboundPackets.INTERACT, ServerboundInteractPacket.DECODER)
                .register(PlayServerboundPackets.PUNCH, ServerboundPunchPacket.DECODER)
                .register(PlayServerboundPackets.CLIENT_COMMAND, ServerboundClientCommandPacket.DECODER)
                .register(PlayServerboundPackets.PLAYER_INPUT, ServerboundPlayerInputPacket.DECODER)
                .register(PlayServerboundPackets.SET_GAME_RULE, ServerboundSetGameRulePacket.DECODER)
                .register(PlayServerboundPackets.CONFIGURATION_ACKNOWLEDGED, ServerboundAcknowledgeConfigurationPacket.DECODER)
                .register(PlayServerboundPackets.DEBUG_SUBSCRIPTION_REQUEST, ServerboundDebugSubscriptionRequestPacket.DECODER);
    }

    private ServerboundPackets() {
    }

    private static StateReaders on(final ConnectionState state) {
        return new StateReaders(READERS.computeIfAbsent(state, _ -> new HashMap<>()));
    }

    public static @Nullable ServerboundPacket decode(final ConnectionState state, final Key name, final CodecAwarePacketBuffer buf) {
        final NetworkDecoder<? super CodecAwarePacketBuffer, ? extends ServerboundPacket> reader = READERS.getOrDefault(state, Map.of()).get(name);
        final ServerboundPacket read = reader == null ? null : reader.read(buf);
        if (buf.nettyBuf().isReadable() && read != null) {
            LOGGER.warn("Packet {} left {} unread bytes (state={})",
                    read.getClass().getSimpleName(), buf.readableBytes(), state);
        }
        return read;
    }

    private record StateReaders(Map<Key, NetworkDecoder<? super CodecAwarePacketBuffer, ? extends ServerboundPacket>> readers) {

        StateReaders register(final Key name, final NetworkDecoder<? super CodecAwarePacketBuffer, ? extends ServerboundPacket> decoder) {
            readers.put(name, decoder);
            return this;
        }
    }
}
