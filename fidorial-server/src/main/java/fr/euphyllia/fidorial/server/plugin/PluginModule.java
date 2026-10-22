package fr.euphyllia.fidorial.server.plugin;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.configuration.ServerConfiguration;
import fr.euphyllia.fidorial.server.events.SimpleEventBus;
import fr.euphyllia.fidorial.server.lifecycle.Quietly;
import fr.euphyllia.fidorial.server.lifecycle.ServerModule;
import fr.euphyllia.fidorial.server.permission.DefaultPermissions;
import fr.euphyllia.fidorial.server.registry.RegistryModule;
import fr.euphyllia.fidorial.server.spark.SparkService;
import fr.fidorial.plugin.Plugin;
import fr.fidorial.plugin.PluginManager;
import fr.fidorial.plugin.PluginMeta;
import fr.fidorial.service.ServiceRegistry;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

/**
 * Plugins, and the spark profiler.
 *
 * <p>Added last: plugins are loaded once every other module is loaded, enabled once every other
 * module is started, and disabled before anything else stops.</p>
 *
 * <p>This is the server's {@link PluginManager}, backed by {@link #manager()}.</p>
 */
public final class PluginModule implements ServerModule, PluginManager {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(PluginModule.class);

    private final RegistryModule registries;
    private final JavaPluginManager manager;
    private final @Nullable SparkService spark;

    public PluginModule(
            final FidorialServer server, final ServerConfiguration config, final SimpleEventBus events,
            final ServiceRegistry services, final RegistryModule registries) {
        this.registries = registries;
        this.spark = config.spark().enabled() ? new SparkService(server, config.spark().path()) : null;
        this.manager = new JavaPluginManager(server, events, services, registries.permissions(), config.general().pluginsPath());
    }

    @Override
    public void load() throws IOException {
        // spark first, so that loading the plugins can be profiled
        if (spark != null) {
            spark.enable();
        } else {
            LOGGER.debug("spark is disabled by configuration");
        }
        DefaultPermissions.register(registries.permissions());
        manager.loadAll();
    }

    @Override
    public void start() {
        manager.enableAll();
    }

    @Override
    public void stop() {
        Quietly.run("plugins", manager::close);
        if (spark != null) {
            Quietly.run("spark", spark::disable);
        }
    }

    public JavaPluginManager manager() {
        return manager;
    }

    public @Nullable SparkService spark() {
        return spark;
    }

    @Override
    public Collection<PluginMeta> loaded() {
        return manager.loaded();
    }

    @Override
    public Optional<Plugin> plugin(final String id) {
        return manager.plugin(id);
    }

    @Override
    public boolean isEnabled(final String id) {
        return manager.isEnabled(id);
    }
}
