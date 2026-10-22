package fr.euphyllia.fidorial.server.lifecycle;

/**
 * A slice of the server dedicated to one domain (worlds, players, network...).
 */
public interface ServerModule {

    /**
     * {@return the name shown in logs}
     */
    default String name() {
        return getClass().getSimpleName();
    }

    /**
     * Registers the default implementations of the API services this module provides. Plugins may
     * replace them with a higher priority.
     *
     * @param services where to register
     */
    default void registerServices(final ServiceRegistrar services) {
    }

    /**
     * Reads persisted data. Called before plugins are loaded.
     *
     * @throws Exception to abort the startup
     */
    default void load() throws Exception {
    }

    /**
     * Starts the module. Called after plugins are loaded, before they are enabled.
     *
     * @throws Exception to abort the startup
     */
    default void start() throws Exception {
    }

    /**
     * Called once the server is fully started: every module started, plugins enabled and
     * {@code ServerStartedEvent} posted.
     */
    default void onStarted() {
    }

    /**
     * Saves what changed since the last save. Called periodically from the auto-save thread
     * (never in headless mode).
     *
     * @throws Exception logged, the next modules are still saved
     */
    default void autoSave() throws Exception {
    }

    /**
     * Stops every activity: threads, tickers, listeners. Called in reverse module order.
     *
     * @throws Exception logged, the next modules are still stopped
     */
    default void stop() throws Exception {
    }

    /**
     * Saves to disk and releases resources. Called in reverse module order, once every module is
     * stopped.
     *
     * @throws Exception logged, the next modules are still closed
     */
    default void close() throws Exception {
    }
}
