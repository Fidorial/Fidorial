package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.login;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.catalog.LoginClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import net.kyori.adventure.key.Key;

/**
 * Asks the client to switch on the given connection options (see {@link ConnectionOptions}).
 * The client answers with {@code options_applied}; the server only switches them on once it receives that answer.
 */
public record ClientboundApplyOptionsPacket(byte options) implements ClientboundPacket {

    @Override
    public Key name() {
        return LoginClientboundPackets.APPLY_OPTIONS;
    }

    @Override
    public void write(final PacketBuffer buf) {
        buf.writeByte(options);
    }
}
