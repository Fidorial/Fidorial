package fr.euphyllia.fidorial.server.schedulers.tick;

/**
 * A change to tick settings, applied by each ticker on its own thread.
 */
public sealed interface TickChange {

    /**
     * {@return the defaults new tickers start from once this change is made}
     */
    TickSettings applyTo(TickSettings settings);

    record SetFrozen(boolean frozen) implements TickChange {
        @Override
        public TickSettings applyTo(final TickSettings settings) {
            return new TickSettings(settings.rate(), frozen);
        }
    }

    record SetRate(float rate) implements TickChange {
        @Override
        public TickSettings applyTo(final TickSettings settings) {
            return new TickSettings(rate, settings.frozen());
        }
    }

    /**
     * Runs {@code ticks} game ticks while frozen, replacing any step in progress.
     */
    record Step(int ticks) implements TickChange {
        @Override
        public TickSettings applyTo(final TickSettings settings) {
            return settings;
        }
    }

    record StopStep() implements TickChange {
        @Override
        public TickSettings applyTo(final TickSettings settings) {
            return settings;
        }
    }
}
