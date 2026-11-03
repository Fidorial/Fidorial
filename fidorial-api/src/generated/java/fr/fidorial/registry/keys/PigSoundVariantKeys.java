package fr.fidorial.registry.keys;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.PigSoundVariant;
import java.util.List;
import java.util.stream.Stream;
import net.kyori.adventure.key.KeyPattern;

/**
 * Typed keys for entries in the {@code minecraft:pig_sound_variant} registry.
 */
public final class PigSoundVariantKeys {
    /**
     * Key for {@code minecraft:big}.
     */
    public static final TypedKey<PigSoundVariant> BIG = create("big");

    /**
     * Key for {@code minecraft:classic}.
     */
    public static final TypedKey<PigSoundVariant> CLASSIC = create("classic");

    /**
     * Key for {@code minecraft:mini}.
     */
    public static final TypedKey<PigSoundVariant> MINI = create("mini");

    private static final List<TypedKey<PigSoundVariant>> VALUES = List.of(
        BIG,
        CLASSIC,
        MINI
    );

    private PigSoundVariantKeys() {
        throw new UnsupportedOperationException("PigSoundVariantKeys cannot be instantiated.");
    }

    private static TypedKey<PigSoundVariant> create(@KeyPattern final String value) {
        return TypedKey.withRegistry(RegistryKey.PIG_SOUND_VARIANT, value);
    }

    /**
     * Returns a stream containing all keys declared by this class.
     *
     * @return a stream of registry keys
     */
    public static Stream<TypedKey<PigSoundVariant>> values() {
        return VALUES.stream();
    }
}
