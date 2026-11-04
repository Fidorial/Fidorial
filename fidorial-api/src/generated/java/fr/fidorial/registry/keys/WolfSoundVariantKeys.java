package fr.fidorial.registry.keys;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.WolfSoundVariant;
import java.util.List;
import java.util.stream.Stream;
import net.kyori.adventure.key.KeyPattern;

/**
 * Typed keys for entries in the {@code minecraft:wolf_sound_variant} registry.
 */
public final class WolfSoundVariantKeys {
    /**
     * Key for {@code minecraft:angry}.
     */
    public static final TypedKey<WolfSoundVariant> ANGRY = create("angry");

    /**
     * Key for {@code minecraft:big}.
     */
    public static final TypedKey<WolfSoundVariant> BIG = create("big");

    /**
     * Key for {@code minecraft:classic}.
     */
    public static final TypedKey<WolfSoundVariant> CLASSIC = create("classic");

    /**
     * Key for {@code minecraft:cute}.
     */
    public static final TypedKey<WolfSoundVariant> CUTE = create("cute");

    /**
     * Key for {@code minecraft:grumpy}.
     */
    public static final TypedKey<WolfSoundVariant> GRUMPY = create("grumpy");

    /**
     * Key for {@code minecraft:puglin}.
     */
    public static final TypedKey<WolfSoundVariant> PUGLIN = create("puglin");

    /**
     * Key for {@code minecraft:sad}.
     */
    public static final TypedKey<WolfSoundVariant> SAD = create("sad");

    private static final List<TypedKey<WolfSoundVariant>> VALUES = List.of(
        ANGRY,
        BIG,
        CLASSIC,
        CUTE,
        GRUMPY,
        PUGLIN,
        SAD
    );

    private WolfSoundVariantKeys() {
        throw new UnsupportedOperationException("WolfSoundVariantKeys cannot be instantiated.");
    }

    private static TypedKey<WolfSoundVariant> create(@KeyPattern final String value) {
        return TypedKey.withRegistry(RegistryKey.WOLF_SOUND_VARIANT, value);
    }

    /**
     * Returns a stream containing all keys declared by this class.
     *
     * @return a stream of registry keys
     */
    public static Stream<TypedKey<WolfSoundVariant>> values() {
        return VALUES.stream();
    }
}
