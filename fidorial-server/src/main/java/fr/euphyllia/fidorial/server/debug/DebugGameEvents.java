package fr.euphyllia.fidorial.server.debug;

import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundDebugEventPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.utils.LocationPositionData;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.utils.PositionData;
import fr.euphyllia.fidorial.server.registry.data.FrozenRegistries;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.math.Location;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.GameEvent;
import fr.fidorial.world.ChunkPos;
import fr.fidorial.world.World;
import net.kyori.adventure.key.Key;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Emits {@code minecraft:game_events} debug events.
 */
public final class DebugGameEvents {

    private final DebugSubscribers subscribers;
    private final Supplier<? extends Collection<ServerPlayer>> players;
    private final Map<Key, Integer> networkIds;

    public DebugGameEvents(final DebugSubscribers subscribers, final Supplier<? extends Collection<ServerPlayer>> players) {
        this.subscribers = subscribers;
        this.players = players;
        final List<Key> entries = FrozenRegistries.entries().get(RegistryKey.GAME_EVENT.key());
        if (entries == null) {
            throw new IllegalStateException("minecraft:game_event is missing from FrozenRegistries");
        }
        final Map<Key, Integer> ids = new HashMap<>(entries.size());
        for (int id = 0; id < entries.size(); id++) {
            ids.put(entries.get(id), id);
        }
        this.networkIds = Map.copyOf(ids);
    }

    public void emit(final World world, final TypedKey<GameEvent> event, final Location location) {
        emit(world, event, LocationPositionData.vec3(location));
    }

    public void emit(final World world, final TypedKey<GameEvent> event, final BlockPosition pos) {
        emit(world, event, new PositionData.Vec3D(pos.x() + 0.5, pos.y() + 0.5, pos.z() + 0.5));
    }

    public void emit(final World world, final TypedKey<GameEvent> event, final PositionData.Vec3D position) {
        final DebugChannel<DebugValues.GameEventInfo> channel = DebugChannels.GAME_EVENTS;
        if (!subscribers.isActive(channel)) {
            return;
        }
        final Integer id = networkIds.get(event.key());
        if (id == null) {
            throw new IllegalArgumentException("Unknown game event " + event.key().asString());
        }
        final int chunkX = (int) Math.floor(position.x()) >> 4;
        final int chunkZ = (int) Math.floor(position.z()) >> 4;
        ClientboundDebugEventPacket<DebugValues.GameEventInfo> packet = null;
        for (final ServerPlayer player : players.get()) {
            if (!DebugSubscribers.wants(player, channel) || !player.world().equals(world)) {
                continue;
            }
            final ChunkPos center = player.chunk();
            final int range = player.connection().effectiveViewDistance();
            if (Math.abs(center.x() - chunkX) > range || Math.abs(center.z() - chunkZ) > range) {
                continue;
            }
            if (packet == null) {
                packet = new ClientboundDebugEventPacket<>(channel, new DebugValues.GameEventInfo(id, position));
            }
            player.connection().send(packet);
        }
    }
}
