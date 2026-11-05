package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.TextCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.codecs.networking.RegistryNetworkCodecs;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.MenuType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;

/**
 * Tells the client to open a container window.
 *
 * <p>Format:
 *
 * <pre>
 *   Window ID     VarInt
 *   Window Type   VarInt          (index into the minecraft:menu registry)
 *   Window Title  Text Component
 * </pre>
 *
 * <p>Never sent for the player's own inventory, which is always considered open server-side.
 *
 * <p>https://minecraft.wiki/w/Java_Edition_protocol/Packets#Open_Screen
 */
public record ClientboundOpenScreenPacket(int windowId, TypedKey<MenuType> menuType, Component title)
        implements ClientboundPacket<ClientboundOpenScreenPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundOpenScreenPacket> CODEC = NetworkEncoder
            .field(PrimitiveCodecs.VAR_INT, ClientboundOpenScreenPacket::windowId)
            .then(RegistryNetworkCodecs.id(RegistryKey.MENU), ClientboundOpenScreenPacket::menuType)
            .then(TextCodecs.COMPONENT, ClientboundOpenScreenPacket::title);

    @Override
    public Key name() {
        return PlayClientboundPackets.OPEN_SCREEN;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundOpenScreenPacket> codec() {
        return CODEC;
    }
}
