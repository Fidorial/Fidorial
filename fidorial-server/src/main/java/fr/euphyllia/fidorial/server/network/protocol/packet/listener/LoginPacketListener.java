package fr.euphyllia.fidorial.server.network.protocol.packet.listener;

import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.login.ServerboundCustomQueryAnswerPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.login.ServerboundHelloPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.login.ServerboundKeyPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.login.ServerboundLoginAcknowledgedPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.login.ServerboundOptionsAppliedPacket;
import fr.fidorial.protocol.PacketListener;

public interface LoginPacketListener extends PacketListener {
    void handleHello(ServerboundHelloPacket packet);

    void handleKey(ServerboundKeyPacket packet);

    void handleCustomQueryAnswer(ServerboundCustomQueryAnswerPacket packet);

    void handleLoginAcknowledged(ServerboundLoginAcknowledgedPacket packet);

    void handleOptionsApplied(ServerboundOptionsAppliedPacket packet);
}
