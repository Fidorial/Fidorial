package fr.fidorial.world.block;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares which block types a trait interface of {@link fr.fidorial.world.block.data} applies to, from the
 * properties they declare.
 *
 * <p>A block type implements a trait when it declares every property of {@link #properties()}, the values of each
 * property satisfy {@link #values()}, and it also satisfies the {@code BlockTrait} of every trait the interface
 * extends. For instance {@code Stairs extends Directional, Bisected, Waterlogged} only asks for {@code shape}: the
 * {@code facing}, {@code half} and {@code waterlogged} requirements come from its parents.</p>
 *
 * <p>Detection never looks at the block key, so a new block sharing the properties of a trait gets it for free.
 * The traits of the API are indexed at compile time by an annotation processor, so a new trait is one annotated
 * public interface extending {@link BlockData}, with no list to update. Plugins compile without that processor:
 * their own traits are added to their blocks with {@link BlockType.Builder#trait(Class)}.</p>
 *
 * @since 0.1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface BlockTrait {

    /**
     * {@return the names of the properties a block type must declare; empty when only the parent traits matter}
     *
     * @since 0.1.0
     */
    String[] properties() default {};

    /**
     * {@return restrictions on the values of the declared properties}
     *
     * @since 0.1.0
     */
    Values[] values() default {};

    /**
     * Restricts the values a property may declare.
     *
     * @since 0.1.0
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({})
    @interface Values {

        /**
         * {@return the name of the restricted property, which is then required}
         *
         * @since 0.1.0
         */
        String property();

        /**
         * {@return the accepted values}
         *
         * @since 0.1.0
         */
        String[] allowed();

        /**
         * {@return {@code true} if the property must declare exactly the {@linkplain #allowed() accepted values},
         * {@code false} if it may declare any of them}
         *
         * @since 0.1.0
         */
        boolean exactly() default false;
    }
}
