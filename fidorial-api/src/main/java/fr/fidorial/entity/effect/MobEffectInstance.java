package fr.fidorial.entity.effect;

import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.MobEffect;
import org.jetbrains.annotations.ApiStatus;

/**
 * A status effect running on a living entity, such as {@code minecraft:speed} for ten seconds.
 *
 * <p>Instances are immutable and created through the {@link MobEffectRegistry} of the server.</p>
 *
 * @since 0.1.0
 */
@ApiStatus.NonExtendable
public interface MobEffectInstance {

    /**
     * Duration of an effect that never runs out.
     *
     * @since 0.1.0
     */
    int INFINITE_DURATION = -1;

    /**
     * Highest amplifier the client can display.
     *
     * @since 0.1.0
     */
    int MAX_AMPLIFIER = 255;

    /**
     * {@return the effect, a {@code minecraft:mob_effect} entry}
     *
     * @since 0.1.0
     */
    TypedKey<MobEffect> type();

    /**
     * {@return the remaining ticks, or {@link #INFINITE_DURATION}}
     *
     * @since 0.1.0
     */
    int duration();

    /**
     * {@return the level minus one, from {@code 0} to {@link #MAX_AMPLIFIER}}
     *
     * @since 0.1.0
     */
    int amplifier();

    /**
     * {@return {@code true} for effects coming from a beacon or a conduit, drawn with fainter particles}
     *
     * @since 0.1.0
     */
    boolean ambient();

    /**
     * {@return {@code true} when the swirling particles are shown}
     *
     * @since 0.1.0
     */
    boolean visible();

    /**
     * {@return {@code true} when the icon is shown in the HUD of a player}
     *
     * @since 0.1.0
     */
    boolean showIcon();

    /**
     * {@return {@code true} if this effect never runs out}
     *
     * @since 0.1.0
     */
    boolean isInfinite();

    /**
     * {@return a builder starting from this effect, to derive a changed copy}
     *
     * @since 0.1.0
     */
    Builder toBuilder();

    /**
     * Builds a {@link MobEffectInstance}.
     *
     * @since 0.1.0
     */
    @ApiStatus.NonExtendable
    interface Builder {

        /**
         * @param duration the duration in ticks, or {@link #INFINITE_DURATION}
         * @return this builder
         * @throws IllegalArgumentException if the duration is negative and not {@link #INFINITE_DURATION}
         * @since 0.1.0
         */
        Builder duration(int duration);

        /**
         * @param amplifier the level minus one, from {@code 0} to {@link #MAX_AMPLIFIER}
         * @return this builder
         * @throws IllegalArgumentException if the amplifier is out of range
         * @since 0.1.0
         */
        Builder amplifier(int amplifier);

        /**
         * @param ambient {@code true} for fainter particles, as for beacon effects
         * @return this builder
         * @since 0.1.0
         */
        Builder ambient(boolean ambient);

        /**
         * @param visible {@code false} to hide the swirling particles
         * @return this builder
         * @since 0.1.0
         */
        Builder visible(boolean visible);

        /**
         * @param showIcon {@code false} to hide the icon from the HUD of a player
         * @return this builder
         * @since 0.1.0
         */
        Builder showIcon(boolean showIcon);

        /**
         * {@return the effect}
         *
         * @since 0.1.0
         */
        MobEffectInstance build();
    }
}
