package fr.euphyllia.fidorial.server.debug;

import fr.euphyllia.fidorial.server.codecs.networking.NetworkCodec;
import fr.euphyllia.fidorial.server.debug.DebugChannel.Scope;
import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.registry.data.FrozenRegistries;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.DebugSubscription;
import fr.fidorial.registry.keys.DebugSubscriptionKeys;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The supported debug subscriptions. Anything not registered here is accepted and ignored.
 */
public final class DebugChannels {

    private static final List<Key> NETWORK_ORDER = networkOrder();
    private static final Map<Integer, DebugChannel<?>> BY_NETWORK_ID = new HashMap<>();

    public static final DebugChannel<Void> DEDICATED_SERVER_TICK_TIME =
            register(DebugSubscriptionKeys.DEDICATED_SERVER_TICK_TIME, Scope.SAMPLE, null);
    public static final DebugChannel<DebugValues.GoalSelectorInfo> GOAL_SELECTORS =
            register(DebugSubscriptionKeys.GOAL_SELECTORS, Scope.ENTITY, DebugValues.GoalSelectorInfo.CODEC);
    public static final DebugChannel<DebugValues.PathInfo> ENTITY_PATHS =
            register(DebugSubscriptionKeys.ENTITY_PATHS, Scope.ENTITY, DebugValues.PathInfo.CODEC);
    public static final DebugChannel<DebugValues.BrainInfo> BRAINS =
            register(DebugSubscriptionKeys.BRAINS, Scope.ENTITY, DebugValues.BrainInfo.CODEC);
    public static final DebugChannel<DebugValues.BlockIntersection> ENTITY_BLOCK_INTERSECTIONS =
            register(DebugSubscriptionKeys.ENTITY_BLOCK_INTERSECTIONS, Scope.BLOCK, DebugValues.BlockIntersection.CODEC);
    public static final DebugChannel<DebugValues.StructuresInfo> STRUCTURES =
            register(DebugSubscriptionKeys.STRUCTURES, Scope.CHUNK, DebugValues.StructuresInfo.CODEC);
    public static final DebugChannel<DebugValues.GameEventInfo> GAME_EVENTS =
            register(DebugSubscriptionKeys.GAME_EVENTS, Scope.EVENT, DebugValues.GameEventInfo.CODEC);

    private DebugChannels() {
        throw new UnsupportedOperationException("DebugChannels cannot be instantiated.");
    }

    public static void bootstrap() {
    }

    public static @Nullable DebugChannel<?> byNetworkId(final int id) {
        return BY_NETWORK_ID.get(id);
    }

    private static List<Key> networkOrder() {
        return FrozenRegistries.entries().get(RegistryKey.DEBUG_SUBSCRIPTION.key());
    }

    private static <T> DebugChannel<T> register(final TypedKey<DebugSubscription> key, final Scope scope, final @Nullable NetworkCodec<PacketBuffer, T> codec) {
        final int id = NETWORK_ORDER.indexOf(key.key());
        final DebugChannel<T> channel = new DebugChannel<>(key, id, scope, codec);
        BY_NETWORK_ID.put(id, channel);
        return channel;
    }
}
