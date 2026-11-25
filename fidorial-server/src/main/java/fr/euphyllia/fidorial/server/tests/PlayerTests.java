package fr.euphyllia.fidorial.server.tests;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.fidorial.entity.GameMode;
import fr.fidorial.entity.player.Player;
import fr.fidorial.math.Location;
import fr.fidorial.testing.ScenarioTestHelper;
import fr.fidorial.testing.annotation.ScenarioTest;
import fr.fidorial.world.World;
import fr.fidorial.world.WorldSpec;
import net.kyori.adventure.key.Key;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("unused")
public final class PlayerTests {

    @ScenarioTest(timeoutTicks = 60)
    public static void crossWorldTeleport(final ScenarioTestHelper helper) {
        final World destinationWorld = crossWorldDestination();
        final Player player = helper.summonPlayer("Teleporting", Location.of(helper.world(), 0.5, 65, 0.5, 0f, 0f), GameMode.SURVIVAL);
        final Location destination = Location.of(destinationWorld, 5.5, 70, 5.5, 0f, 0f);

        final AtomicReference<CompletableFuture<Boolean>> teleportFuture = new AtomicReference<>();
        helper.sequence()
                .execute(() -> teleportFuture.set(player.teleport(destination)))
                .waitUntil(() -> teleportFuture.get().isDone(), "Expected teleport to complete")
                .execute(() -> helper.assertTrue(teleportFuture.get().join(), "Expected teleport to succeed"))
                .waitUntil(() -> player.world().equals(destinationWorld),
                        "Expected the player's world to have switched to the destination")
                .waitUntil(() -> player.location().equals(destination),
                        "Expected player at " + destination + " in the destination world but was at " + player.location())
                .build();
    }

    @ScenarioTest(timeoutTicks = 20)
    public static void respawnInAnotherWorld(final ScenarioTestHelper helper) {
        if (true) { // Todo
            helper.sequence().build();
            return;
        }
        final World destinationWorld = crossWorldDestination();
        final Player player = helper.summonPlayer("Respawning", Location.of(helper.world(), 0.5, 65, 0.5, 0f, 0f), GameMode.SURVIVAL);
        final Location point = Location.of(destinationWorld, 1.5, 70, 1.5, 0f, 0f);

        player.setRespawnPoint(destinationWorld, point);
        final AtomicReference<CompletableFuture<Boolean>> respawnFuture = new AtomicReference<>();

        helper.sequence()
                .execute(() -> player.kill(player))
                .execute(() -> respawnFuture.set(player.respawn()))
                .waitUntil(() -> respawnFuture.get().isDone(), "Expected respawn to complete")
                .execute(() -> helper.assertTrue(respawnFuture.get().join(), "Expected respawn to succeed"))
                .waitUntil(() -> player.world().equals(destinationWorld),
                        "Expected the player to be respawned in the explicitly given world, not the default one")
                .waitUntil(() -> player.location().equals(point),
                        "Expected the player location after respawning to match what was set, got " + player.location())
                .build();
    }

    @ScenarioTest(timeoutTicks = 500) // IO heavy
    public static void stressTest(final ScenarioTestHelper helper) {
        final World world = helper.world();
        final List<Player> players = new ArrayList<>(1000);
        for (int i = 0; i < 1000; i++) {
            players.add(helper.summonPlayer(String.valueOf(i), randomLocation(world), GameMode.SURVIVAL));
        }
        final List<CompletableFuture<Boolean>> teleports = new ArrayList<>(players.size());

        helper.sequence()
                .waitUntil(() -> players.stream().allMatch(player -> world.entity(player.entityId()).isPresent()),
                        "Expected every player to be in the world")
                .waitUntil(() -> new HashSet<>(FidorialServer.getInstance().players().online()).containsAll(players),
                        "Expected every player to be online")
                .execute(() -> {
                    for (final Player player : players) {
                        teleports.add(player.teleport(randomLocation(world)));
                    }
                })
                .waitUntil(() -> teleports.stream().allMatch(CompletableFuture::isDone),
                        "Expected every teleport to complete")
                .execute(() -> helper.assertTrue(teleports.stream().allMatch(CompletableFuture::join),
                        "Expected every teleport to succeed"))
                .build();
    }

    private static Location randomLocation(final World world) {
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        return Location.of(
                world,
                random.nextDouble(-1000, 1000),
                65,
                random.nextDouble(-1000, 1000),
                random.nextFloat(-180f, 180f),
                random.nextFloat(-90f, 90f));
    }

    private static World crossWorldDestination() {
        final FidorialServer server = FidorialServer.getInstance();
        final Key key = Key.key("scenario_test", "cross_world_destination");
        final World world = server.worlds().manager().world(key);
        return world != null ? world : server.worlds().create(WorldSpec.builder(key).build());
    }
}
