package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.TextCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;

import java.util.UUID;

public sealed interface ClientboundBossEventPacket extends ClientboundPacket<ClientboundBossEventPacket> {

    NetworkEncoder<PacketBuffer, BossBar.Color> COLOR = PrimitiveCodecs.enumByOrdinal(BossBar.Color.class);
    NetworkEncoder<PacketBuffer, BossBar.Overlay> OVERLAY = PrimitiveCodecs.enumByOrdinal(BossBar.Overlay.class);

    /**
     * The id, then the operation's own encoder, which starts with the operation id.
     */
    NetworkEncoder<CodecAwarePacketBuffer, ClientboundBossEventPacket> CODEC = (buf, packet) -> {
        PrimitiveCodecs.UUID.write(buf, packet.id());
        switch (packet) {
            case final Add add -> Add.CODEC.write(buf, add);
            case final Remove remove -> Remove.CODEC.write(buf, remove);
            case final UpdateProgress update -> UpdateProgress.CODEC.write(buf, update);
            case final UpdateName update -> UpdateName.CODEC.write(buf, update);
            case final UpdateStyle update -> UpdateStyle.CODEC.write(buf, update);
            case final UpdateProperties update -> UpdateProperties.CODEC.write(buf, update);
        }
    };

    UUID id();

    @Override
    default Key name() {
        return PlayClientboundPackets.BOSS_EVENT;
    }

    @Override
    default NetworkEncoder<CodecAwarePacketBuffer, ClientboundBossEventPacket> codec() {
        return CODEC;
    }

    record Add(UUID id, Component title, float progress, BossBar.Color color,
               BossBar.Overlay overlay, int flags) implements ClientboundBossEventPacket {

        static final NetworkEncoder<CodecAwarePacketBuffer, Add> CODEC = NetworkEncoder
                .constant(PrimitiveCodecs.VAR_INT, 0)
                .then(TextCodecs.COMPONENT, Add::title)
                .then(PrimitiveCodecs.FLOAT, Add::progress)
                .then(COLOR, Add::color)
                .then(OVERLAY, Add::overlay)
                .then(PrimitiveCodecs.UNSIGNED_BYTE, Add::flags);
    }

    record Remove(UUID id) implements ClientboundBossEventPacket {

        static final NetworkEncoder<CodecAwarePacketBuffer, Remove> CODEC = NetworkEncoder.constant(PrimitiveCodecs.VAR_INT, 1);
    }

    record UpdateProgress(UUID id, float progress) implements ClientboundBossEventPacket {

        static final NetworkEncoder<CodecAwarePacketBuffer, UpdateProgress> CODEC = NetworkEncoder
                .constant(PrimitiveCodecs.VAR_INT, 2)
                .then(PrimitiveCodecs.FLOAT, UpdateProgress::progress);
    }

    record UpdateName(UUID id, Component title) implements ClientboundBossEventPacket {

        static final NetworkEncoder<CodecAwarePacketBuffer, UpdateName> CODEC = NetworkEncoder
                .constant(PrimitiveCodecs.VAR_INT, 3)
                .then(TextCodecs.COMPONENT, UpdateName::title);
    }

    record UpdateStyle(UUID id, BossBar.Color color, BossBar.Overlay overlay) implements ClientboundBossEventPacket {

        static final NetworkEncoder<CodecAwarePacketBuffer, UpdateStyle> CODEC = NetworkEncoder
                .constant(PrimitiveCodecs.VAR_INT, 4)
                .then(COLOR, UpdateStyle::color)
                .then(OVERLAY, UpdateStyle::overlay);
    }

    record UpdateProperties(UUID id, int flags) implements ClientboundBossEventPacket {

        static final NetworkEncoder<CodecAwarePacketBuffer, UpdateProperties> CODEC = NetworkEncoder
                .constant(PrimitiveCodecs.VAR_INT, 5)
                .then(PrimitiveCodecs.UNSIGNED_BYTE, UpdateProperties::flags);
    }
}
