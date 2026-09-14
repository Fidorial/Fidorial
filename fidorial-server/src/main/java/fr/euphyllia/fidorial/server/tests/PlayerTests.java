package fr.euphyllia.fidorial.server.tests;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.fidorial.entity.GameMode;
import fr.fidorial.entity.Player;
import fr.fidorial.testing.ScenarioTestHelper;
import fr.fidorial.testing.annotation.ScenarioTest;
import fr.fidorial.world.Location;
import fr.fidorial.world.World;
import fr.fidorial.world.WorldBuilder;
import net.kyori.adventure.key.Key;

@SuppressWarnings("unused")
public final class PlayerTests {

    @ScenarioTest(timeoutTicks = 60)
    public static void crossWorldTeleport(final ScenarioTestHelper helper) {
        final World destinationWorld = crossWorldDestination();
        final Player player = helper.summonPlayer("Teleporting", new Location(0.5, 65, 0.5, 0f, 0f), GameMode.SURVIVAL);
        final Location destination = new Location(5.5, 70, 5.5, 0f, 0f);

        helper.sequence()
                .waitUntil(() -> helper.assertTrue(player.teleport(destinationWorld, destination), "Expected teleport to succeed"))
                .waitUntil(() -> player.world() == destinationWorld,
                        "Expected the player's world to have switched to the destination")
                .waitUntil(() -> player.location().equals(destination),
                        "Expected player at " + destination + " in the destination world but was at " + player.location())
                .build();
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void respawnInAnotherWorld(final ScenarioTestHelper helper) {
        final World destinationWorld = crossWorldDestination();
        final Player player = helper.summonPlayer("Respawning", new Location(0.5, 65, 0.5, 0f, 0f), GameMode.SURVIVAL);
        final Location point = new Location(1.5, 70, 1.5, 0f, 0f);

        player.setRespawnPoint(destinationWorld, point);

        helper.sequence()
                .execute(() -> player.kill(player))
                .waitUntil(player::respawn, "Expected respawn to succeed")
                .waitUntil(() -> player.world() == destinationWorld,
                        "Expected the player to be respawned in the explicitly given world, not the default one")
                .waitUntil(() -> player.location().equals(point),
                        "Expected the player location after respawning to match what was set, got " + player.location())
                .build();
    }

    private static World crossWorldDestination() {
        final FidorialServer server = FidorialServer.getInstance();
        final Key key = Key.key("scenario_test", "cross_world_destination");
        final World world = server.worldManager().world((key));
        return world != null ? world : server.createWorld(WorldBuilder.builder(key).build());
    }
}
