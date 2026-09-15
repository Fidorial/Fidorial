package fr.euphyllia.fidorial.server.network.listener;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.events.ServerStatusRequestEventImpl;
import fr.euphyllia.fidorial.server.network.ClientConnection;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.status.ClientboundPongResponsePacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.status.ClientboundStatusResponsePacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.StatusPacketListener;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.status.ServerboundPingRequestPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.status.ServerboundStatusRequestPacket;
import fr.euphyllia.fidorial.server.status.StatusResponseBuilder;
import fr.fidorial.event.server.ServerStatusRequestEvent;
import fr.fidorial.status.ServerStatus;

public final class StatusPacketHandler implements StatusPacketListener {

    private final ClientConnection connection;

    public StatusPacketHandler(final ClientConnection connection) {
        this.connection = connection;
    }

    @Override
    public void handleStatusRequest(final ServerboundStatusRequestPacket packet) {
        final FidorialServer server = FidorialServer.getInstance();
        final ServerStatus status = ServerStatus.builder()
                .favicon(server.favicon().orElse(null))
                .description(server.description())
                .maxPlayers(server.maxPlayers())
                .players(server.playerCount())
                .version(new ServerStatus.Version(
                        server.brandName() + " " + server.minecraftVersion(),
                        server.protocolVersion()
                ))
                .enforceSecureChat(server.config().enforcesSecureChat())
                .build();
        final ServerStatusRequestEvent event = new ServerStatusRequestEventImpl(status);
        server.events().post(event);
        final String json = StatusResponseBuilder.build(event.status());
        connection.send(new ClientboundStatusResponsePacket(json));
    }

    @Override
    public void handlePingRequest(final ServerboundPingRequestPacket packet) {
        connection.sendAndClose(new ClientboundPongResponsePacket(packet.payload()));
    }
}
