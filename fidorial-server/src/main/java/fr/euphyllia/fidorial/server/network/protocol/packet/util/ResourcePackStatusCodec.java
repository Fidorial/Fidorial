package fr.euphyllia.fidorial.server.network.protocol.packet.util;

import io.netty.handler.codec.DecoderException;
import net.kyori.adventure.resource.ResourcePackStatus;

public final class ResourcePackStatusCodec {

    private ResourcePackStatusCodec() {
    }

    // look at Result box under the packet: https://minecraft.wiki/w/Java_Edition_protocol/Packets#Resource_Pack_Response
    public static ResourcePackStatus fromWireId(final int wireId) {
        return switch (wireId) {
            case 0 -> ResourcePackStatus.SUCCESSFULLY_LOADED;
            case 1 -> ResourcePackStatus.DECLINED;
            case 2 -> ResourcePackStatus.FAILED_DOWNLOAD;
            case 3 -> ResourcePackStatus.ACCEPTED;
            case 4 -> ResourcePackStatus.DOWNLOADED;
            case 5 -> ResourcePackStatus.INVALID_URL;
            case 6 -> ResourcePackStatus.FAILED_RELOAD;
            case 7 -> ResourcePackStatus.DISCARDED;
            default -> throw new DecoderException("Unexpected resource pack status: " + wireId);
        };
    }
}
