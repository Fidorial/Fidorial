package fr.fidorial.registry.keys;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.ConsumeEffectType;
import java.util.List;
import java.util.stream.Stream;
import net.kyori.adventure.key.KeyPattern;

/**
 * Typed keys for entries in the {@code minecraft:consume_effect_type} registry.
 */
public final class ConsumeEffectTypeKeys {
    /**
     * Key for {@code minecraft:apply_effects}.
     */
    public static final TypedKey<ConsumeEffectType> APPLY_EFFECTS = create("apply_effects");

    /**
     * Key for {@code minecraft:clear_all_effects}.
     */
    public static final TypedKey<ConsumeEffectType> CLEAR_ALL_EFFECTS = create("clear_all_effects");

    /**
     * Key for {@code minecraft:play_sound}.
     */
    public static final TypedKey<ConsumeEffectType> PLAY_SOUND = create("play_sound");

    /**
     * Key for {@code minecraft:remove_effects}.
     */
    public static final TypedKey<ConsumeEffectType> REMOVE_EFFECTS = create("remove_effects");

    /**
     * Key for {@code minecraft:teleport_randomly}.
     */
    public static final TypedKey<ConsumeEffectType> TELEPORT_RANDOMLY = create("teleport_randomly");

    private static final List<TypedKey<ConsumeEffectType>> VALUES = List.of(
        APPLY_EFFECTS,
        CLEAR_ALL_EFFECTS,
        PLAY_SOUND,
        REMOVE_EFFECTS,
        TELEPORT_RANDOMLY
    );

    private ConsumeEffectTypeKeys() {
        throw new UnsupportedOperationException("ConsumeEffectTypeKeys cannot be instantiated.");
    }

    private static TypedKey<ConsumeEffectType> create(@KeyPattern final String value) {
        return TypedKey.withRegistry(RegistryKey.CONSUME_EFFECT_TYPE, value);
    }

    /**
     * Returns a stream containing all keys declared by this class.
     *
     * @return a stream of registry keys
     */
    public static Stream<TypedKey<ConsumeEffectType>> values() {
        return VALUES.stream();
    }
}
