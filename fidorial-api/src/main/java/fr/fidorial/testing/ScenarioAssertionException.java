package fr.fidorial.testing;

import org.jetbrains.annotations.ApiStatus;

/**
 * Thrown when a scenario test fails, carrying the tick it failed at.
 */
@ApiStatus.Internal
final class ScenarioAssertionException extends RuntimeException {

    private final int tick;

    ScenarioAssertionException(final String message, final int tick) {
        super(message);
        this.tick = tick;
    }

    int tick() {
        return tick;
    }
}
