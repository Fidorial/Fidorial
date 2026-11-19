package fr.euphyllia.fidorial.server.plugin;

import fr.fidorial.Server;
import fr.fidorial.event.EventBus;
import fr.fidorial.messaging.PluginMessenger;
import fr.fidorial.plugin.PluginContext;
import fr.fidorial.plugin.PluginMeta;
import fr.fidorial.service.ServiceRegistry;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

final class SimplePluginContext implements PluginContext, AutoCloseable {

    private final PluginMeta meta;
    private final Server server;
    private final EventBus events;
    private final ServiceRegistry services;
    private final PluginMessenger messenger;
    private final Path dataFolder;
    private final Path jarPath;
    private final ComponentLogger logger;
    private @Nullable JarFile jarFile;
    private boolean jarOpenFailed;

    SimplePluginContext(final PluginMeta meta, final Server server, final EventBus events,
                        final ServiceRegistry services, final PluginMessenger messenger, final Path dataFolder,
                        final Path jarPath) {
        this.meta = meta;
        this.server = server;
        this.events = events;
        this.services = services;
        this.messenger = messenger;
        this.dataFolder = dataFolder;
        this.jarPath = jarPath;
        this.logger = ComponentLogger.logger("plugin/" + meta.id());
    }

    @Override
    public PluginMeta meta() {
        return meta;
    }

    @Override
    public Server server() {
        return server;
    }

    @Override
    public EventBus events() {
        return events;
    }

    @Override
    public ServiceRegistry services() {
        return services;
    }

    @Override
    public PluginMessenger messenger() {
        return messenger;
    }

    @Override
    public ComponentLogger logger() {
        return logger;
    }

    @Override
    public Path dataFolder() {
        try {
            Files.createDirectories(dataFolder);
        } catch (final IOException e) {
            throw new UncheckedIOException("Unable to create data file for " + meta.id() + ".", e);
        }
        return dataFolder;
    }

    @Override
    public @Nullable InputStream resource(final String path) {
        final JarFile jar = openJar();
        if (jar == null) {
            return null;
        }
        final ZipEntry entry = jar.getEntry(path);
        if (entry == null) {
            return null;
        }
        try {
            return jar.getInputStream(entry);
        } catch (final IOException e) {
            logger.warn("Could not read resource '{}' from plugin jar", path, e);
            return null;
        }
    }

    private synchronized @Nullable JarFile openJar() {
        if (jarFile != null) {
            return jarFile;
        }
        if (jarOpenFailed) {
            return null;
        }
        try {
            jarFile = new JarFile(jarPath.toFile());
            return jarFile;
        } catch (final IOException e) {
            jarOpenFailed = true;
            logger.warn("Could not open plugin jar '{}'", jarPath, e);
            return null;
        }
    }

    @Override
    public synchronized void close() {
        if (jarFile == null) {
            return;
        }
        try {
            jarFile.close();
        } catch (final IOException e) {
            logger.warn("Could not close plugin jar '{}'", jarPath, e);
        } finally {
            jarFile = null;
        }
    }
}
