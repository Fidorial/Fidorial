package fr.euphyllia.fidorial.server.entity.player;

import fr.euphyllia.fidorial.server.network.ClientConnection;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.fidorial.math.Location;
import fr.fidorial.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The connections attached to the server and the players behind them.
 *
 * <p>This has no dependency, so it can be handed to any component at construction time (as
 * {@code online::players} or {@code online::broadcast}) instead of a reference to the whole
 * server. Modules that must react to joins and leaves register a {@link Listener}.</p>
 */
public final class PlayerList {

    private final Set<ClientConnection> connections = ConcurrentHashMap.newKeySet();
    private final List<Listener> listeners = new CopyOnWriteArrayList<>();
    private volatile List<ServerPlayer> snapshot = List.of();

    /**
     * Calls {@code listener} after every connection added or removed.
     *
     * @param listener the listener
     */
    public void addListener(final Listener listener) {
        listeners.add(listener);
    }

    /**
     * Adds a connection and refreshes the player snapshot.
     *
     * @param connection the connection
     */
    public void add(final ClientConnection connection) {
        connections.add(connection);
        refreshSnapshot();
        for (final Listener listener : listeners) {
            listener.onConnect(connection);
        }
    }

    /**
     * Removes a connection and refreshes the player snapshot.
     *
     * @param connection the connection
     */
    public void remove(final ClientConnection connection) {
        connections.remove(connection);
        refreshSnapshot();
        for (final Listener listener : listeners) {
            listener.onDisconnect(connection);
        }
    }

    /**
     * {@return an immutable snapshot of the players currently connected}
     */
    public List<ServerPlayer> players() {
        return snapshot;
    }

    /**
     * {@return the number of connections, including the ones that do not have a player yet}
     */
    public int connectionCount() {
        return connections.size();
    }

    /**
     * Sends a packet to every connection.
     *
     * @param packet the packet
     */
    public void broadcast(final ClientboundPacket packet) {
        for (final ClientConnection connection : connections) {
            connection.send(packet);
        }
    }

    /**
     * Sends a packet to the players of {@code world} within {@code radius} blocks of a point.
     *
     * @param world  the world
     * @param x      the x coordinate of the point
     * @param y      the y coordinate of the point
     * @param z      the z coordinate of the point
     * @param radius the radius, in blocks
     * @param packet the packet
     */
    public void broadcastNear(
            final World world, final double x, final double y, final double z, final double radius,
            final ClientboundPacket packet) {
        final double radiusSq = radius * radius;
        for (final ServerPlayer player : snapshot) {
            if (player.isRemoved() || !player.world().equals(world)) {
                continue;
            }
            final Location loc = player.location();
            final double dx = loc.x() - x;
            final double dy = loc.y() - y;
            final double dz = loc.z() - z;
            if (dx * dx + dy * dy + dz * dz <= radiusSq) {
                player.connection().send(packet);
            }
        }
    }

    // synchronized so that two concurrent refreshes cannot publish an outdated snapshot last
    private synchronized void refreshSnapshot() {
        final List<ServerPlayer> players = new ArrayList<>(connections.size());
        for (final ClientConnection connection : connections) {
            final ServerPlayer player = connection.player();
            if (player != null) {
                players.add(player);
            }
        }
        this.snapshot = List.copyOf(players);
    }

    /**
     * Reacts to connections being added or removed.
     */
    public interface Listener {

        default void onConnect(final ClientConnection connection) {
        }

        default void onDisconnect(final ClientConnection connection) {
        }
    }
}
