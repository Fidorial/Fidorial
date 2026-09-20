package fr.euphyllia.fidorial.server.tests;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.fidorial.entity.GameMode;
import fr.fidorial.entity.Player;
import fr.fidorial.math.Location;
import fr.fidorial.testing.ScenarioTestHelper;
import fr.fidorial.testing.annotation.ScenarioTest;
import fr.fidorial.world.World;
import fr.fidorial.world.WorldBuilder;
import net.kyori.adventure.key.Key;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("unused")
public final class PlayerTests {

    @ScenarioTest(timeoutTicks = 60)
    public static void crossWorldTeleport(final ScenarioTestHelper helper) {
        final World destinationWorld = crossWorldDestination();
        final Player player = helper.summonPlayer("Teleporting", Location.of(destinationWorld, 0.5, 65, 0.5, 0f, 0f), GameMode.SURVIVAL);
        final Location destination = Location.of(destinationWorld, 5.5, 70, 5.5, 0f, 0f);

        final AtomicReference<CompletableFuture<Boolean>> teleportFuture = new AtomicReference<>();
        helper.sequence()
                .execute(() -> teleportFuture.set(player.teleport(destination)))
                .waitUntil(() -> teleportFuture.get().isDone(), "Expected teleport to complete")
                .execute(() -> helper.assertTrue(teleportFuture.get().join(), "Expected teleport to succeed"))
                .waitUntil(() -> player.world() == destinationWorld,
                        "Expected the player's world to have switched to the destination")
                .waitUntil(() -> player.location().equals(destination),
                        "Expected player at " + destination + " in the destination world but was at " + player.location())
                .build();
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void respawnInAnotherWorld(final ScenarioTestHelper helper) {
        final World destinationWorld = crossWorldDestination();
        final Player player = helper.summonPlayer("Respawning", Location.of(destinationWorld, 0.5, 65, 0.5, 0f, 0f), GameMode.SURVIVAL);
        final Location point = Location.of(destinationWorld, 1.5, 70, 1.5, 0f, 0f);

        player.setRespawnPoint(point);
        final AtomicReference<CompletableFuture<Boolean>> respawnFuture = new AtomicReference<>();

        helper.sequence()
                .execute(() -> player.kill(player))
                .execute(() -> respawnFuture.set(player.respawn()))
                .waitUntil(() -> respawnFuture.get().isDone(), "Expected respawn to complete")
                .execute(() -> helper.assertTrue(respawnFuture.get().join(), "Expected respawn to succeed"))
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
