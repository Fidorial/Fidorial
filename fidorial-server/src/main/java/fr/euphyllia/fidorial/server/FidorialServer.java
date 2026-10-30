package fr.euphyllia.fidorial.server;

import com.google.common.collect.Iterables;
import fr.euphyllia.fidorial.server.bootstrap.GameBootstrap;
import fr.euphyllia.fidorial.server.command.CommandModule;
import fr.euphyllia.fidorial.server.configuration.ServerConfiguration;
import fr.euphyllia.fidorial.server.configuration.exception.InvalidConfigurationException;
import fr.euphyllia.fidorial.server.debug.DebugModule;
import fr.euphyllia.fidorial.server.entity.EntityModule;
import fr.euphyllia.fidorial.server.entity.player.PlayerList;
import fr.euphyllia.fidorial.server.entity.player.PlayerModule;
import fr.euphyllia.fidorial.server.events.SimpleEventBus;
import fr.euphyllia.fidorial.server.lifecycle.ModuleManager;
import fr.euphyllia.fidorial.server.lifecycle.Quietly;
import fr.euphyllia.fidorial.server.lifecycle.ServerState;
import fr.euphyllia.fidorial.server.map.MapModule;
import fr.euphyllia.fidorial.server.metrics.TelemetryModule;
import fr.euphyllia.fidorial.server.moderation.ModerationModule;
import fr.euphyllia.fidorial.server.network.ClientConnection;
import fr.euphyllia.fidorial.server.network.NetworkModule;
import fr.euphyllia.fidorial.server.plugin.PluginModule;
import fr.euphyllia.fidorial.server.registry.RegistryModule;
import fr.euphyllia.fidorial.server.schedulers.WorkerModule;
import fr.euphyllia.fidorial.server.service.SimpleServiceRegistry;
import fr.euphyllia.fidorial.server.status.FidorialServerListing;
import fr.euphyllia.fidorial.server.world.WorldModule;
import fr.fidorial.Server;
import fr.fidorial.event.EventBus;
import fr.fidorial.event.server.ServerStartedEvent;
import fr.fidorial.event.server.ServerStoppingEvent;
import fr.fidorial.scheduler.RegionizedScheduler;
import fr.fidorial.service.ServiceRegistry;
import fr.fidorial.translation.TranslationStore;
import fr.fidorial.version.BuildSpecification;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import static fr.euphyllia.fidorial.server.VersionConstants.DATA_VERSION;

public final class FidorialServer implements Server {

    public static final ComponentLogger LOGGER = ComponentLogger.logger(FidorialServer.class);

    private static final AtomicReference<@Nullable FidorialServer> INSTANCE = new AtomicReference<>();

    private final AtomicReference<ServerState> state = new AtomicReference<>(ServerState.CREATED);
    private final boolean headless;
    private final ServerConfiguration config;
    private final FidorialServerListing listing;
    private final SimpleEventBus events = new SimpleEventBus();
    private final ServiceRegistry services = new SimpleServiceRegistry();
    private final ModuleManager modules = new ModuleManager(services, this);

    private final TelemetryModule telemetry;
    private final RegistryModule registries;
    private final WorkerModule workers;
    private final ModerationModule moderation;
    private final PlayerModule players;
    private final EntityModule entities;
    private final DebugModule debug;
    private final WorldModule worlds;
    private final MapModule maps;
    private final CommandModule commands;
    private final NetworkModule network;
    private final PluginModule plugins;

    private volatile @Nullable Iterable<? extends Audience> audiences;

    public FidorialServer() throws IOException {
        this(false);
    }

    /**
     * Builds every module
     *
     * @param headless {@code true} to run without network nor console (tests)
     */
    public FidorialServer(final boolean headless) throws IOException {
        if (INSTANCE.get() != null) {
            throw new IllegalStateException("FidorialServer is already initialized");
        }

        final BuildSpecification build = VersionConstants.BUILD;
        LOGGER.info("Starting Fidorial {} for Minecraft {} ({})", build.apiVersion(), build.minecraftVersion().name(),
                build.buildNumber().isPresent() ? "build " + build.buildNumber().getAsInt() : "local");
        LOGGER.debug("Version {}, protocol {}, data version {}", build.version(), build.protocolVersion(), DATA_VERSION);

        GameBootstrap.bootstrap();
        this.headless = headless;
        this.config = ServerConfiguration.load();
        this.listing = new FidorialServerListing(config);

        try {
            telemetry = modules.add(new TelemetryModule(this));
            registries = modules.add(new RegistryModule(config.worlds().path().resolve("datapacks")));
            workers = modules.add(new WorkerModule(config));
            moderation = modules.add(new ModerationModule(config));
            players = modules.add(new PlayerModule(this, config, services, moderation));
            entities = modules.add(new EntityModule(this, config, workers, players));
            debug = modules.add(new DebugModule(workers, players, entities));
            worlds = modules.add(new WorldModule(config, events, services, registries, workers, players, entities, debug));
            maps = modules.add(new MapModule(this));
            commands = modules.add(new CommandModule(this, headless, registries, services));
            network = modules.add(new NetworkModule(this, config, headless, registries));
            plugins = modules.add(new PluginModule(this, config, events, services, registries));
        } catch (final Throwable t) {
            modules.shutdown(); // releases the threads of the modules already built
            throw t;
        }

        players.connections().addListener(new PlayerList.Listener() {
            @Override
            public void onConnect(final ClientConnection connection) {
                audiences = null;
            }

            @Override
            public void onDisconnect(final ClientConnection connection) {
                audiences = null;
            }
        });

        if (!INSTANCE.compareAndSet(null, this)) {
            modules.shutdown();
            throw new IllegalStateException("FidorialServer is already initialized");
        }
    }

