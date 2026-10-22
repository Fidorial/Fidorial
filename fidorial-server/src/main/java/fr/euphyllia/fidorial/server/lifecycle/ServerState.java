package fr.euphyllia.fidorial.server.lifecycle;

/**
 * The stages a {@code FidorialServer} goes through. Transitions only move forward:
 * {@code CREATED -> STARTING -> RUNNING -> STOPPING -> STOPPED}, and a server that was never
 * started may go straight from {@code CREATED} to {@code STOPPING}.
 */
public enum ServerState {
    /**
     * Every component is built, nothing has been started yet (no world loaded, no plugin loaded,
     * no port bound).
     */
    CREATED,
    /**
     * {@code start()} is running: data, plugins and worlds are being loaded.
     */
    STARTING,
    /**
     * Fully started and accepting players.
     */
    RUNNING,
    /**
     * {@code shutdown()} is running.
     */
    STOPPING,
    /**
     * Every subsystem has been closed. The server cannot be restarted.
     */
    STOPPED;

    /**
     * {@return whether the server is starting or running, i.e. not yet asked to stop}
     */
    public boolean isActive() {
        return this == STARTING || this == RUNNING;
    }
}
