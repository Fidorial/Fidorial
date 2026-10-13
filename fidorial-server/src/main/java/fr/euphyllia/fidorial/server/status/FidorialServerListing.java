package fr.euphyllia.fidorial.server.status;

import fr.euphyllia.fidorial.server.configuration.ServerConfiguration;
import fr.fidorial.status.Favicon;
import fr.fidorial.status.ServerListing;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public final class FidorialServerListing implements ServerListing {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(FidorialServerListing.class);
    private static final Path SERVER_ICON = Path.of("server-icon.png");

    private volatile @Nullable Favicon favicon;
    private volatile Component description;
    private volatile int maxPlayers;

    public FidorialServerListing(final ServerConfiguration config) {
        this.favicon = loadFavicon();
        this.description = config.status().motd();
        this.maxPlayers = config.status().maxPlayers();
    }

    private static @Nullable Favicon loadFavicon() {
        if (!Files.isRegularFile(SERVER_ICON)) {
            return null;
        }
        try {
            return Favicon.read(SERVER_ICON);
        } catch (final Exception e) {
            LOGGER.warn("Could not load server icon", e);
            return null;
        }
    }

    @Override
    public Optional<Favicon> favicon() {
        return Optional.ofNullable(favicon);
    }

    @Override
    public void favicon(final Favicon favicon) {
        this.favicon = favicon;
    }

    @Override
    public Component description() {
        return description;
    }

    @Override
    public void description(final Component description) {
        this.description = description;
    }

    @Override
    public int maxPlayers() {
        return maxPlayers;
    }

    @Override
    public void maxPlayers(final int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }
}
