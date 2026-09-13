package fr.euphyllia.fidorial.server.testing;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.network.ClientConnection;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.fidorial.entity.GameMode;
import fr.fidorial.entity.Player;
import fr.fidorial.entity.PlayerProfile;
import fr.fidorial.inventory.EnderChestInventory;
import fr.fidorial.inventory.PlayerInventory;
import fr.fidorial.world.Location;

import java.util.UUID;

final class ScenarioTestPlayer {

    static Player spawn(final FidorialServer server, final ServerWorld world, final String name, final Location location, final GameMode gameMode) {
        final PlayerProfile profile = new PlayerProfile(UUID.nameUUIDFromBytes(("test:" + name).getBytes()), name);
        final ClientConnection connection = ClientConnection.mock(server);

        final ServerPlayer player = new ServerPlayer(
                server.entityIds().allocate(),
                profile,
                new PlayerInventory(),
                new EnderChestInventory(),
                gameMode,
                connection,
                world,
                location);

        connection.setProfile(profile);
        connection.setPlayer(player);
        world.addEntity(player);
        connection.bindMockPlayer(player);
        server.addPlayerConnection(connection);
        player.invalidatePermissions();
        return player;
    }

    static void despawn(final FidorialServer server, final ServerPlayer player) {
        final ClientConnection connection = player.connection();
        connection.mockDisconnect();
        server.removePlayerConnection(connection);
    }
}
