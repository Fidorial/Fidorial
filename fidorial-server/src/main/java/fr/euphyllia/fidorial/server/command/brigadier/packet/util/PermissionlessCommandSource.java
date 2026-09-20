package fr.euphyllia.fidorial.server.command.brigadier.packet.util;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.fidorial.command.CommandSender;
import fr.fidorial.command.CommandSource;
import fr.fidorial.entity.Entity;
import fr.fidorial.math.Location;
import org.jspecify.annotations.Nullable;

public final class PermissionlessCommandSource implements CommandSource {
    @Override
    public Location location() {
        return null;
    }

    @Override
    public CommandSender sender() {
        return PermissionlessCommandSender.INSTANCE;
    }

    @Override
    public @Nullable Entity executor() {
        return null;
    }

    @Override
    public FidorialServer server() {
        return FidorialServer.getInstance();
    }

    public static PermissionlessCommandSource instance() {
        return new PermissionlessCommandSource();
    }
}
