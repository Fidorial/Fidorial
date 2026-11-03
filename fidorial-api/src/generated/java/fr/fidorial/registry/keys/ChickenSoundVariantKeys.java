package fr.fidorial.registry.keys;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.ChickenSoundVariant;
import java.util.List;
import java.util.stream.Stream;
import net.kyori.adventure.key.KeyPattern;

/**
 * Typed keys for entries in the {@code minecraft:chicken_sound_variant} registry.
 */
public final class ChickenSoundVariantKeys {
    /**
     * Key for {@code minecraft:classic}.
     */
    public static final TypedKey<ChickenSoundVariant> CLASSIC = create("classic");

    /**
     * Key for {@code minecraft:picky}.
     */
    public static final TypedKey<ChickenSoundVariant> PICKY = create("picky");

    private static final List<TypedKey<ChickenSoundVariant>> VALUES = List.of(
        CLASSIC,
        PICKY
    );

    private ChickenSoundVariantKeys() {
        throw new UnsupportedOperationException("ChickenSoundVariantKeys cannot be instantiated.");
    }

    private static TypedKey<ChickenSoundVariant> create(@KeyPattern final String value) {
        return TypedKey.withRegistry(RegistryKey.CHICKEN_SOUND_VARIANT, value);
    }

    /**
     * Returns a stream containing all keys declared by this class.
     *
     * @return a stream of registry keys
     */
    public static Stream<TypedKey<ChickenSoundVariant>> values() {
        return VALUES.stream();
    }
}
