package fr.fidorial.registry.keys;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.Timeline;
import java.util.List;
import java.util.stream.Stream;
import net.kyori.adventure.key.KeyPattern;

/**
 * Typed keys for entries in the {@code minecraft:timeline} registry.
 */
public final class TimelineKeys {
    /**
     * Key for {@code minecraft:day}.
     */
    public static final TypedKey<Timeline> DAY = create("day");

    /**
     * Key for {@code minecraft:early_game}.
     */
    public static final TypedKey<Timeline> EARLY_GAME = create("early_game");

    /**
     * Key for {@code minecraft:moon}.
     */
    public static final TypedKey<Timeline> MOON = create("moon");

    /**
     * Key for {@code minecraft:villager_schedule}.
     */
    public static final TypedKey<Timeline> VILLAGER_SCHEDULE = create("villager_schedule");

    private static final List<TypedKey<Timeline>> VALUES = List.of(
        DAY,
        EARLY_GAME,
        MOON,
        VILLAGER_SCHEDULE
    );

    private TimelineKeys() {
        throw new UnsupportedOperationException("TimelineKeys cannot be instantiated.");
    }

    private static TypedKey<Timeline> create(@KeyPattern final String value) {
        return TypedKey.withRegistry(RegistryKey.TIMELINE, value);
    }

    /**
     * Returns a stream containing all keys declared by this class.
     *
     * @return a stream of registry keys
     */
    public static Stream<TypedKey<Timeline>> values() {
        return VALUES.stream();
    }
}
