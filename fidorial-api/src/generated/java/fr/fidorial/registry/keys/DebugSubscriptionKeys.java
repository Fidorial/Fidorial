package fr.fidorial.registry.keys;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.DebugSubscription;
import java.util.List;
import java.util.stream.Stream;
import net.kyori.adventure.key.KeyPattern;

/**
 * Typed keys for entries in the {@code minecraft:debug_subscription} registry.
 */
public final class DebugSubscriptionKeys {
    /**
     * Key for {@code minecraft:bee_hives}.
     */
    public static final TypedKey<DebugSubscription> BEE_HIVES = create("bee_hives");

    /**
     * Key for {@code minecraft:bees}.
     */
    public static final TypedKey<DebugSubscription> BEES = create("bees");

    /**
     * Key for {@code minecraft:brains}.
     */
    public static final TypedKey<DebugSubscription> BRAINS = create("brains");

    /**
     * Key for {@code minecraft:breezes}.
     */
    public static final TypedKey<DebugSubscription> BREEZES = create("breezes");

    /**
     * Key for {@code minecraft:dedicated_server_tick_time}.
     */
    public static final TypedKey<DebugSubscription> DEDICATED_SERVER_TICK_TIME = create("dedicated_server_tick_time");

    /**
     * Key for {@code minecraft:entity_block_intersections}.
     */
    public static final TypedKey<DebugSubscription> ENTITY_BLOCK_INTERSECTIONS = create("entity_block_intersections");

    /**
     * Key for {@code minecraft:entity_paths}.
     */
    public static final TypedKey<DebugSubscription> ENTITY_PATHS = create("entity_paths");

    /**
     * Key for {@code minecraft:game_event_listeners}.
     */
    public static final TypedKey<DebugSubscription> GAME_EVENT_LISTENERS = create("game_event_listeners");

    /**
     * Key for {@code minecraft:game_events}.
     */
    public static final TypedKey<DebugSubscription> GAME_EVENTS = create("game_events");

    /**
     * Key for {@code minecraft:goal_selectors}.
     */
    public static final TypedKey<DebugSubscription> GOAL_SELECTORS = create("goal_selectors");

    /**
     * Key for {@code minecraft:neighbor_updates}.
     */
    public static final TypedKey<DebugSubscription> NEIGHBOR_UPDATES = create("neighbor_updates");

    /**
     * Key for {@code minecraft:pois}.
     */
    public static final TypedKey<DebugSubscription> POIS = create("pois");

    /**
     * Key for {@code minecraft:raids}.
     */
    public static final TypedKey<DebugSubscription> RAIDS = create("raids");

    /**
     * Key for {@code minecraft:redstone_wire_orientations}.
     */
    public static final TypedKey<DebugSubscription> REDSTONE_WIRE_ORIENTATIONS = create("redstone_wire_orientations");

    /**
     * Key for {@code minecraft:structures}.
     */
    public static final TypedKey<DebugSubscription> STRUCTURES = create("structures");

    /**
     * Key for {@code minecraft:village_sections}.
     */
    public static final TypedKey<DebugSubscription> VILLAGE_SECTIONS = create("village_sections");

    private static final List<TypedKey<DebugSubscription>> VALUES = List.of(
        BEE_HIVES,
        BEES,
        BRAINS,
        BREEZES,
        DEDICATED_SERVER_TICK_TIME,
        ENTITY_BLOCK_INTERSECTIONS,
        ENTITY_PATHS,
        GAME_EVENT_LISTENERS,
        GAME_EVENTS,
        GOAL_SELECTORS,
        NEIGHBOR_UPDATES,
        POIS,
        RAIDS,
        REDSTONE_WIRE_ORIENTATIONS,
        STRUCTURES,
        VILLAGE_SECTIONS
    );

    private DebugSubscriptionKeys() {
        throw new UnsupportedOperationException("DebugSubscriptionKeys cannot be instantiated.");
    }

    private static TypedKey<DebugSubscription> create(@KeyPattern final String value) {
        return TypedKey.create(RegistryKey.DEBUG_SUBSCRIPTION, value);
    }

    /**
     * Returns a stream containing all keys declared by this class.
     *
     * @return a stream of registry keys
     */
    public static Stream<TypedKey<DebugSubscription>> values() {
        return VALUES.stream();
    }
}
