package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.common;

import fr.euphyllia.fidorial.server.codecs.dialog.DialogCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import fr.fidorial.dialog.Dialog;
import fr.fidorial.dialog.DialogDefinition;
import fr.fidorial.dialog.DialogReference;
import fr.fidorial.registry.RegistryKey;
import net.kyori.adventure.key.Key;

import java.util.Objects;

/**
 * Opens a dialog on the client.
 *
 * @param name   the packet identifier for the current connection phase
 * @param dialog the dialog to open; a reference is sent as its network ID in the dialog registry
 * @see <a href="https://minecraft.wiki/w/Java_Edition_protocol/Packets#Show_Dialog">Show Dialog</a>
 */
public record ClientboundShowDialogPacket(Key name, Dialog dialog) implements ClientboundPacket<ClientboundShowDialogPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundShowDialogPacket> CODEC = NetworkEncoder.member(ClientboundShowDialogPacket::encode);

    public ClientboundShowDialogPacket {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(dialog, "dialog");
    }

    /**
     * Creates a packet carrying a full dialog definition.
     *
     * @param name   the packet identifier for the current connection phase
     * @param dialog the dialog to open
     * @return the packet
     */
    public static ClientboundShowDialogPacket inline(final Key name, final DialogDefinition dialog) {
        return new ClientboundShowDialogPacket(name, dialog);
    }

    /**
     * Creates a packet pointing at a registered dialog.
     *
     * @param name      the packet identifier for the current connection phase
     * @param reference the dialog to open
     * @return the packet
     */
    public static ClientboundShowDialogPacket reference(final Key name, final DialogReference reference) {
        return new ClientboundShowDialogPacket(name, reference);
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundShowDialogPacket> codec() {
        return CODEC;
    }

    private void encode(final CodecAwarePacketBuffer buf) {
        switch (dialog) {
            case final DialogReference reference -> buf.writeVarInt(buf.registries().registry(RegistryKey.DIALOG).networkId(reference.key()) + 1);
            case final DialogDefinition definition -> {
                buf.writeVarInt(0);
                buf.writeNbt(DialogCodecs.toNbt(definition));
            }
        }
    }
}
