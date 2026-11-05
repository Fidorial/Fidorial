package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.RootCommandNode;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.command.brigadier.packet.CommandTreeSerializer;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import fr.fidorial.command.CommandSource;
import net.kyori.adventure.key.Key;

public record ClientboundCommandsPacket(CommandDispatcher<CommandSource> dispatcher, CommandSource source)
        implements ClientboundPacket<ClientboundCommandsPacket> {
    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundCommandsPacket> CODEC = NetworkEncoder.member(ClientboundCommandsPacket::encode);

    @Override
    public Key name() {
        return PlayClientboundPackets.COMMANDS;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundCommandsPacket> codec() {
        return CODEC;
    }

    private void encode(final CodecAwarePacketBuffer buf) {
        final RootCommandNode<CommandSource> filtered = CommandTreeSerializer.filter(dispatcher.getRoot(), source);
        CommandTreeSerializer.write(buf, filtered);
    }
}
