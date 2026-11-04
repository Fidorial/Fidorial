package fr.fidorial.registry.keys;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.TrimPattern;
import java.util.List;
import java.util.stream.Stream;
import net.kyori.adventure.key.KeyPattern;

/**
 * Typed keys for entries in the {@code minecraft:trim_pattern} registry.
 */
public final class TrimPatternKeys {
    /**
     * Key for {@code minecraft:bolt}.
     */
    public static final TypedKey<TrimPattern> BOLT = create("bolt");

    /**
     * Key for {@code minecraft:coast}.
     */
    public static final TypedKey<TrimPattern> COAST = create("coast");

    /**
     * Key for {@code minecraft:dune}.
     */
    public static final TypedKey<TrimPattern> DUNE = create("dune");

    /**
     * Key for {@code minecraft:eye}.
     */
    public static final TypedKey<TrimPattern> EYE = create("eye");

    /**
     * Key for {@code minecraft:flow}.
     */
    public static final TypedKey<TrimPattern> FLOW = create("flow");

    /**
     * Key for {@code minecraft:host}.
     */
    public static final TypedKey<TrimPattern> HOST = create("host");

    /**
     * Key for {@code minecraft:raiser}.
     */
    public static final TypedKey<TrimPattern> RAISER = create("raiser");

    /**
     * Key for {@code minecraft:rib}.
     */
    public static final TypedKey<TrimPattern> RIB = create("rib");

    /**
     * Key for {@code minecraft:sentry}.
     */
    public static final TypedKey<TrimPattern> SENTRY = create("sentry");

    /**
     * Key for {@code minecraft:shaper}.
     */
    public static final TypedKey<TrimPattern> SHAPER = create("shaper");

    /**
     * Key for {@code minecraft:silence}.
     */
    public static final TypedKey<TrimPattern> SILENCE = create("silence");

    /**
     * Key for {@code minecraft:snout}.
     */
    public static final TypedKey<TrimPattern> SNOUT = create("snout");

    /**
     * Key for {@code minecraft:spire}.
     */
    public static final TypedKey<TrimPattern> SPIRE = create("spire");

    /**
     * Key for {@code minecraft:tide}.
     */
    public static final TypedKey<TrimPattern> TIDE = create("tide");

    /**
     * Key for {@code minecraft:vex}.
     */
    public static final TypedKey<TrimPattern> VEX = create("vex");

    /**
     * Key for {@code minecraft:ward}.
     */
    public static final TypedKey<TrimPattern> WARD = create("ward");

    /**
     * Key for {@code minecraft:wayfinder}.
     */
    public static final TypedKey<TrimPattern> WAYFINDER = create("wayfinder");

    /**
     * Key for {@code minecraft:wild}.
     */
    public static final TypedKey<TrimPattern> WILD = create("wild");

    private static final List<TypedKey<TrimPattern>> VALUES = List.of(
        BOLT,
        COAST,
        DUNE,
        EYE,
        FLOW,
        HOST,
        RAISER,
        RIB,
        SENTRY,
        SHAPER,
        SILENCE,
        SNOUT,
        SPIRE,
        TIDE,
        VEX,
        WARD,
        WAYFINDER,
        WILD
    );

    private TrimPatternKeys() {
        throw new UnsupportedOperationException("TrimPatternKeys cannot be instantiated.");
    }

    private static TypedKey<TrimPattern> create(@KeyPattern final String value) {
        return TypedKey.withRegistry(RegistryKey.TRIM_PATTERN, value);
    }

    /**
     * Returns a stream containing all keys declared by this class.
     *
     * @return a stream of registry keys
     */
    public static Stream<TypedKey<TrimPattern>> values() {
        return VALUES.stream();
    }
}
