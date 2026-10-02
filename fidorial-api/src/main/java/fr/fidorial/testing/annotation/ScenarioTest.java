package fr.fidorial.testing.annotation;

import fr.fidorial.testing.ScenarioTestHelper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a {@code public static void} method taking a {@link ScenarioTestHelper}
 * as a scenario test, run against a real server by the test harness.
 *
 * @since 0.1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ScenarioTest {

    /**
     * {@return the group the test belongs to, to run tests selectively}
     *
     * @since 0.1.0
     */
    String group() default "fidorial.builtin";

    /**
     * {@return the number of ticks after which the test fails}
     *
     * @since 0.1.0
     */
    int timeoutTicks() default 200;

    /**
     * {@return {@code true} if a failure of this test fails the whole run}
     *
     * @since 0.1.0
     */
    boolean required() default true;
}
