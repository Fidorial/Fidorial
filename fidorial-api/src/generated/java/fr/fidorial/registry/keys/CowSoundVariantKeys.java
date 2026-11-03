package fr.fidorial.registry.keys;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.CowSoundVariant;
import java.util.List;
import java.util.stream.Stream;
import net.kyori.adventure.key.KeyPattern;

/**
 * Typed keys for entries in the {@code minecraft:cow_sound_variant} registry.
 */
public final class CowSoundVariantKeys {
    /**
     * Key for {@code minecraft:classic}.
     */
    public static final TypedKey<CowSoundVariant> CLASSIC = create("classic");

    /**
     * Key for {@code minecraft:moody}.
     */
    public static final TypedKey<CowSoundVariant> MOODY = create("moody");

    private static final List<TypedKey<CowSoundVariant>> VALUES = List.of(
        CLASSIC,
        MOODY
    );

    private CowSoundVariantKeys() {
        throw new UnsupportedOperationException("CowSoundVariantKeys cannot be instantiated.");
    }

    private static TypedKey<CowSoundVariant> create(@KeyPattern final String value) {
        return TypedKey.withRegistry(RegistryKey.COW_SOUND_VARIANT, value);
    }

    /**
     * Returns a stream containing all keys declared by this class.
     *
     * @return a stream of registry keys
     */
    public static Stream<TypedKey<CowSoundVariant>> values() {
        return VALUES.stream();
    }
}
