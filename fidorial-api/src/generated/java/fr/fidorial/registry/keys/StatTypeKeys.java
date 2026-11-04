package fr.fidorial.registry.keys;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.StatType;
import java.util.List;
import java.util.stream.Stream;
import net.kyori.adventure.key.KeyPattern;

/**
 * Typed keys for entries in the {@code minecraft:stat_type} registry.
 */
public final class StatTypeKeys {
    /**
     * Key for {@code minecraft:broken}.
     */
    public static final TypedKey<StatType> BROKEN = create("broken");

    /**
     * Key for {@code minecraft:crafted}.
     */
    public static final TypedKey<StatType> CRAFTED = create("crafted");

    /**
     * Key for {@code minecraft:custom}.
     */
    public static final TypedKey<StatType> CUSTOM = create("custom");

    /**
     * Key for {@code minecraft:dropped}.
     */
    public static final TypedKey<StatType> DROPPED = create("dropped");

    /**
     * Key for {@code minecraft:killed}.
     */
    public static final TypedKey<StatType> KILLED = create("killed");

    /**
     * Key for {@code minecraft:killed_by}.
     */
    public static final TypedKey<StatType> KILLED_BY = create("killed_by");

    /**
     * Key for {@code minecraft:mined}.
     */
    public static final TypedKey<StatType> MINED = create("mined");

    /**
     * Key for {@code minecraft:picked_up}.
     */
    public static final TypedKey<StatType> PICKED_UP = create("picked_up");

    /**
     * Key for {@code minecraft:used}.
     */
    public static final TypedKey<StatType> USED = create("used");

    private static final List<TypedKey<StatType>> VALUES = List.of(
        BROKEN,
        CRAFTED,
        CUSTOM,
        DROPPED,
        KILLED,
        KILLED_BY,
        MINED,
        PICKED_UP,
        USED
    );

    private StatTypeKeys() {
        throw new UnsupportedOperationException("StatTypeKeys cannot be instantiated.");
    }

    private static TypedKey<StatType> create(@KeyPattern final String value) {
        return TypedKey.withRegistry(RegistryKey.STAT_TYPE, value);
    }

    /**
     * Returns a stream containing all keys declared by this class.
     *
     * @return a stream of registry keys
     */
    public static Stream<TypedKey<StatType>> values() {
        return VALUES.stream();
    }
}
