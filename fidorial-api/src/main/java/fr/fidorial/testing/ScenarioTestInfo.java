package fr.fidorial.testing;

import fr.fidorial.world.World;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/**
 * The running state of one {@link ScenarioTestInstance}, ticked by the server test runner.
 *
 * @since 0.1.0
 */
@ApiStatus.Internal
public final class ScenarioTestInfo {

    void sequenceBuilderCreated() {
        sequenceBuilderCreated = true;
    }

    /**
     * The outcome of a test run.
     *
     * @since 0.1.0
     */
    public enum State {
        /**
         * The test is still running.
         */
        RUNNING,
        /**
         * The test completed without failure.
         */
        PASSED,
        /**
         * The test threw, failed an assertion or timed out.
         */
        FAILED
    }

    private final ScenarioTestInstance instance;
    private final ScenarioTestHelper helper;
    private int tick;
    private State state = State.RUNNING;
    private @Nullable Throwable error;
    private boolean invoked;
    private @Nullable ScenarioTestSequence sequence;
    private boolean sequenceBuilderCreated;

    /**
     * Prepares a test run.
     *
     * @param instance      the test to run
     * @param world         the world the test runs in
     * @param playerFactory the factory of the mock players the test summons
     * @since 0.1.0
     */
    public ScenarioTestInfo(final ScenarioTestInstance instance, final World world, final ScenarioTestPlayerFactory playerFactory) {
        this.instance = instance;
        this.helper = new ScenarioTestHelper(world, playerFactory, this);
    }

    /**
     * {@return the tick the test last ran at}
     *
     * @since 0.1.0
     */
    public int tick() {
        return tick;
    }

    /**
     * {@return the current state of the test}
     *
     * @since 0.1.0
     */
    public State state() {
        return state;
    }

    /**
     * {@return the test being run}
     *
     * @since 0.1.0
     */
    public ScenarioTestInstance instance() {
        return instance;
    }

    /**
     * {@return the failure of the test, or {@code null} unless it failed}
     *
     * @since 0.1.0
     */
    @Nullable public Throwable error() {
        return error;
    }

    /**
     * {@return {@code true} once the test passed or failed}
     *
     * @since 0.1.0
     */
    public boolean isDone() {
        return state != State.RUNNING;
    }

    void succeed() {
        if (state == State.RUNNING) {
            state = State.PASSED;
            helper.despawnPlayers();
        }
    }

    void attachSequence(final ScenarioTestSequence sequence) {
        this.sequence = sequence;
    }

    /**
     * Advances the test: invokes the test method on the first call, then ticks its sequence until it
     * completes, fails or times out.
     *
     * @param currentTick the number of ticks elapsed since the test started
     * @since 0.1.0
     */
    public void tick(final int currentTick) {
        if (state != State.RUNNING) {
            return;
        }
        this.tick = currentTick;
        try {
            if (!invoked) {
                invoked = true;
                instance.invoke(helper);
            }
            if (sequenceBuilderCreated && sequence == null) {
                throw new IllegalStateException("ScenarioTestSequence was created but never built. Did you forget to call .build()?");
            }
            if (sequence == null) {
                succeed();
                return;
            }
            if (sequence.tick()) {
                succeed();
            }
            if (state == State.RUNNING && currentTick >= instance.timeoutTicks()) {
                final ScenarioAssertionException timeout = new ScenarioAssertionException("Timed out after " + instance.timeoutTicks() + " ticks", currentTick);
                final Throwable lastFailure = sequence != null ? sequence.lastFailure() : null;
                if (lastFailure != null) {
                    timeout.initCause(lastFailure);
                }
                fail(timeout);
            }
        } catch (final ReflectiveOperationException e) {
            fail(e.getCause() != null ? e.getCause() : e);
        } catch (final Throwable t) {
            fail(t);
        }
    }

    private void fail(final Throwable t) {
        this.state = State.FAILED;
        this.error = t;
        helper.despawnPlayers();
    }
}
