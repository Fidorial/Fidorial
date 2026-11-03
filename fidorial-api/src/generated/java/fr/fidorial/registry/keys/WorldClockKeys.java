package fr.fidorial.registry.keys;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.WorldClock;
import java.util.List;
import java.util.stream.Stream;
import net.kyori.adventure.key.KeyPattern;

/**
 * Typed keys for entries in the {@code minecraft:world_clock} registry.
 */
public final class WorldClockKeys {
    /**
     * Key for {@code minecraft:overworld}.
     */
    public static final TypedKey<WorldClock> OVERWORLD = create("overworld");

    /**
     * Key for {@code minecraft:the_end}.
     */
    public static final TypedKey<WorldClock> THE_END = create("the_end");

    private static final List<TypedKey<WorldClock>> VALUES = List.of(
        OVERWORLD,
        THE_END
    );

    private WorldClockKeys() {
        throw new UnsupportedOperationException("WorldClockKeys cannot be instantiated.");
    }

    private static TypedKey<WorldClock> create(@KeyPattern final String value) {
        return TypedKey.withRegistry(RegistryKey.WORLD_CLOCK, value);
    }

    /**
     * Returns a stream containing all keys declared by this class.
     *
     * @return a stream of registry keys
     */
    public static Stream<TypedKey<WorldClock>> values() {
        return VALUES.stream();
    }
}
