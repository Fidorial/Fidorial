package fr.fidorial.testing;

import fr.fidorial.world.World;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

@ApiStatus.Internal
public final class ScenarioTestInfo {

    void sequenceBuilderCreated() {
        sequenceBuilderCreated = true;
    }

    public enum State {
        RUNNING,
        PASSED,
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

    public ScenarioTestInfo(final ScenarioTestInstance instance, final World world, final ScenarioTestPlayerFactory playerFactory) {
        this.instance = instance;
        this.helper = new ScenarioTestHelper(world, playerFactory, this);
    }

    public int tick() {
        return tick;
    }

    public State state() {
        return state;
    }

    public ScenarioTestInstance instance() {
        return instance;
    }

    @Nullable public Throwable error() {
        return error;
    }

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
