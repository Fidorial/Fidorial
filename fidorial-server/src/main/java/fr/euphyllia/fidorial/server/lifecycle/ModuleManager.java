package fr.euphyllia.fidorial.server.lifecycle;

import fr.fidorial.service.ServicePriority;
import fr.fidorial.service.ServiceRegistry;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Runs the {@linkplain ServerModule hooks} of every module, in the order the modules were added.
 */
public final class ModuleManager {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(ModuleManager.class);

    private final List<ServerModule> modules = new ArrayList<>();
    private final ServiceRegistrar registrar;
    private @Nullable ScheduledExecutorService autoSave;
    private boolean shutDown;

    /**
     * @param services     where modules register their default services
     * @param serviceOwner the owner of those registrations
     */
    public ModuleManager(final ServiceRegistry services, final Object serviceOwner) {
        this.registrar = new ServiceRegistrar() {
            @Override
            public <T> void provide(final Class<T> service, final T implementation) {
                services.register(service, implementation, serviceOwner, ServicePriority.LOWEST);
            }
        };
    }

    /**
     * Adds a module. Its position defines when its hooks run.
     *
     * @param module the module
     * @param <M>    the module type
     * @return {@code module}
     */
    public synchronized <M extends ServerModule> M add(final M module) {
        if (shutDown) {
            throw new IllegalStateException("Cannot add " + module.name() + ": the modules are shut down");
        }
        modules.add(module);
        return module;
    }

    /**
     * {@return the modules, in order}
     */
    public synchronized List<ServerModule> modules() {
        return List.copyOf(modules);
    }

    /**
     * Registers the default services of every module, then loads them.
     *
     * @throws Exception if a module fails to load
     */
    public void load() throws Exception {
        for (final ServerModule module : modules()) {
            module.registerServices(registrar);
        }
        for (final ServerModule module : modules()) {
            LOGGER.debug("Loading {}", module.name());
            module.load();
        }
    }

    /**
     * Starts every module.
     *
     * @throws Exception if a module fails to start
     */
    public void start() throws Exception {
        for (final ServerModule module : modules()) {
            LOGGER.debug("Starting {}", module.name());
            module.start();
        }
    }

    /**
     * Tells every module that the server is fully started.
     */
    public void started() {
        for (final ServerModule module : modules()) {
            Quietly.run(module.name() + " started", module::onStarted);
        }
    }

    /**
     * Calls {@link ServerModule#autoSave()} on every module, periodically.
     *
     * @param intervalSeconds the delay between two saves
     */
    public synchronized void startAutoSave(final int intervalSeconds) {
        if (autoSave != null || shutDown) {
            return;
        }
        final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(
                r -> Thread.ofPlatform().name("fidorial-autosave").unstarted(r));
        executor.scheduleAtFixedRate(this::autoSave, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
        this.autoSave = executor;
    }

    private void autoSave() {
        for (final ServerModule module : modules()) {
            Quietly.run(module.name() + " auto-save", module::autoSave);
        }
    }

    /**
     * Stops then closes every module, in reverse order. Calling this again does nothing.
     */
    public void shutdown() {
        final List<ServerModule> reversed;
        final @Nullable ScheduledExecutorService saver;
        synchronized (this) {
            if (shutDown) {
                return;
            }
            shutDown = true;
            reversed = List.copyOf(modules.reversed());
            saver = autoSave;
        }
        if (saver != null) {
            // wait for a save in progress, so that it does not race with close()
            Quietly.run("auto-save", () -> {
                saver.shutdownNow();
                saver.awaitTermination(10, TimeUnit.SECONDS);
            });
        }
        for (final ServerModule module : reversed) {
            LOGGER.debug("Stopping {}", module.name());
            Quietly.run(module.name() + " stop", module::stop);
        }
        for (final ServerModule module : reversed) {
            LOGGER.debug("Closing {}", module.name());
            Quietly.run(module.name() + " close", module::close);
        }
    }
}
