package fr.fidorial.entity.effect;

import fr.fidorial.entity.Player;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.MobEffect;
import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Predicate;

/**
 * An effect a plugin adds to the game, created by {@link MobEffectRegistry#define(TypedKey)}.
 *
 * <p>{@code minecraft:mob_effect} is baked into the client: a vanilla client only knows the vanilla
 * effects, by their network ID, and drops the connection on an ID it does not know. So a new effect is
 * shown to a player in one of three ways:</p>
 * <ul>
 *     <li>its own ID, to the players whose (modded) client {@linkplain #isKnownBy knows it};</li>
 *     <li>else the vanilla effect it {@linkplain #fallback falls back to}, so the icon still shows;</li>
 *     <li>else not at all: the effect only exists on the server.</li>
 * </ul>
 *
 * @since 0.1.0
 */
@ApiStatus.NonExtendable
public interface MobEffectDefinition {

    /**
     * {@return the key of the effect}
     *
     * @since 0.1.0
     */
    TypedKey<MobEffect> key();

    /**
     * {@return whether the effect helps or harms}
     *
     * @since 0.1.0
     */
    Category category();

    /**
     * {@return the vanilla effect shown to players whose client does not know this one, empty to show
     * nothing}
     *
     * @since 0.1.0
     */
    Optional<TypedKey<MobEffect>> fallback();

    /**
     * {@return {@code true} if the client of this player knows the effect, and receives its own ID}
     *
     * @param player the player about to be told about the effect
     * @since 0.1.0
     */
    boolean isKnownBy(Player player);

    /**
     * {@return the network ID asked for, empty to take the next free ID after the vanilla effects}
     *
     * @since 0.1.0
     */
    OptionalInt requestedNetworkId();

    /**
     * {@return what the effect does}
     *
     * @since 0.1.0
     */
    MobEffectBehaviour behaviour();

    /**
     * Whether an effect helps or harms, as vanilla sorts them.
     *
     * @since 0.1.0
     */
    enum Category {
        /**
         * The effect helps, like Speed.
         */
        BENEFICIAL,
        /**
         * The effect harms, like Poison.
         */
        HARMFUL,
        /**
         * Neither, like Glowing.
         */
        NEUTRAL
    }

    /**
     * Builds a {@link MobEffectDefinition}.
     *
     * @since 0.1.0
     */
    @ApiStatus.NonExtendable
    interface Builder {

        /**
         * @param category whether the effect helps or harms; {@link Category#NEUTRAL} by default
         * @return this builder
         * @since 0.1.0
         */
        Builder category(Category category);

        /**
         * @param fallback the vanilla effect shown to clients that do not know this one
         * @return this builder
         * @throws IllegalArgumentException if {@code fallback} is not a vanilla effect
         * @since 0.1.0
         */
        Builder fallback(TypedKey<MobEffect> fallback);

        /**
         * @param knownBy picks the players whose client knows this effect; nobody by default
         * @return this builder
         * @since 0.1.0
         */
        Builder knownBy(Predicate<Player> knownBy);

        /**
         * Asks for a fixed network ID, to match the one the client mod gives the effect.
         *
         * @param networkId the network ID, after the vanilla effects
         * @return this builder
         * @since 0.1.0
         */
        Builder networkId(int networkId);

        /**
         * @param behaviour what the effect does; nothing by default
         * @return this builder
         * @since 0.1.0
         */
        Builder behaviour(MobEffectBehaviour behaviour);

        /**
         * {@return the definition, to hand to {@link MobEffectRegistry#register}}
         *
         * @since 0.1.0
         */
        MobEffectDefinition build();
    }
}
