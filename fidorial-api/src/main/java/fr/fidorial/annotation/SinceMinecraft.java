package fr.fidorial.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The Minecraft version that introduced the game feature an API element maps to, as opposed to
 * {@code @since}, which gives the Fidorial version that added the element.
 *
 * <p>The annotation is {@link Documented}, so it shows up in the Javadoc of the element, and is kept
 * in the class files for tools that want to read it.</p>
 *
 * @since 0.1.0
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({
        ElementType.TYPE,
        ElementType.FIELD,
        ElementType.METHOD,
        ElementType.CONSTRUCTOR,
        ElementType.RECORD_COMPONENT,
        ElementType.PACKAGE,
        ElementType.MODULE
})
public @interface SinceMinecraft {

    /**
     * {@return the Minecraft version, for instance {@code "1.21.6"} or {@code "26.4"}}
     *
     * @since 0.1.0
     */
    String value();
}
