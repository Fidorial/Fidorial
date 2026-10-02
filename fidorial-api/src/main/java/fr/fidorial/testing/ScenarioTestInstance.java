package fr.fidorial.testing;

import fr.fidorial.testing.annotation.ScenarioTest;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.Method;

/**
 * A test method annotated with {@link ScenarioTest}, as discovered by the runner.
 *
 * @since 0.1.0
 */
@ApiStatus.Internal
public final class ScenarioTestInstance {

    private final Method method;
    private final String group;
    private final int timeoutTicks;
    private final boolean required;

    /**
     * Wraps a test method.
     *
     * @param method       the static method taking a {@link ScenarioTestHelper}
     * @param group        the group the test belongs to
     * @param timeoutTicks the number of ticks after which the test fails
     * @param required     whether a failure fails the whole run
     * @since 0.1.0
     */
    public ScenarioTestInstance(final Method method, final String group, final int timeoutTicks, final boolean required) {
        this.method = method;
        this.group = group;
        this.timeoutTicks = timeoutTicks;
        this.required = required;
    }

    /**
     * {@return the identifier of the test, {@code <class>/<method>}}
     *
     * @since 0.1.0
     */
    public String id() {
        return method.getDeclaringClass().getSimpleName() + "/" + method.getName();
    }

    /**
     * {@return the group the test belongs to}
     *
     * @since 0.1.0
     */
    public String group() {
        return group;
    }

    /**
     * {@return the number of ticks after which the test fails}
     *
     * @since 0.1.0
     */
    public int timeoutTicks() {
        return timeoutTicks;
    }

    /**
     * {@return {@code true} if a failure fails the whole run}
     *
     * @since 0.1.0
     */
    public boolean required() {
        return required;
    }

    void invoke(final ScenarioTestHelper helper) throws ReflectiveOperationException {
        method.invoke(null, helper);
    }
}
