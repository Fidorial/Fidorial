package fr.fidorial.registry.keys;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.CatSoundVariant;
import java.util.List;
import java.util.stream.Stream;
import net.kyori.adventure.key.KeyPattern;

/**
 * Typed keys for entries in the {@code minecraft:cat_sound_variant} registry.
 */
public final class CatSoundVariantKeys {
    /**
     * Key for {@code minecraft:classic}.
     */
    public static final TypedKey<CatSoundVariant> CLASSIC = create("classic");

    /**
     * Key for {@code minecraft:royal}.
     */
    public static final TypedKey<CatSoundVariant> ROYAL = create("royal");

    private static final List<TypedKey<CatSoundVariant>> VALUES = List.of(
        CLASSIC,
        ROYAL
    );

    private CatSoundVariantKeys() {
        throw new UnsupportedOperationException("CatSoundVariantKeys cannot be instantiated.");
    }

    private static TypedKey<CatSoundVariant> create(@KeyPattern final String value) {
        return TypedKey.withRegistry(RegistryKey.CAT_SOUND_VARIANT, value);
    }

    /**
     * Returns a stream containing all keys declared by this class.
     *
     * @return a stream of registry keys
     */
    public static Stream<TypedKey<CatSoundVariant>> values() {
        return VALUES.stream();
    }
}
