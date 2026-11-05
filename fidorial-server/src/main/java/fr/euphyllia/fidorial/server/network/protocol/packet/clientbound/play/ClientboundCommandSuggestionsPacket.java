package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import com.mojang.brigadier.Message;
import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.StringLikeCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.TextCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import java.util.List;

public record ClientboundCommandSuggestionsPacket(int id, int start, int length, List<Entry> entries)
        implements ClientboundPacket<ClientboundCommandSuggestionsPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundCommandSuggestionsPacket> CODEC = NetworkEncoder
            .field(PrimitiveCodecs.VAR_INT, ClientboundCommandSuggestionsPacket::id)
            .then(PrimitiveCodecs.VAR_INT, ClientboundCommandSuggestionsPacket::start)
            .then(PrimitiveCodecs.VAR_INT, ClientboundCommandSuggestionsPacket::length)
            .then(Entry.CODEC.listOf(PacketBuffer::writeVarInt), ClientboundCommandSuggestionsPacket::entries);

    @Override
    public Key name() {
        return PlayClientboundPackets.COMMAND_SUGGESTIONS;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundCommandSuggestionsPacket> codec() {
        return CODEC;
    }

    public record Entry(String text, @Nullable Message tooltip) {

        public static final NetworkEncoder<PacketBuffer, Entry> CODEC = NetworkEncoder
                .field(StringLikeCodecs.STRING, Entry::text)
                .then(PrimitiveCodecs.prefixedNullable(TextCodecs.COMPONENT), Entry::tooltipComponent);

        private @Nullable Component tooltipComponent() {
            final Message tooltip = this.tooltip;
            return tooltip == null ? null : tooltip.asComponent();
        }
    }
}
