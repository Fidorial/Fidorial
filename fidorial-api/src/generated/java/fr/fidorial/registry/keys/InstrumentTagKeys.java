package fr.fidorial.registry.keys;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TagKey;
import fr.fidorial.registry.data.Instrument;
import net.kyori.adventure.key.KeyPattern;

/**
 * Keys of the vanilla tags of the {@code minecraft:instrument} registry.
 */
public final class InstrumentTagKeys {
    /**
     * Tag {@code #minecraft:goat_horns}.
     */
    public static final TagKey<Instrument> GOAT_HORNS = create("goat_horns");

    /**
     * Tag {@code #minecraft:regular_goat_horns}.
     */
    public static final TagKey<Instrument> REGULAR_GOAT_HORNS = create("regular_goat_horns");

    /**
     * Tag {@code #minecraft:screaming_goat_horns}.
     */
    public static final TagKey<Instrument> SCREAMING_GOAT_HORNS = create("screaming_goat_horns");

    private InstrumentTagKeys() {
        throw new UnsupportedOperationException("InstrumentTagKeys cannot be instantiated.");
    }

    private static TagKey<Instrument> create(@KeyPattern final String value) {
        return TagKey.create(RegistryKey.INSTRUMENT, value);
    }
}
