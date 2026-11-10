package fr.euphyllia.fidorial.server.network.protocol;

import fr.euphyllia.fidorial.server.network.connection.ConnectionState;
import fr.euphyllia.fidorial.server.network.protocol.catalog.ConfigurationClientboundPacketIds;
import fr.euphyllia.fidorial.server.network.protocol.catalog.ConfigurationServerboundPacketIds;
import fr.euphyllia.fidorial.server.network.protocol.catalog.HandshakeServerboundPacketIds;
import fr.euphyllia.fidorial.server.network.protocol.catalog.LoginClientboundPacketIds;
import fr.euphyllia.fidorial.server.network.protocol.catalog.LoginServerboundPacketIds;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPacketIds;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayServerboundPacketIds;
import fr.euphyllia.fidorial.server.network.protocol.catalog.StatusClientboundPacketIds;
import fr.euphyllia.fidorial.server.network.protocol.catalog.StatusServerboundPacketIds;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

import static fr.euphyllia.fidorial.server.VersionConstants.MINECRAFT_VERSION_ID;

public final class ProtocolMap {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(ProtocolMap.class);
    private final Map<ConnectionState, Direction> clientbound = new EnumMap<>(ConnectionState.class);
    private final Map<ConnectionState, Direction> serverbound = new EnumMap<>(ConnectionState.class);

    private ProtocolMap() {
        serverbound.put(ConnectionState.HANDSHAKE, Direction.of(HandshakeServerboundPacketIds.BY_IDENTIFIER));
        clientbound.put(ConnectionState.STATUS, Direction.of(StatusClientboundPacketIds.BY_IDENTIFIER));
        serverbound.put(ConnectionState.STATUS, Direction.of(StatusServerboundPacketIds.BY_IDENTIFIER));
        clientbound.put(ConnectionState.LOGIN, Direction.of(LoginClientboundPacketIds.BY_IDENTIFIER));
        serverbound.put(ConnectionState.LOGIN, Direction.of(LoginServerboundPacketIds.BY_IDENTIFIER));
        clientbound.put(ConnectionState.CONFIGURATION, Direction.of(ConfigurationClientboundPacketIds.BY_IDENTIFIER));
        serverbound.put(ConnectionState.CONFIGURATION, Direction.of(ConfigurationServerboundPacketIds.BY_IDENTIFIER));
        clientbound.put(ConnectionState.PLAY, Direction.of(PlayClientboundPacketIds.BY_IDENTIFIER));
        serverbound.put(ConnectionState.PLAY, Direction.of(PlayServerboundPacketIds.BY_IDENTIFIER));
    }

    public static ProtocolMap load() {
        final ProtocolMap map = new ProtocolMap();
        LOGGER.debug(
                "Loaded {} protocol table from the generated packet catalogs.",
                MINECRAFT_VERSION_ID);
        return map;
    }

    public boolean isAvailable() {
        return true;
    }

    public int clientboundId(final ConnectionState state, final Key name) {
        final int id = clientbound.getOrDefault(state, Direction.EMPTY).id(name);
        if (id < 0)
            throw new IllegalStateException("Paquet clientbound inconnu dans la table : " + state + "/" + name);
        return id;
    }

    public @Nullable Key serverboundName(final ConnectionState state, final int id) {
        return serverbound.getOrDefault(state, Direction.EMPTY).name(id);
    }

    public int serverboundId(final ConnectionState state, final Key name) {
        final int id = serverbound.getOrDefault(state, Direction.EMPTY).id(name);
        if (id < 0)
            throw new IllegalStateException("Paquet serverbound inconnu dans la table : " + state + "/" + name);
        return id;
    }

    /**
     * @param byName packet ids, {@code -1} for an unknown name
     * @param byId   packet names indexed by id; packet ids are dense, so an array beats a map
     */
    private record Direction(Object2IntMap<Key> byName, @Nullable Key[] byId) {

        static final Direction EMPTY = of(Object2IntMaps.emptyMap());

        static Direction of(final Object2IntMap<Key> byName) {
            final Object2IntOpenHashMap<Key> ids = new Object2IntOpenHashMap<>(byName);
            ids.defaultReturnValue(-1);
            ids.trim();
            final @Nullable Key[] names = new Key[ids.values().intStream().max().orElse(-1) + 1];
            for (final Object2IntMap.Entry<Key> entry : Object2IntMaps.fastIterable(byName)) {
                names[entry.getIntValue()] = entry.getKey();
            }
            return new Direction(Object2IntMaps.unmodifiable(ids), names);
        }

        int id(final Key name) {
            return byName.getInt(name);
        }

        @Nullable Key name(final int id) {
            return id >= 0 && id < byId.length ? byId[id] : null;
        }
    }
}
