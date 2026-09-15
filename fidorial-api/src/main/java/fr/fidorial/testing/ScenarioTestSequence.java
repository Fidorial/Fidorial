package fr.fidorial.testing;

import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.BooleanSupplier;

/**
 * A sequence of steps executed one tick at a time.
 * Built with {@link Builder} obtained from {@link ScenarioTestHelper#sequence()}.
 */
public final class ScenarioTestSequence {

    private record Step(Runnable action, boolean retryable) {
    }

    private final Deque<Step> steps;
    private @Nullable ScenarioAssertionException lastFailure;

    private ScenarioTestSequence(final Deque<Step> steps) {
        this.steps = steps;
    }

    boolean tick() {
        while (!steps.isEmpty()) {
            final Step step = steps.peekFirst();
            try {
                step.action().run();
            } catch (final ScenarioAssertionException e) {
                if (step.retryable()) {
                    lastFailure = e;
                    return false; // retry next tick
                }
                throw e;
            }
            steps.pollFirst();
        }
        return true;
    }

    @Nullable ScenarioAssertionException lastFailure() {
        return lastFailure;
    }

    /**
     * Gathers steps for a {@link ScenarioTestSequence}.
     * <p>
     * Steps only start ticking once {@link #build()} is called.
     */
    public static final class Builder {

        private final ScenarioTestInfo info;
        private final Deque<Step> steps = new ArrayDeque<>();
        private boolean built;

        Builder(final ScenarioTestInfo info) {
            this.info = info;
        }

        /**
         * Runs the {@code action} once, then moves to the next step.
         */
        public Builder execute(final Runnable action) {
            checkNotBuilt();
            steps.addLast(new Step(action, false));
            return this;
        }

        /**
         * Retries the {@code assertion} every tick until it stops throwing {@link ScenarioAssertionException}.
         */
        public Builder waitUntil(final Runnable assertion) {
            checkNotBuilt();
            steps.addLast(new Step(assertion, true));
            return this;
        }

        /**
         * Retries {@code condition} every tick until it returns {@code true}, throwing
         * {@link ScenarioAssertionException} with {@code messageIfFalse} on each failed attempt.
         */
        public Builder waitUntil(final BooleanSupplier condition, final String messageIfFalse) {
            checkNotBuilt();
            steps.addLast(new Step(() -> {
                if (!condition.getAsBoolean()) {
                    throw new ScenarioAssertionException(messageIfFalse, info.tick());
                }
            }, true));
            return this;
        }

        /**
         * Builds and returns the {@link ScenarioTestSequence}.
         */
        public ScenarioTestSequence build() {
            checkNotBuilt();
            built = true;
            final ScenarioTestSequence sequence = new ScenarioTestSequence(steps);
            info.attachSequence(sequence);
            return sequence;
        }

        private void checkNotBuilt() {
            if (built) {
                throw new IllegalStateException("Sequence already built");
            }
        }
    }
}
