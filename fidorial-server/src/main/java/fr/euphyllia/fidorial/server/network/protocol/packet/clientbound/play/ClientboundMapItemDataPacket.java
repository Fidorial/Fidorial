package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.map.MapPatch;
import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Paints a map canvas and replaces its icons.
 *
 * <p><a href="https://minecraft.wiki/w/Java_Edition_protocol/Packets#Map_Data">Map Data</a></p>
 *
 * @param mapId       the map id
 * @param scale       the zoom level, {@code 0}-{@code 4}
 * @param locked      whether the map is locked in a cartography table
 * @param decorations the icons replacing the previous ones, or {@code null} to keep them
 * @param patch       the pixels to paint, or {@code null} to only update the other fields
 */
public record ClientboundMapItemDataPacket(int mapId, int scale, boolean locked,
                                           @Nullable List<Decoration> decorations, @Nullable MapPatch patch)
        implements ClientboundPacket {

    /**
     * Paints pixels, keeping the icons.
     *
     * @param mapId  the map id
     * @param scale  the zoom level
     * @param locked whether the map is locked
     * @param patch  the pixels, or {@code null}
     */
    public ClientboundMapItemDataPacket(final int mapId, final int scale, final boolean locked,
                                        final @Nullable MapPatch patch) {
        this(mapId, scale, locked, null, patch);
    }

    @Override
    public Key name() {
        return PlayClientboundPackets.MAP_ITEM_DATA;
    }

    @Override
    public void write(final PacketBuffer buf) {
        buf.writeVarInt(mapId);
        buf.writeByte(scale);
        buf.writeBoolean(locked);
        final List<Decoration> icons = decorations;
        buf.writeBoolean(icons != null);
        if (icons != null) {
            buf.writeVarInt(icons.size());
            for (final Decoration icon : icons) {
                buf.writeVarInt(icon.type());
                buf.writeByte(icon.x());
                buf.writeByte(icon.y());
                buf.writeByte(icon.rotation() & 15);
                final Component label = icon.label();
                buf.writeBoolean(label != null);
                if (label != null) {
                    buf.writeComponent(label);
                }
            }
        }
        final MapPatch p = patch;
        if (p == null) {
            buf.writeByte(0);
            return;
        }
        buf.writeByte(p.width());
        buf.writeByte(p.height());
        buf.writeByte(p.x());
        buf.writeByte(p.y());
        buf.writeByteArray(p.colors());
    }

    /**
     * An icon as the client receives it.
     *
     * @param type     the network id of the {@code minecraft:map_decoration_type}
     * @param x        the column, in half pixels from the center, {@code -128}-{@code 127}
     * @param y        the row, in half pixels from the center, {@code -128}-{@code 127}
     * @param rotation the rotation, {@code 0}-{@code 15}
     * @param label    the label, or {@code null}
     */
    public record Decoration(int type, int x, int y, int rotation, @Nullable Component label) {
    }
}