    /**
     * {@return the server}
     *
     * @throws IllegalStateException if no server is fully constructed yet
     */
    public static FidorialServer getInstance() {
        final FidorialServer server = INSTANCE.get();
        if (server == null) {
            throw new IllegalStateException("FidorialServer is not initialized yet. Do not call getInstance() from a "
                    + "constructor or a static initializer: resolve the server when it is actually used.");
        }
        return server;
    }

    public static boolean isInitialized() {
        return INSTANCE.get() != null;
    }

    public void start() throws Exception {
        if (!state.compareAndSet(ServerState.CREATED, ServerState.STARTING)) {
            if (state.get().isActive()) {
                return;
            }
            throw new IllegalStateException("A server that has been shut down cannot be started again");
        }
        try {
            modules.load();
            modules.start();
            if (!headless) {
                modules.startAutoSave(config.general().autoSaveSeconds());
            }
            if (!state.compareAndSet(ServerState.STARTING, ServerState.RUNNING)) {
                LOGGER.warn("The server was stopped while it was starting");
                return;
            }
            events.post(new ServerStartedEvent(this));
            modules.started();
        } catch (final InvalidConfigurationException e) {
            shutdown();
            throw e;
        } catch (final Exception e) {
            LOGGER.error("Startup interrupted, shutting down", e);
            shutdown();
            throw e;
        }
    }

    @Override
    public void shutdown() {
        final ServerState previous = state.getAndUpdate(s -> s == ServerState.STOPPED ? s : ServerState.STOPPING);
        if (previous == ServerState.STOPPING || previous == ServerState.STOPPED) {
            return;
        }

        LOGGER.info("Stopping the Fidorial server...");
        if (previous != ServerState.CREATED) {
            Quietly.run("stopping event", () -> events.post(new ServerStoppingEvent(this)));
            Quietly.run("kicking players", () -> players.online()
                    .forEach(player -> player.kick(Component.translatable("commands.stop.stopping"))));
            Quietly.run("registries", () -> registries.setStarted(false));
        }
        modules.shutdown();
        state.set(ServerState.STOPPED);
        LOGGER.debug("Fidorial shut down correctly.");
    }

    public ServerState state() {
        return state.get();
    }

    @Override
    public boolean isRunning() {
        return state.get().isActive();
    }

    public boolean isHeadless() {
        return headless;
    }

    public ServerConfiguration config() {
        return config;
    }

    public ModuleManager modules() {
        return modules;
    }

    @Override
    public FidorialServerListing listing() {
        return listing;
    }

    @Override
    public PlayerModule players() {
        return players;
    }

    @Override
    public WorldModule worlds() {
        return worlds;
    }

    @Override
    public MapModule maps() {
        return maps;
    }

    @Override
    public RegistryModule registries() {
        return registries;
    }

    @Override
    public CommandModule commands() {
        return commands;
    }

    @Override
    public PluginModule plugins() {
        return plugins;
    }

    @Override
    public RegionizedScheduler scheduler() {
        return workers.regionizer();
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
    public TranslationStore translationStore() {
        return TranslationStore.current();
    }

    public WorkerModule workers() {
        return workers;
    }

    public ModerationModule moderation() {
        return moderation;
    }

    public EntityModule entities() {
        return entities;
    }

    public DebugModule debug() {
        return debug;
    }

    public NetworkModule network() {
        return network;
    }

    public TelemetryModule telemetry() {
        return telemetry;
    }

    @Override
    public String brandName() {
        return "Fidorial";
    }

    @Override
    public BuildSpecification buildSpecification() {
        return VersionConstants.BUILD;
    }

    @Override
    public Iterable<? extends Audience> audiences() {
        Iterable<? extends Audience> cached = this.audiences;
        if (cached == null) {
            cached = Iterables.concat(Collections.singleton(commands.console()), players.online());
            this.audiences = cached;
        }
        return cached;
    }
}
