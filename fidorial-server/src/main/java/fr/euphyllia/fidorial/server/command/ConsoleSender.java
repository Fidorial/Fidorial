package fr.euphyllia.fidorial.server.command;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.ServerConfig;
import fr.euphyllia.fidorial.server.network.nbt.ComponentResolver;
import fr.fidorial.command.CommandSender;
import fr.fidorial.command.CommandSource;
import fr.fidorial.entity.Entity;
import fr.fidorial.math.Location;
import fr.fidorial.permission.PermissionResolver;
import fr.fidorial.permission.PermissionState;
import fr.fidorial.permission.PermissionStateHolder;
import fr.fidorial.translation.TranslationStore;
import fr.fidorial.world.World;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.permission.PermissionChecker;
import net.kyori.adventure.pointer.Pointers;
import net.kyori.adventure.pointer.PointersSupplier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public class ConsoleSender implements CommandSender, PermissionStateHolder, CommandSource {

    public static final ComponentLogger LOGGER = ComponentLogger.logger("Console");
    private static final PointersSupplier<ConsoleSender> pointers = PointersSupplier.<ConsoleSender>builder()
            .resolving(Identity.NAME, ConsoleSender::name)
            .resolving(Identity.LOCALE, ConsoleSender::locale)
            .resolving(PermissionChecker.POINTER, sender -> sender::permissionState)
            .build();

    private final PermissionState permissions;
    private Locale locale = Locale.US;

    public ConsoleSender(final FidorialServer server) {
        this.permissions = new PermissionState(
                this,
                server.permissions(),
                () -> server.services()
                        .find(PermissionResolver.class)
                        .map(List::of)
                        .orElseGet(List::of));
    }

    public void setLocale(final String language) {
        this.locale = Locale.forLanguageTag(language);
    }

    public void setLocale(final Locale locale) {
        this.locale = locale;
    }

    public Locale locale() {
        return this.locale;
    }

    @Override
    public Pointers pointers() {
        return pointers.view(this);
    }

    @Override
    public void sendMessage(final Component message) {
        final Component resolved = ComponentResolver.resolve(message, this);
        LOGGER.info(TranslationStore.render(resolved, locale()));
    }

    @Override
    public String name() {
        return "Console";
    }

    @Override
    public PermissionState permissions() {
        return permissions;
    }

    @Override
    public boolean isOperator() {
        return true;
    }

    @Override
    public void setOperator(final boolean operator) {
        throw new UnsupportedOperationException("The console is always an operator");
    }

    @Override
    public Location location() {
        // provide the location as default spawn
        final ServerConfig config = server().config();
        final World world = server().worlds().stream().findAny().orElseThrow();
        final double x = config.spawnX();
        final double y = config.spawnY();
        final double z = config.spawnZ();
        return Location.of(world, x, y, z, 0, 0);
    }

    @Override
    public CommandSender sender() {
        return this;
    }

    @Override
    public @Nullable Entity executor() {
        return null;
    }

    @Override
    public FidorialServer server() {
        return FidorialServer.getInstance();
    }
}
