package fr.euphyllia.fidorial.server.lifecycle;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

/**
 * Runs a step that must not prevent the next ones from running if it fails (shutdown steps,
 * mostly). The failure is logged.
 */
public final class Quietly {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(Quietly.class);

    private Quietly() {
        throw new UnsupportedOperationException("Quietly cannot be instantiated.");
    }

    /**
     * @param what   a human-readable name of the step, used in logs
     * @param action the step
     */
    public static void run(final String what, final Action action) {
        try {
            action.run();
        } catch (final Throwable t) {
            LOGGER.error("Error while running '{}'", what, t);
        }
    }

    @FunctionalInterface
    public interface Action {
        void run() throws Exception;
    }
}
