package fr.fidorial.registry.keys;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.Instrument;
import java.util.List;
import java.util.stream.Stream;
import net.kyori.adventure.key.KeyPattern;

/**
 * Typed keys for entries in the {@code minecraft:instrument} registry.
 */
public final class InstrumentKeys {
    /**
     * Key for {@code minecraft:admire_goat_horn}.
     */
    public static final TypedKey<Instrument> ADMIRE_GOAT_HORN = create("admire_goat_horn");

    /**
     * Key for {@code minecraft:call_goat_horn}.
     */
    public static final TypedKey<Instrument> CALL_GOAT_HORN = create("call_goat_horn");

    /**
     * Key for {@code minecraft:dream_goat_horn}.
     */
    public static final TypedKey<Instrument> DREAM_GOAT_HORN = create("dream_goat_horn");

    /**
     * Key for {@code minecraft:feel_goat_horn}.
     */
    public static final TypedKey<Instrument> FEEL_GOAT_HORN = create("feel_goat_horn");

    /**
     * Key for {@code minecraft:ponder_goat_horn}.
     */
    public static final TypedKey<Instrument> PONDER_GOAT_HORN = create("ponder_goat_horn");

    /**
     * Key for {@code minecraft:seek_goat_horn}.
     */
    public static final TypedKey<Instrument> SEEK_GOAT_HORN = create("seek_goat_horn");

    /**
     * Key for {@code minecraft:sing_goat_horn}.
     */
    public static final TypedKey<Instrument> SING_GOAT_HORN = create("sing_goat_horn");

    /**
     * Key for {@code minecraft:yearn_goat_horn}.
     */
    public static final TypedKey<Instrument> YEARN_GOAT_HORN = create("yearn_goat_horn");

    private static final List<TypedKey<Instrument>> VALUES = List.of(
        ADMIRE_GOAT_HORN,
        CALL_GOAT_HORN,
        DREAM_GOAT_HORN,
        FEEL_GOAT_HORN,
        PONDER_GOAT_HORN,
        SEEK_GOAT_HORN,
        SING_GOAT_HORN,
        YEARN_GOAT_HORN
    );

    private InstrumentKeys() {
        throw new UnsupportedOperationException("InstrumentKeys cannot be instantiated.");
    }

    private static TypedKey<Instrument> create(@KeyPattern final String value) {
        return TypedKey.withRegistry(RegistryKey.INSTRUMENT, value);
    }

    /**
     * Returns a stream containing all keys declared by this class.
     *
     * @return a stream of registry keys
     */
    public static Stream<TypedKey<Instrument>> values() {
        return VALUES.stream();
    }
}
