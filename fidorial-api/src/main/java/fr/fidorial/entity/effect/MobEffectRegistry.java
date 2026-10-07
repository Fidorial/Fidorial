package fr.fidorial.entity.effect;

import fr.fidorial.Server;
import fr.fidorial.plugin.Plugin;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.MobEffect;

import java.util.Collection;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

/**
 * Where effects are declared: the vanilla ones, the ones plugins give behaviour to, and the ones they
 * add. It also creates the {@link MobEffectInstance}s to apply.
 *
 * @see Server#effects()
 * @since 0.1.0
 */
public interface MobEffectRegistry {

    /**
     * {@return a builder of a visible level I effect with its icon, lasting one second until changed}
     *
     * @param type a vanilla or registered effect, such as {@code MobEffectKeys.SPEED}
     * @since 0.1.0
     */
    MobEffectInstance.Builder builder(TypedKey<MobEffect> type);

    /**
     * {@return a visible effect with its icon, as applied by potions and mobs}
     *
     * @param type      a vanilla or registered effect
     * @param duration  the duration in ticks, or {@link MobEffectInstance#INFINITE_DURATION}
     * @param amplifier the level minus one
     * @since 0.1.0
     */
    default MobEffectInstance instance(final TypedKey<MobEffect> type, final int duration, final int amplifier) {
        return builder(type).duration(duration).amplifier(amplifier).build();
    }

    /**
     * {@return a builder of a new effect, to {@link #register(MobEffectDefinition, Plugin) register}}
     *
     * @param key the key of the new effect, outside the {@code minecraft} namespace
     * @since 0.1.0
     */
    MobEffectDefinition.Builder define(TypedKey<MobEffect> key);

    /**
     * Adds an effect to the game.
     *
     * @param definition the effect
     * @param owner      the plugin adding it
     * @throws IllegalArgumentException if the key is a vanilla effect or already registered, or the
     *                                  requested network ID is taken
     * @since 0.1.0
     */
    void register(MobEffectDefinition definition, Plugin owner);

    /**
     * Gives a behaviour to an effect, vanilla or registered, on top of the ones it already has.
     *
     * @param type      the effect
     * @param behaviour what it does
     * @param owner     the plugin attaching it
     * @since 0.1.0
     */
    void attach(TypedKey<MobEffect> type, MobEffectBehaviour behaviour, Plugin owner);

    /**
     * Drops an effect added through {@link #register(MobEffectDefinition, Plugin)}. Entities still
     * running it lose it on their next tick.
     *
     * @param type the effect
     * @return {@code true} if it was registered
     * @since 0.1.0
     */
    boolean unregister(TypedKey<MobEffect> type);

    /**
     * Drops the behaviours one plugin attached to an effect.
     *
     * @param type  the effect
     * @param owner the plugin that attached them
     * @return {@code true} if at least one behaviour was dropped
     * @since 0.1.0
     */
    boolean detach(TypedKey<MobEffect> type, Plugin owner);

    /**
     * Drops every effect and behaviour a plugin declared.
     *
     * @param owner the plugin to clean up after
     * @since 0.1.0
     */
    void unregisterAll(Plugin owner);

    /**
     * {@return the definition registered under that key, empty for a vanilla effect}
     *
     * @param type the effect
     * @since 0.1.0
     */
    Optional<MobEffectDefinition> definition(TypedKey<MobEffect> type);

    /**
     * {@return every effect registered by a plugin}
     *
     * @since 0.1.0
     */
    Collection<MobEffectDefinition> definitions();

    /**
     * {@return every effect that can be applied, vanilla ones included}
     *
     * @since 0.1.0
     */
    Set<TypedKey<MobEffect>> types();

    /**
     * {@return {@code true} if the effect is vanilla or registered}
     *
     * @param type the effect
     * @since 0.1.0
     */
    boolean isEffect(TypedKey<MobEffect> type);

    /**
     * {@return the network ID of the effect: the vanilla one, or the one a registered effect was given;
     * empty for an unknown effect}
     *
     * @param type the effect
     * @since 0.1.0
     */
    OptionalInt networkId(TypedKey<MobEffect> type);
}
