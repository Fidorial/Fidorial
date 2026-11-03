package fr.fidorial.registry.keys;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.SlotDisplay;
import java.util.List;
import java.util.stream.Stream;
import net.kyori.adventure.key.KeyPattern;

/**
 * Typed keys for entries in the {@code minecraft:slot_display} registry.
 */
public final class SlotDisplayKeys {
    /**
     * Key for {@code minecraft:any_fuel}.
     */
    public static final TypedKey<SlotDisplay> ANY_FUEL = create("any_fuel");

    /**
     * Key for {@code minecraft:composite}.
     */
    public static final TypedKey<SlotDisplay> COMPOSITE = create("composite");

    /**
     * Key for {@code minecraft:dyed}.
     */
    public static final TypedKey<SlotDisplay> DYED = create("dyed");

    /**
     * Key for {@code minecraft:empty}.
     */
    public static final TypedKey<SlotDisplay> EMPTY = create("empty");

    /**
     * Key for {@code minecraft:item}.
     */
    public static final TypedKey<SlotDisplay> ITEM = create("item");

    /**
     * Key for {@code minecraft:item_stack}.
     */
    public static final TypedKey<SlotDisplay> ITEM_STACK = create("item_stack");

    /**
     * Key for {@code minecraft:only_with_component}.
     */
    public static final TypedKey<SlotDisplay> ONLY_WITH_COMPONENT = create("only_with_component");

    /**
     * Key for {@code minecraft:smithing_trim}.
     */
    public static final TypedKey<SlotDisplay> SMITHING_TRIM = create("smithing_trim");

    /**
     * Key for {@code minecraft:tag}.
     */
    public static final TypedKey<SlotDisplay> TAG = create("tag");

    /**
     * Key for {@code minecraft:with_any_potion}.
     */
    public static final TypedKey<SlotDisplay> WITH_ANY_POTION = create("with_any_potion");

    /**
     * Key for {@code minecraft:with_remainder}.
     */
    public static final TypedKey<SlotDisplay> WITH_REMAINDER = create("with_remainder");

    private static final List<TypedKey<SlotDisplay>> VALUES = List.of(
        ANY_FUEL,
        COMPOSITE,
        DYED,
        EMPTY,
        ITEM,
        ITEM_STACK,
        ONLY_WITH_COMPONENT,
        SMITHING_TRIM,
        TAG,
        WITH_ANY_POTION,
        WITH_REMAINDER
    );

    private SlotDisplayKeys() {
        throw new UnsupportedOperationException("SlotDisplayKeys cannot be instantiated.");
    }

    private static TypedKey<SlotDisplay> create(@KeyPattern final String value) {
        return TypedKey.withRegistry(RegistryKey.SLOT_DISPLAY, value);
    }

    /**
     * Returns a stream containing all keys declared by this class.
     *
     * @return a stream of registry keys
     */
    public static Stream<TypedKey<SlotDisplay>> values() {
        return VALUES.stream();
    }
}
